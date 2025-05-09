package dev.pandasystems.remappertool

import dev.pandasystems.remappertool.data.TinyMappings
import dev.pandasystems.remappertool.remappers.visitors.HierarchyAwareClassVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.slf4j.LoggerFactory
import dev.pandasystems.remappertool.remappers.visitors.TinyClassRemapper
import dev.pandasystems.remappertool.remappers.TinyMappingsRemapper
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.collections.iterator

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

	val haClassVisitor = HierarchyAwareClassVisitor()

	// Process the entries
	entries.toMap().forEach { (name, bytes) ->
		// Remove signing-related files
		if (name.startsWith("META-INF/") && (
					name.endsWith(".SF") ||
							name.endsWith(".RSA") ||
							name.endsWith(".DSA")
					)
		) {
			entries.remove(name)
			return@forEach
		}

		// Remove signing-related entries from the manifest
		if (name == "META-INF/MANIFEST.MF") {
			val manifestContent = bytes.toString(Charsets.UTF_8)
			val cleanedManifest = manifestContent.lines()
				.filter { line ->
					!line.startsWith("Name: ") &&
							!line.startsWith("SHA-384-Digest: ") &&
							!line.startsWith(" ") &&  // Remove continuation lines
							line.isNotBlank()
				}
				.joinToString("\n")
			entries[name] = cleanedManifest.toByteArray()
		}

		if (name.endsWith(".class")) {
			try {
				val classReader = ClassReader(bytes)
				val classWriter = ClassWriter(classReader, 0)

				classReader.accept(haClassVisitor, 0)
				val classRemapper = TinyClassRemapper(haClassVisitor, classWriter, remapper)
				classReader.accept(classRemapper, 0)

				entries[name] = classWriter.toByteArray()
			} catch (e: Exception) {
				LoggerFactory.getLogger("remapper").error("Failed to remap class $name", e)
			}
		}
	}

	// Write the processed entries to the output jar
	JarOutputStream(output.outputStream()).use { outputStream ->
		entries.forEach { (name, bytes) ->
			val mappedName = if (name.endsWith(".class")) {
				remapper.map(name.substringBeforeLast(".")) + ".class"
			} else name

			outputStream.putNextEntry(JarEntry(mappedName))
			outputStream.write(bytes)
			outputStream.closeEntry()
		}
	}
}