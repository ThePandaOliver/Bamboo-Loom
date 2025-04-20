package dev.pandasystems.bambooloom.remapping

import dev.pandasystems.bambooloom.data.Mapping
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.slf4j.LoggerFactory
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.io.path.createTempFile

class Remapper(private val mapping: Mapping) {
	companion object {
		private val logger = LoggerFactory.getLogger(Remapper::class.java)
	}

	fun remap(jarFile: File) {
		val tempFile = createTempFile(jarFile.parentFile.toPath(), jarFile.nameWithoutExtension).toFile()

		JarOutputStream(tempFile.outputStream()).use { outputStream ->
			JarFile(jarFile).use { jar ->
				for (entry in jar.entries()) {
					val name = entry.name
					val extension = name.substringAfterLast('.', "")
					val pureName = name.removeSuffix(".$extension")

					// If the entry doesn't have a mapping, make a direct copy
					if (!mapping.map.containsKey(pureName)) {
						outputStream.putNextEntry(JarEntry(name))
						outputStream.write(jar.getInputStream(entry).readBytes())
						outputStream.closeEntry()
						continue
					}
					val newPureName = mapping.map[pureName] ?: pureName
					val newName = "$newPureName.$extension"

					logger.info("Remapping $pureName -> $newPureName")

					val classBytes = jar.getInputStream(entry).readBytes()
					val remappedClassBytes = remapClass(classBytes, mapping)

					outputStream.putNextEntry(JarEntry(newName))
					outputStream.write(remappedClassBytes)
					outputStream.closeEntry()
				}
			}
		}

		tempFile.copyTo(jarFile, true)
		tempFile.delete()
	}

	private fun remapClass(classBytes: ByteArray, mapping: Mapping): ByteArray {
		val classReader = ClassReader(classBytes)
		val classWriter = ClassWriter(0)

		val remapper = object : org.objectweb.asm.commons.Remapper() {
			override fun map(internalName: String): String {
				// Convert internal names (e.g., "com/example/MyClass")
				return mapping.map[internalName] ?: internalName
			}
		}

		val classVisitor = ClassRemapper(classWriter, remapper)
		classReader.accept(classVisitor, 0)

		return classWriter.toByteArray()
	}
}