package dev.pandasystems.bambooloom.remapping

import org.gradle.internal.impldep.org.junit.experimental.ParallelComputer.classes
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
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.sequences.chunked
import kotlin.sequences.forEach

fun remapJarWithTinyMapping(mapping: TinyMapping, fromNamespace: String, toNamespace: String, inputFile: File, outputFile: File = inputFile) {
	val remappedEntries = ConcurrentHashMap<String, ByteArray>()
	val tinyRemapper = TinyRemapper(mapping, fromNamespace, toNamespace)

	JarFile(inputFile).use { jar ->
		val chunks = jar.entries().asSequence().chunked(1000)
		chunks.forEach { entries ->
			for (entry in entries.parallelStream()) {
				val unmappedFullName = entry.name

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

				val mappedName = tinyRemapper.map(unmappedName) ?: unmappedName
				val mappedFullName = "$mappedName$extension"

				val bytes = jar.getInputStream(entry).readBytes()
				remappedEntries[mappedFullName] = tinyRemapper.map(unmappedName)?.let {
					try {
						val classReader = ClassReader(bytes)
						val classWriter = ClassWriter(classReader, 0)

						val classVisitor = ClassRemapper(classWriter, tinyRemapper)
						classReader.accept(classVisitor, 0)

						classWriter.toByteArray()
					} catch (e: Exception) {
						LoggerFactory.getLogger("remapper").error("Failed to remap class $unmappedFullName -> $mappedFullName", e)
						bytes
					}
				} ?: bytes
			}
		}
	}

	JarOutputStream(outputFile.outputStream()).use { outputStream ->
		for ((newName, bytes) in remappedEntries) {
			outputStream.putNextEntry(JarEntry(newName))
			outputStream.write(bytes)
			outputStream.closeEntry()
		}
	}
}

class TinyRemapper(mapping: TinyMapping, fromNamespace: String, toNamespace: String) : Remapper() {
	// <unmapped name> -> <mapped name>
	val classes = mutableMapOf<String, String>()

	// <mapped owner class>.<unmapped name>$<unmapped descriptor> -> <mapped name>
	val fields = mutableMapOf<String, String>()

	// <mapped owner class>.<unmapped name>$<unmapped descriptor> -> <mapped name>
	val methods = mutableMapOf<String, String>()

	init {
		val fromNamespaceIndex = mapping.namespaces.withIndex().find { it.value.name == fromNamespace }?.index ?: throw IllegalArgumentException("Unknown namespace: $fromNamespace")
		val toNamespaceIndex = mapping.namespaces.withIndex().find { it.value.name == toNamespace }?.index ?: throw IllegalArgumentException("Unknown namespace: $toNamespace")

		val fromClasses = mapping.classes[fromNamespaceIndex]
		val toClasses = mapping.classes[toNamespaceIndex]

		fromClasses.forEachIndexed { index, unmappedClass ->
			if (unmappedClass == null) return@forEachIndexed
			val mappedClass = toClasses[index]

			classes[unmappedClass.name] = mappedClass?.name ?: unmappedClass.name

			unmappedClass.fields.forEachIndexed { index, unmappedField ->
				if (unmappedField == null) return@forEachIndexed
				val mappedField = mappedClass?.fields?.get(index)

				fields["${unmappedClass.name}.${unmappedField.name}$${unmappedField.descriptor}"] = mappedField?.name ?: unmappedField.name
			}

			unmappedClass.methods.forEachIndexed { index, unmappedMethod ->
				if (unmappedMethod == null) return@forEachIndexed
				val mappedMethod = mappedClass?.methods?.get(index)

				methods["${unmappedClass.name}.${unmappedMethod.name}$${unmappedMethod.descriptor}"] = mappedMethod?.name ?: unmappedMethod.name
			}
		}
	}

	override fun map(internalName: String): String? {
		return classes[internalName] ?: internalName
	}

	override fun mapFieldName(owner: String, name: String, descriptor: String): String? {
		return fields["$owner.$name$$descriptor"] ?: name
	}

	override fun mapMethodName(owner: String, name: String, descriptor: String): String? {
		return methods["$owner.$name$$descriptor"] ?: name
	}
}