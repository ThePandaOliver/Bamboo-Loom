package dev.pandasystems.bambooloom.remapping

class TinyMapping(
	val namespaces: List<String>,
	val classMappings: List<TinyClassMapping>
)

class TinyClassMapping(val namespace: String) {
	val names = mutableListOf<String?>()

	val fieldsMappings = TinyFieldMapping(namespace)
	val methodMappings = TinyMethodMapping(namespace)
}

class TinyFieldMapping(val namespace: String) {
	val names = mutableListOf<String?>()
}

class TinyMethodMapping(val namespace: String) {
	val names = mutableListOf<String?>()
	val parameters = mutableListOf<String?>()
}

fun deserializeTinyMapping(data: String): TinyMapping {
	val lineIterator = data.lines().asIterable().iterator()

	val header = lineIterator.next().split("\t")
	val type = header[0]
	require(type == "tiny") { "$type is not a tiny mapping file" }
	val version = header[1]
	require(version == "2") { "Unsupported tiny version: $version" }

	val namespaces = header.drop(3)
	val classes = namespaces.map { TinyClassMapping(it) }

	while (lineIterator.hasNext()) {
		val line = lineIterator.next()

		when {
			line.startsWith("c\t") -> {
				val names = line.split("\t").drop(1)
				for ((index, name) in names.withIndex()) {
					val clazz = classes[index]
					clazz.names.add(name)
				}
			}
		}
	}

	return TinyMapping(namespaces, classes)
}