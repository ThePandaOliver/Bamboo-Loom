package dev.pandasystems.bambooloom.data

data class Mapping(
	val classes: List<Class>,
) {
	companion object {
		fun parseOfficial(data: String): Mapping {
			val lines = data.lines()

			val classes = mutableListOf<Class>()
			var currentClass: Class? = null

			for (line in lines) {
				val trimmedLine = line.trim()

				when {
					// Check for a new class mapping
					trimmedLine.matches(Regex("\\S+ -> \\S+:")) -> {
						// If we were parsing a previous class, add it to the list
						if (currentClass != null) {
							classes.add(currentClass)
						}

						// Parse class names (obfuscated -> deobfuscated)
						val (deobfuscatedName, obfuscatedName) = trimmedLine
							.removeSuffix(":")
							.split(" -> ")
							.map { it.trim().replace('.', '/') }

						currentClass = Class(
							from = obfuscatedName,
							to = deobfuscatedName,
							fields = mutableListOf(),
							methods = mutableListOf()
						)
					}

					// Check for field mappings
					trimmedLine.matches(Regex("\\s*\\S+ \\S+ -> \\S+")) -> {
						val parts = trimmedLine.split(" -> ")
						val descriptorAndField = parts[0].trim().split(" ")
						val descriptor = descriptorAndField[0]
						val deobfuscatedName = descriptorAndField[1]
						val obfuscatedName = parts[1].trim()

						if (currentClass != null) {
							(currentClass.fields as MutableList).add(
								Field(
									from = obfuscatedName,
									to = deobfuscatedName,
									descriptor = descriptor
								)
							)
						}
					}

					// Check for method mappings
					trimmedLine.matches(Regex("\\s*\\d+:\\d+:\\S+ \\S+\\(.*\\) -> \\S+")) -> {
						val parts = trimmedLine.split(" -> ")
						val signatureAndMethod = parts[0].trim().split(" ", limit = 3)
						val descriptor = signatureAndMethod[2]
						val deobfuscatedName = descriptor.substringBefore("(")
						val obfuscatedName = parts[1].trim()

						if (currentClass != null) {
							(currentClass.methods as MutableList).add(
								Method(
									from = obfuscatedName,
									to = deobfuscatedName,
									descriptor = descriptor
								)
							)
						}
					}
				}
			}

			// Add the last parsed class if any
			if (currentClass != null) {
				classes.add(currentClass)
			}

			return Mapping(classes = classes)
		}
	}

	data class Class(
		val from: String,
		val to: String,

		val fields: List<Field>,
		val methods: List<Method>,
	) {
		val fieldMap: Map<String, String> = fields.associate { it.from to it.to }
		val methodMap: Map<String, String> = methods.associate { it.from to it.to }
	}

	data class Field(
		val from: String,
		val to: String,
		val descriptor: String,
	)

	data class Method(
		val from: String,
		val to: String,
		val descriptor: String,
	)

	val map: Map<String, String> = classes.associate { it.from to it.to }
}
