package dev.pandasystems.bambooloom.data

import org.slf4j.LoggerFactory

class Mapping(
	val classes: Map<String, Class>,
) {
	companion object {
		fun parseOfficial(data: String): Mapping {
			val classes = mutableMapOf<String, Class>()
			var currentClass: Class? = null
			val currentClassFields = mutableMapOf<String, String>()
			val currentClassMethods = mutableMapOf<String, String>()

			val logger = LoggerFactory.getLogger(Mapping::class.java)
			logger.info("Parsing mapping with official format...")

			data.lineSequence().forEach { line ->
				val trimmedLine = line.trim()

				// Skip empty lines and comments that don't contain metadata
				if (trimmedLine.isEmpty() || (trimmedLine.startsWith("#") && !trimmedLine.contains("{"))) {
					return@forEach
				}

				when {
					// Class mapping line (e.g., "com.mojang.blaze3d.Blaze3D -> fib:")
					trimmedLine.contains("->") && trimmedLine.endsWith(":") -> {
						// Save previous class if exists
						currentClass?.let { clazz ->
							classes[clazz.from] = clazz
						}

						// Parse new class
						val (to, from) = trimmedLine.split("->").map { it.trim().replace('.', '/') }
						currentClassFields.clear()
						currentClassMethods.clear()
						currentClass = Class(
							from = from.removeSuffix(":"),
							to = to,
							fields = mutableMapOf(),
							methods = mutableMapOf()
						)
					}

					// Method mapping (e.g., "9:10:void youJustLostTheGame() -> a")
					trimmedLine.contains("->") && !trimmedLine.endsWith(":") -> {
						val parts = trimmedLine.split("->").map { it.trim() }
						val entryName = parts[1]

						// Parse method signature
						val methodParts = parts[0].split(":")
						val lastPart = methodParts.last().substringAfterLast(" ")

						if (lastPart.contains("(") || lastPart.contains(")")) {
							// This is a method
							val mappedName = lastPart.substring(lastPart.lastIndexOf(" ") + 1, lastPart.indexOf("("))
							currentClassMethods[entryName] = mappedName
							currentClass?.let {
								(it.methods as MutableMap)[entryName] = mappedName
							}
						} else {
							// This is a field
							val mappedName = lastPart
							currentClassFields[entryName] = mappedName
							currentClass?.let {
								(it.fields as MutableMap)[entryName] = mappedName
							}
						}
					}
				}
			}

			// Remember to add the last class
			currentClass?.let { clazz ->
				classes[clazz.from] = clazz
			}

			return Mapping(classes)
		}
	}

	operator fun get(name: String): Class? = classes[name]

	class Class(
		val from: String,
		val to: String,

		val fields: Map<String, String>,
		val methods: Map<String, String>,
	) {
		fun getFieldName(name: String, descriptor: String = ""): String? = fields[name + descriptor]
		fun getMethodName(name: String, descriptor: String = ""): String? = methods[name + descriptor]
	}
}
