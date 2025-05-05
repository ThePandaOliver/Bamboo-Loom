package dev.pandasystems.mapping

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

fun TinyMappings.applyMappings(from: String, to: String, input: File, output: File = input) {
	val remappedEntries = ConcurrentHashMap<String, ByteArray>()
	val remapper = TinyMappingsRemapper(this, from, to)

	JarFile(input).use { jarFile ->
		val chunks = jarFile.entries().asSequence().chunked(1000)
		chunks.forEach { entries ->
			for (entry in entries.parallelStream()) {
				val unmappedFullName = entry.name

				// TODO: make it recreate the manifest, instead of removing it
				if (unmappedFullName.startsWith("META-INF/") && (
							unmappedFullName.endsWith(".SF") ||
									unmappedFullName.endsWith(".RSA") ||
									unmappedFullName.endsWith(".DSA") ||
									unmappedFullName.equals("META-INF/MANIFEST.MF", true)
							)
				) {
					continue
				}

				val extension = "." + unmappedFullName.substringAfterLast('.', "")
				val unmappedName = unmappedFullName.removeSuffix(extension)

				val mappedName = remapper.map(unmappedName)
				val mappedFullName = "$mappedName$extension"

				val bytes = jarFile.getInputStream(entry).readBytes()
				remappedEntries[mappedFullName] = if (extension == ".class" && mappedName != unmappedName) {
					try {
						val classReader = ClassReader(bytes)
						val classWriter = ClassWriter(classReader, 0)

						val classVisitor = ClassRemapper(classWriter, remapper)
						classReader.accept(classVisitor, 0)

						classWriter.toByteArray()
					} catch (e: Exception) {
						LoggerFactory.getLogger("remapper").error("Failed to remap class $unmappedFullName -> $mappedFullName", e)
						bytes
					}
				} else {
					bytes
				}
			}
		}
	}

	JarOutputStream(output.outputStream()).use { outputStream ->
		remappedEntries.forEach { (name, bytes) ->
			outputStream.putNextEntry(JarEntry(name))
			outputStream.write(bytes)
			outputStream.closeEntry()
		}
	}
}

class TinyMappingsRemapper(
	mappings: TinyMappings,
	fromNamespace: String,
	toNamespace: String
) : Remapper() {
	private val classes = mappings.content.associate { it.getName(fromNamespace) to it.getName(toNamespace) }
	private val fields = mappings.content.map { classMapping ->
		val className = classMapping.getName(fromNamespace)
		classMapping.fields.map { "$className.${it.getName(fromNamespace)}" to it.getName(toNamespace).split(".").first() }
	}.flatten().toMap()
	private val methods = mappings.content.map { classMapping ->
		val className = classMapping.getName(fromNamespace)
		classMapping.methods.map { "$className.${it.getName(fromNamespace)}" to it.getName(toNamespace).split(".").first() }
	}.flatten().toMap()

	override fun map(internalName: String): String {
		return classes[internalName] ?: internalName
	}

	override fun mapFieldName(owner: String, name: String, descriptor: String): String {
		return fields["$owner.$name.$descriptor"] ?: name
	}

	override fun mapMethodName(owner: String, name: String, descriptor: String): String {
		return methods["$owner.$name.$descriptor"] ?: name
	}
}