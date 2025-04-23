package dev.pandasystems.bambooloom.remapping

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

class RemapperToolV2(private val remapper: LoomRemapperV2) {
	private val logger = LoggerFactory.getLogger(RemapperToolV2::class.java)

	fun remap(jarFile: File) {
		val tempFile = createTempFile(jarFile.parentFile.toPath(), jarFile.nameWithoutExtension).toFile()

		logger.info("Starting remapping process for $jarFile")
		JarFile(jarFile).use { jar ->
			// Use ConcurrentHashMap to store remapped entries
			val remappedEntries = ConcurrentHashMap<String, ByteArray>()

			for (entry in jar.entries()) {
				val unmappedFullName = entry.name
				val extension = "." + unmappedFullName.substringAfterLast('.', "")
				val unmappedName = unmappedFullName.removeSuffix(extension)

				val mappedName = remapper.classes[unmappedName] ?: unmappedName
				val mappedFullName = "$mappedName$extension"

				val bytes = jar.getInputStream(entry).readBytes()
				val remappedBytes = if (remapper.classes.containsKey(unmappedName)) {
					val classReader = ClassReader(bytes)
					val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)

					val classVisitor = ClassRemapper(classWriter, remapper)
					classReader.accept(classVisitor, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)

					classWriter.toByteArray()
				} else {
					bytes
				}

				remappedEntries[mappedFullName] = remappedBytes
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