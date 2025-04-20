package dev.pandasystems.bambooloom.remapping

import dev.pandasystems.bambooloom.data.Mapping
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.io.path.createTempFile
import kotlin.time.ExperimentalTime

class RemapperTool(private val mapping: Mapping) {
	private val logger = LoggerFactory.getLogger(RemapperTool::class.java)

	@OptIn(ExperimentalTime::class)
	fun remap(jarFile: File) {
		val tempFile = createTempFile(jarFile.parentFile.toPath(), jarFile.nameWithoutExtension).toFile()
		val remapper = LoomRemapper(mapping)

		logger.info("Starting remapping process for $jarFile")
		JarFile(jarFile).use { jar ->
			val entryChunks = jar.entries().asSequence().chunked(1000)

			// Use ConcurrentHashMap to store remapped entries
			val remappedEntries = ConcurrentHashMap<String, ByteArray>()

			entryChunks.forEach { entries ->
				entries.parallelStream().forEach { entry ->
					val name = entry.name
					val extension = name.substringAfterLast('.', "")
					val pureName = name.removeSuffix(".$extension")

					val clazz = mapping[pureName]
					val newPureName = clazz?.to ?: pureName
					val newName = "$newPureName.$extension"

					val bytes = jar.getInputStream(entry).readBytes()
					val remappedBytes = if (clazz != null) {
//						logger.info("Remapping $pureName -> $newPureName")

						val classReader = ClassReader(bytes)
						val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)

						val classVisitor = ClassRemapper(classWriter, remapper)
						classReader.accept(classVisitor, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)

						classWriter.toByteArray()
					} else {
						bytes
					}

					remappedEntries[newName] = remappedBytes
				}
			}

			JarOutputStream(tempFile.outputStream()).use { outputStream ->
				for ((newName, bytes) in remappedEntries) {
					outputStream.putNextEntry(JarEntry(newName))
					outputStream.write(bytes)
					outputStream.closeEntry()
				}
			}
		}


		tempFile.copyTo(jarFile, true)
		tempFile.delete()
	}
}