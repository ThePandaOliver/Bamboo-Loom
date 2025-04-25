package dev.pandasystems.bambooloom.remapping

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import org.slf4j.LoggerFactory
import java.io.File
import java.util.SortedMap
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

class LoomRemapper(
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
		return fields["$owner.$name.$descriptor"] ?: name
	}

	override fun mapMethodName(owner: String, name: String, descriptor: String): String {
		return methods["$owner.$name.$descriptor"] ?: name
	}

	companion object {
		/**
		 * Parses Tiny format mapping data and converts it into a [LoomRemapper] object.
		 *
		 * @param data The Tiny format mapping data as a string. Each line represents a mapping entry,
		 *             including class, field, or method mappings, separated by tab characters.
		 *             Supports the following prefixes:
		 *             - `c`: Represents a class mapping.
		 *             - `f`: Represents a field mapping.
		 *             - `m`: Represents a method mapping.
		 *             Classes, fields, and methods are mapped with their respective details specified.
		 *
		 * @return A [LoomRemapper] object containing parsed mappings for classes, fields, and methods.
		 *         The mappings are stored in maps with unmapped names as keys and mapped names as values.
		 */
		fun parseTiny(data: String): LoomRemapper {
			val classes = mutableMapOf<String, String>()
			val fields = mutableMapOf<String, String>()
			val methods = mutableMapOf<String, String>()
			var lastClass: String? = null

			val chunks = data.lineSequence().chunked(1000)
			chunks.forEach { lines ->
				lines.forEach { line ->
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
							fields["$lastClass.$unmappedName.$descriptor"] = mappedName
						}

						type == "m" && lastClass != null -> {
							val descriptor = parts[1]
							val unmappedName = parts[2]
							val mappedName = parts[3]
							methods["$lastClass.$unmappedName.$descriptor"] = mappedName
						}
					}
				}
			}

			return LoomRemapper(classes, fields, methods)
		}

		/**
		 * Fetches the tiny mappings located in the given file, and parses them into a [LoomRemapper] object.
		 * @see parseTiny
		 */
		fun parseTinyFile(file: File): LoomRemapper {
			return parseTiny(file.readText())
		}

		/**
		 * Fetches the tiny mappings located in the given jar, and parses them into a [LoomRemapper] object.
		 * @see parseTiny
		 */
		fun parseTinyJar(jar: JarFile): LoomRemapper {
			return parseTiny(jar.getInputStream(jar.getJarEntry("mappings/mappings.tiny")).readBytes().decodeToString())
		}

		/**
		 * Fetches the tiny mappings located in the given jar, and parses them into a [LoomRemapper] object.
		 * @see parseTiny
		 */
		fun parseTinyJar(jar: File): LoomRemapper {
			return parseTinyJar(JarFile(jar))
		}

		/**
		 * Parses the provided mapping data in string format and constructs a [LoomRemapper] object
		 * containing the mappings for classes, fields, and methods.
		 *
		 * @param data The textual content of the mapping file that specifies class, field, and method remappings.
		 *             This data should follow the expected format for the mappings utility to process it correctly.
		 * @return A [LoomRemapper] instance that contains the parsed class, field, and method mappings.
		 */
		fun parseOfficial(data: String): LoomRemapper {
			fun mapTypeToDescriptor(type: String): String {
				return when (type) {
					"void" -> "V"
					"boolean" -> "Z"
					"byte" -> "B"
					"char" -> "C"
					"double" -> "D"
					"float" -> "F"
					"int" -> "I"
					"long" -> "J"
					"short" -> "S"
					else -> {
						if (type.endsWith("[]")) {
							"[" + mapTypeToDescriptor(type.removeSuffix("[]"))
						} else {
							"L" + type.replace('.', '/') + ";"
						}
					}
				}
			}

			val classes = mutableMapOf<String, String>()
			val fields = mutableMapOf<String, String>()
			val methods = mutableMapOf<String, String>()
			var lastClass: String? = null

			val chunks = data.lineSequence().chunked(1000)
			chunks.forEach { lines ->
				lines.forEach { line ->
					if (line.startsWith("#") || line.isBlank()) {
						return@forEach
					}

					// All the regex patterns
					val classRegex = Regex("^(.+) -> (.+):$")
					val methodRegex = Regex("^ {4}(\\d+:\\d+:)?(.+) (.+)\\((.*)\\) -> (.+)$")
					val fieldRegex = Regex("^ {4}(.+) (.+) -> (.+)$")

					if (classRegex.matches(line)) { // This lets us know it's a class
						val match = classRegex.matchEntire(line)
						val unmappedName = match?.groups?.get(1)?.value?.replace(".", "/") ?: ""
						val mappedName = match?.groups?.get(2)?.value?.replace(".", "/") ?: ""

						classes[unmappedName] = mappedName
						lastClass = unmappedName
					} else if (methodRegex.matches(line)) { // This lets us know it's a method
						val match = methodRegex.matchEntire(line)
						val returnValue = match?.groups?.get(2)?.value?.replace(".", "/") ?: ""
						val unmappedName = match?.groups?.get(3)?.value?.replace(".", "/") ?: ""
						val parametersRaw = match?.groups?.get(4)?.value ?: ""
						val mappedName = match?.groups?.get(5)?.value?.replace(".", "/") ?: ""

						// Generate a descriptor for the method
						val parameters = parametersRaw.split(",").filter { it.isNotBlank() }
							.joinToString("") { mapTypeToDescriptor(it.replace(".", "/")) }
						val returnDescriptor = mapTypeToDescriptor(returnValue)
						val descriptor = "($parameters)$returnDescriptor"

						methods["$lastClass.$unmappedName.$descriptor"] = mappedName
					} else if (fieldRegex.matches(line)) { // This lets us know it's a field
						val match = fieldRegex.matchEntire(line)
						val fieldType = match?.groups?.get(1)?.value?.replace(".", "/") ?: ""
						val unmappedName = match?.groups?.get(2)?.value?.replace(".", "/") ?: ""
						val mappedName = match?.groups?.get(3)?.value?.replace(".", "/") ?: ""

						// Generate a descriptor for the field
						val descriptor = mapTypeToDescriptor(fieldType)

						fields["$lastClass.$unmappedName.$descriptor"] = mappedName
					}
				}
			}

			return LoomRemapper(classes, fields, methods)
		}

		/**
		 * Fetches the official mappings located in the given file, and parses them into a [LoomRemapper] object.
		 * @see parseOfficial
		 */
		fun parseOfficialFile(file: File): LoomRemapper {
			return parseOfficial(file.readText())
		}
	}

	fun remap(jarFile: JarFile, outputFile: File) {
		// Use ConcurrentHashMap to store remapped entries
		val remappedEntries = ConcurrentHashMap<String, ByteArray>()

		jarFile.use { jar ->
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

					val mappedName = classes[unmappedName] ?: unmappedName
					val mappedFullName = "$mappedName$extension"

					val bytes = jar.getInputStream(entry).readBytes()
					remappedEntries[mappedFullName] = if (classes.containsKey(unmappedName)) {
						try {
							val classReader = ClassReader(bytes)
							val classWriter = ClassWriter(classReader, 0)

							val classVisitor = LoomClassRemapper(classWriter, this)
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

		JarOutputStream(outputFile.outputStream()).use { outputStream ->
			for ((newName, bytes) in remappedEntries) {
				outputStream.putNextEntry(JarEntry(newName))
				outputStream.write(bytes)
				outputStream.closeEntry()
			}
		}
	}

	fun remapDescriptor(descriptor: String): String {
		val result = StringBuilder()
		var i = 0
		while (i < descriptor.length) {
			when (val ch = descriptor[i]) {
				'L' -> {
					// Parse a class type (Lcom/example/MyClass;)
					val semiColonIndex = descriptor.indexOf(';', i)
					val internalName = descriptor.substring(i + 1, semiColonIndex)
					val remappedName = map(internalName)
					result.append("L").append(remappedName).append(";")
					i = semiColonIndex
				}
				'[', 'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z', 'V' -> result.append(ch) // Primitive or array
				else -> result.append(ch)
			}
			i++
		}
		return result.toString()
	}

	fun reverse(): LoomRemapper {
		val classes = mutableMapOf<String, String>()
		val fields = mutableMapOf<String, String>()
		val methods = mutableMapOf<String, String>()

		for ((unmapped, mapped) in this.classes) {
			classes[mapped] = unmapped
		}

		for ((unmapped, mapped) in this.fields) {
			val unmappedParts = unmapped.split(".")

			val unmappedClass = unmappedParts[0]
			val unmappedName = unmappedParts[1]
			val unmappedDescriptor = unmappedParts[2]

			val mappedClass = this.classes[unmappedClass] ?: unmappedClass
			val mappedName = mapped
			val mappedDescriptor = this.remapDescriptor(unmappedDescriptor)

			fields["$mappedClass.$mappedName.$mappedDescriptor"] = unmappedName
		}

		for ((unmapped, mapped) in this.methods) {
			val unmappedParts = unmapped.split(".")

			val unmappedClass = unmappedParts[0]
			val unmappedName = unmappedParts[1]
			val unmappedDescriptor = unmappedParts[2]

			val mappedClass = this.classes[unmappedClass] ?: unmappedClass
			val mappedName = mapped
			val mappedDescriptor = this.remapDescriptor(unmappedDescriptor)

			methods["$mappedClass.$mappedName.$mappedDescriptor"] = unmappedName
		}

		return LoomRemapper(classes, fields, methods)
	}

	fun serialize(): String {
		val stringBuilder = StringBuilder()
		stringBuilder.appendLine("tiny\t2\t0\tofficial\tintermediary")

		for ((obfuscatedClass, deobfuscatedClass) in classes) {
			stringBuilder.appendLine("c\t$obfuscatedClass\t$deobfuscatedClass")

			val fields = this.fields.filter { (obfuscatedField, _) -> obfuscatedField.startsWith("$obfuscatedClass.") }
			val methods = this.methods.filter { (obfuscatedMethod, _) -> obfuscatedMethod.startsWith("$obfuscatedClass.") }

			for ((obfuscated, deobfuscated) in fields) {
				val (obfuscatedOwner, obfuscatedName, obfuscatedDescriptor) = obfuscated.split('.')
				stringBuilder.appendLine("\tf\t$obfuscatedDescriptor\t$obfuscatedName\t$deobfuscated")
			}

			for ((obfuscated, deobfuscated) in methods) {
				val (obfuscatedOwner, obfuscatedName, obfuscatedDescriptor) = obfuscated.split('.')
				stringBuilder.appendLine("\tm\t$obfuscatedDescriptor\t$obfuscatedName\t$deobfuscated")
			}
		}

		return stringBuilder.toString()
	}
}