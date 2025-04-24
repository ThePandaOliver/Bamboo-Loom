package dev.pandasystems.bambooloom.remapping

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.io.path.createTempFile

class LoomRemapperV2(
	/**
	 * Key = Unmapped class name
	 * Value = Mapped class name
	 */
	val classes: Map<String, String>,

	/**
	 * Key = Unmapped field name ($owner.$name:$descriptor)
	 * Value = Mapped field name
	 */
	val fields: Map<String, String>,

	/**
	 * Key = Unmapped method name ($owner.$name:$descriptor)
	 * Value = Mapped method name
	 */
	val methods: Map<String, String>,
) : Remapper() {
	override fun map(internalName: String): String {
		return classes[internalName] ?: internalName
	}

	override fun mapFieldName(owner: String, name: String, descriptor: String): String {
		return fields["$owner.$name"] ?: name
	}

	override fun mapMethodName(owner: String, name: String, descriptor: String): String {
		return methods["$owner.$name"] ?: name
	}

	companion object {
		/**
		 * Parses Tiny format mapping data and converts it into a `LoomRemapperV2` object.
		 *
		 * @param data The Tiny format mapping data as a string. Each line represents a mapping entry,
		 *             including class, field, or method mappings, separated by tab characters.
		 *             Supports the following prefixes:
		 *             - `c`: Represents a class mapping.
		 *             - `f`: Represents a field mapping.
		 *             - `m`: Represents a method mapping.
		 *             Classes, fields, and methods are mapped with their respective details specified.
		 *
		 * @return A `LoomRemapperV2` object containing parsed mappings for classes, fields, and methods.
		 *         The mappings are stored in maps with unmapped names as keys and mapped names as values.
		 */
		fun parseTiny(data: String): LoomRemapperV2 {
			val classes = mutableMapOf<String, String>()
			val fields = mutableMapOf<String, String>()
			val methods = mutableMapOf<String, String>()
			var lastClass: String? = null

			data.lines().forEach { line ->
				val parts = line.trim().split("\t".toRegex())
				val type = parts[0]

				when {
					type == "c" -> {
						val unmappedName = parts[1]
						val mappedName = parts[2]
						classes[unmappedName] = mappedName
						lastClass = unmappedName
					}

					type == "f" && lastClass != null -> {
						val descriptor = parts[1]
						val unmappedName = parts[2]
						val mappedName = parts[3]
						fields["$lastClass.$unmappedName"] = mappedName
					}

					type == "m" && lastClass != null -> {
						val descriptor = parts[1]
						val unmappedName = parts[2]
						val mappedName = parts[3]
						methods["$lastClass.$unmappedName"] = mappedName
					}
				}
			}

			return LoomRemapperV2(classes, fields, methods)
		}

		/**
		 * Fetches the tiny mappings located in the given file, and parses them into a `LoomRemapperV2` object.
		 * @see parseTiny
		 */
		fun parseTinyFile(file: File): LoomRemapperV2 {
			return parseTiny(file.readText())
		}

		/**
		 * Fetches the tiny mappings located in the given jar, and parses them into a `LoomRemapperV2` object.
		 * @see parseTiny
		 */
		fun parseTinyJar(jar: JarFile): LoomRemapperV2 {
			return parseTiny(jar.getInputStream(jar.getJarEntry("mappings/mappings.tiny")).readBytes().decodeToString())
		}

		/**
		 * Fetches the tiny mappings located in the given jar, and parses them into a `LoomRemapperV2` object.
		 * @see parseTiny
		 */
		fun parseTinyJar(jar: File): LoomRemapperV2 {
			return parseTinyJar(JarFile(jar))
		}
	}

	fun remap(jarFile: JarFile, outputFile: File) {
		// Use ConcurrentHashMap to store remapped entries
		val remappedEntries = ConcurrentHashMap<String, ByteArray>()

		jarFile.use { jar ->
			for (entry in jar.entries()) {
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

				val mappedName = classes[unmappedName] ?: unmappedName
				val mappedFullName = "$mappedName$extension"

				val bytes = jar.getInputStream(entry).readBytes()
				remappedEntries[mappedFullName] = if (classes.containsKey(unmappedName)) {
					val classReader = ClassReader(bytes)
					val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)

					val classVisitor = ClassRemapper(classWriter, this)
					classReader.accept(classVisitor, ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)

					classWriter.toByteArray()
				} else {
					bytes
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
}