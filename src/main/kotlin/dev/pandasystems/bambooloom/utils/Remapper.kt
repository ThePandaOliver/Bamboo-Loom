package dev.pandasystems.bambooloom.utils

import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class Remapper(
	project: Project,
	mappingFile: File,
	targetJar: File,
	val outputFile: File = targetJar.parentFile.resolve("${targetJar.nameWithoutExtension}-mapped.jar"),
) {
	companion object {
		fun intermediary(project: Project, version: String, targetJar: File): Remapper {
			val mappingFile = LoomPaths.versionMappingsDir(project, version).resolve("intermediary-mapping.tiny")

			// Download and extract the mappings from the jar file
			File.createTempFile("mappings-", ".jar").apply {
				deleteOnExit()
				downloadFrom(URI("https://maven.fabricmc.net/net/fabricmc/intermediary/$version/intermediary-$version-v2.jar").toURL(), true)

				JarFile(this).use { jarFile ->
					jarFile.getJarEntry("mappings/mappings.tiny")?.let { entry ->
						jarFile.getInputStream(entry).use { inputStream ->
							if (!mappingFile.parentFile.exists())
								mappingFile.parentFile.mkdirs()

							mappingFile.outputStream().use { outputStream ->
								outputStream.write(inputStream.readBytes())
							}
						}
					}
				}
			}

			return Remapper(project, mappingFile, targetJar)
		}
	}

	init {
		val mapping = TinyMapping(mappingFile)

		JarFile(targetJar).use { jar ->
			JarOutputStream(FileOutputStream(outputFile)).use { output ->
				val entries = jar.entries()

				while (entries.hasMoreElements()) {
					val entry = entries.nextElement()
					val inputStream = jar.getInputStream(entry)

					if (entry.name.endsWith(".class")) {
						val transformedBytes = transformClass(inputStream, mapping.classes.associate { it.oldName to it.newName })
						output.putNextEntry(JarEntry(entry.name))
						output.write(transformedBytes)
					} else {
						output.putNextEntry(JarEntry(entry.name))
						output.write(inputStream.readBytes())
					}
				}
			}
		}
	}

	private fun transformClass(inputStream: InputStream, remappings: Map<String, String>): ByteArray {
		val classReader = ClassReader(inputStream)
		val classWriter = ClassWriter(0)
		val remapper = SimpleRemapper(remappings)
		val visitor = ClassRemapper(classWriter, remapper)
		classReader.accept(visitor, 0)

		return classWriter.toByteArray()
	}
}