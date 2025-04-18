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
						val (obfuscatedName, deobfuscatedName) = trimmedLine
							.removeSuffix(":")
							.split(" -> ")
							.map { it.trim() }

						currentClass = Class(
							obfuscatedName = obfuscatedName,
							deobfuscatedName = deobfuscatedName,
							fields = mutableListOf(),
							methods = mutableListOf()
						)
					}

					// Check for field mappings
					trimmedLine.matches(Regex("\\s*\\S+ \\S+ -> \\S+")) -> {
						val parts = trimmedLine.split(" -> ")
						val descriptorAndField = parts[0].trim().split(" ")
						val descriptor = descriptorAndField[0]
						val obfuscatedName = descriptorAndField[1]
						val deobfuscatedName = parts[1].trim()

						if (currentClass != null) {
							(currentClass.fields as MutableList).add(
								Field(
									obfuscatedName = obfuscatedName,
									deobfuscatedName = deobfuscatedName,
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
						val obfuscatedName = descriptor.substringBefore("(")
						val deobfuscatedName = parts[1].trim()

						if (currentClass != null) {
							(currentClass.methods as MutableList).add(
								Method(
									obfuscatedName = obfuscatedName,
									deobfuscatedName = deobfuscatedName,
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
		val obfuscatedName: String,
		val deobfuscatedName: String,

		val fields: List<Field>,
		val methods: List<Method>,
	)

	data class Field(
		val obfuscatedName: String,
		val deobfuscatedName: String,
		val descriptor: String,
	)

	data class Method(
		val obfuscatedName: String,
		val deobfuscatedName: String,
		val descriptor: String,
	)
}
