package dev.pandasystems.mapping

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.slf4j.LoggerFactory
import remappers.TinyClassRemapper
import remappers.TinyMappingsRemapper
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

fun TinyMappings.applyMappings(from: String, to: String, input: File, output: File = input) {
	val remapper = TinyMappingsRemapper(this, from, to)

	// Read all entries and their contents before closing the jar file
	val entries = mutableMapOf<String, ByteArray>()
	JarFile(input).use { jarFile ->
		for (entry in jarFile.entries()) {
			jarFile.getInputStream(entry).use { inputStream ->
				entries[entry.name] = inputStream.readBytes()
			}
		}
	}

	// Process the entries
	entries.toMap().forEach { (name, bytes) ->
		// TODO: Temporary Skip signing-related files until a function for renaming the signatures for each entry is implemented
		if (name.startsWith("META-INF/") && (
					name.endsWith(".SF") ||
							name.endsWith(".RSA") ||
							name.endsWith(".DSA") ||
							name.equals("META-INF/MANIFEST.MF", true)
					)
		) {
			entries.remove(name)
			return@forEach
		}

		if (name.endsWith(".class")) {
			try {
				val classReader = ClassReader(bytes)
				val classWriter = ClassWriter(classReader, 0)
				val classVisitor = TinyClassRemapper(classWriter, remapper)
				classReader.accept(classVisitor, 0)
				entries[name] = classWriter.toByteArray()
			} catch (e: Exception) {
				LoggerFactory.getLogger("remapper").error("Failed to remap class $name", e)
			}
		}
	}

	// Write the processed entries to the output jar
	JarOutputStream(output.outputStream()).use { outputStream ->
		entries.forEach { (name, bytes) ->
			outputStream.putNextEntry(JarEntry(name))
			outputStream.write(bytes)
			outputStream.closeEntry()
		}
	}
}