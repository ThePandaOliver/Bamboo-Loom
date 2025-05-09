package dev.pandasystems.bambooloom.mapping

object TinyMappingsSerializer {
	fun deserialize(content: String): TinyMappings {
		try {
			require(content.isNotEmpty()) { "Content is empty" }
			val iterator = content.lines().iterator()

			val headerValues = iterator.next().split("\t")
			val format = headerValues[0]
			require(format == "tiny") { "Unsupported format: $format" }
			val majorVersion = headerValues[1].toInt()
			require(majorVersion == 2) { "Unsupported major version: $majorVersion" }
			val minorVersion = headerValues[2].toInt()
			require(minorVersion == 0) { "Unsupported minor version: $minorVersion" }
			val namespaces = headerValues.subList(3, headerValues.size)
			require(namespaces.size >= 2) { "Requires a minimum of two namespaces, got ${namespaces.size}" }

			// class index -> namespace -> name
			val classNames = mutableListOf<Map<String, String>>()

			// owner class index -> field index -> namespace -> name
			val fieldNames = mutableListOf<MutableList<Map<String, String>>>()
			// owner class index -> field index -> descriptor
			val fieldDescriptors = mutableListOf<MutableList<String>>()

			// owner class index -> method index -> namespace -> name
			val methodNames = mutableListOf<MutableList<Map<String, String>>>()
			// owner class index -> method index -> descriptor
			val methodDescriptors = mutableListOf<MutableList<String>>()

			while (iterator.hasNext()) {
				val line = iterator.next()

				when {
					line.startsWith("c\t") -> {
						val names = line.substring("c\t".length).split("\t")
						classNames += names.withIndex().filter { it.value.isNotEmpty() }.associate { (index, name) -> namespaces[index] to name }

						// Creating entries for this class index
						fieldNames += mutableListOf<Map<String, String>>()
						fieldDescriptors += mutableListOf<String>()
						methodNames += mutableListOf<Map<String, String>>()
						methodDescriptors += mutableListOf<String>()
					}
					classNames.isEmpty() -> continue

					line.startsWith("\tf\t") -> {
						val classIndex = classNames.size - 1

						val values = line.substring("\tf\t".length).split("\t")
						val descriptor = values.first()
						val names = values.subList(1, values.size)

						fieldNames[classIndex] += names.withIndex().filter { it.value.isNotEmpty() }.associate { (index, name) -> namespaces[index] to name }
						fieldDescriptors[classIndex] += descriptor
					}

					line.startsWith("\tm\t") -> {
						val classIndex = classNames.size - 1

						val values = line.substring("\tm\t".length).split("\t")
						val descriptor = values.first()
						val names = values.subList(1, values.size)

						methodNames[classIndex] += names.withIndex().filter { it.value.isNotEmpty() }.associate { (index, name) -> namespaces[index] to name }
						methodDescriptors[classIndex] += descriptor
					}
				}
			}

			fun remapDescriptor(descriptor: String, toNamespace: String): String {
				val fromNamespace: String = namespaces.first()
				if (fromNamespace == toNamespace) return descriptor

				val result = StringBuilder()
				var i = 0
				while (i < descriptor.length) {
					when (val ch = descriptor[i]) {
						'L' -> {
							// Parse a class type (Lcom/example/MyClass;)
							val semiColonIndex = descriptor.indexOf(';', i)
							val internalName = descriptor.substring(i + 1, semiColonIndex)
							val remappedName = classNames.find { names -> names.any { it.key == fromNamespace && it.value == internalName } }?.get(toNamespace) ?: internalName
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

			val content = classNames.mapIndexed { classIndex, names ->
				val fields = fieldNames[classIndex].mapIndexed { fieldIndex, names ->
					FieldMapping(names.map { (namespace, name) ->
						val descriptor = remapDescriptor(fieldDescriptors[classIndex][fieldIndex], namespace)
						namespace to "$name.$descriptor"
					}.toMap())
				}
				val methods = methodNames[classIndex].mapIndexed { methodIndex, names ->
					MethodMapping(names.map { (namespace, name) ->
						val descriptor = remapDescriptor(methodDescriptors[classIndex][methodIndex], namespace)
						namespace to "$name.$descriptor"
					}.toMap())
				}
				ClassMapping(names, fields, methods)
			}

			return TinyMappings(
				header = TinyHeader(majorVersion, minorVersion, namespaces),
				content = content
			)
		} catch (e: Exception) {
			throw IllegalArgumentException("Failed to deserialize Tiny mappings", e)
		}
	}

	fun serialize(mappings: TinyMappings): String {
		return TODO("Implement serializer")
	}
}