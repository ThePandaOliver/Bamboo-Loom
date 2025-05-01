package dev.pandasystems.bambooloom.remapping

fun deserializeTinyV2(data: String): TinyMapping {
	val lineList = data.lines()
	val lineIterator = lineList.iterator()

	val header = lineIterator.next().split("\t")
	val type = header[0]
	require(type == "tiny") { "Mappings file is not a Tiny V2 file" }
	val version = header[1]
	require(version == "2") { "Unsupported Tiny version: $version" }

	// Namespaces
	val namespaceNames = header.drop(3)
	val sourceNamespace = Namespace(namespaceNames.first(), null)
	val namespaces = listOf(sourceNamespace) + namespaceNames.drop(1).map { Namespace(it, null) }

	// Classes
	val classes: List<MutableList<TinyClass?>> = (0 until namespaces.size).map { mutableListOf() }

	// Regexes
	val classRegex = Regex("^c(\t.){2,}")
	val fieldRegex = Regex("^\tf(\t.\t.){2,}")
	val methodRegex = Regex("^\tm(\t.\t.){2,}")

	while (lineIterator.hasNext()) {
		val line = lineIterator.next()

		when {
			classRegex.matches(line) -> {
				val names = classRegex.matchEntire(line)?.groupValues?.map { it.trim() } ?: continue
				for ((index, tinyClasses) in classes.withIndex()) {
					val namespace = namespaces[index]
					tinyClasses += names.getOrNull(index + 1)?.let { TinyClass(namespace, it) }
				}
			}
			classes.isEmpty() -> continue

			fieldRegex.matches(line) -> {
				val names = fieldRegex.matchEntire(line)?.groupValues?.map { it.trim().split("\t") } ?: continue
				for ((index, tinyClasses) in classes.withIndex()) {
					val namespace = namespaces[index]
					val clazz = tinyClasses.last() ?: continue
					clazz.fields as MutableList += TinyField(namespace, names[index][0], names[index][1])
				}
			}

			methodRegex.matches(line) -> {
				val names = methodRegex.matchEntire(line)?.groupValues?.map { it.trim().split("\t") } ?: continue
				for ((index, tinyClasses) in classes.withIndex()) {
					val namespace = namespaces[index]
					val clazz = tinyClasses.last() ?: continue
					clazz.methods as MutableList += TinyMethod(namespace, names[index][0], names[index][1])
				}
			}
		}
	}

	return TinyMapping(namespaces, classes)
}