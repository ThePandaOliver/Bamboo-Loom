package dev.pandasystems.bambooloom.remapping

import java.io.File
import java.util.jar.JarFile

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

	while (lineIterator.hasNext()) {
		val line = lineIterator.next()

		when {
			line.startsWith("c\t") -> {
				val names = line.replaceFirst("c\t", "").split("\t")
				for ((index, tinyClasses) in classes.withIndex()) {
					val namespace = namespaces[index]
					tinyClasses += names.getOrNull(index + 1)?.let { TinyClass(namespace, it) }
				}
			}
			classes.isEmpty() -> continue

			line.startsWith("\tf\t") -> {
				val entries = line.replaceFirst("\tf\t", "").split("\t")
				println(entries)
				val names = entries.chunked(2).map { it[0] to it[1] }
				for ((index, tinyClasses) in classes.withIndex()) {
					val namespace = namespaces[index]
					val clazz = tinyClasses.last() ?: continue
					names.getOrNull(index)?.let { (descriptor, name) ->
						clazz.fields as MutableList += TinyField(namespace, name, descriptor)
					} ?: (clazz.fields as MutableList + null)
				}
			}

			line.startsWith("\tm\t") -> {
				val entries = line.replaceFirst("\tm\t", "").split("\t")
				val names = entries.chunked(2).map { it[0] to it[1] }
				for ((index, tinyClasses) in classes.withIndex()) {
					val namespace = namespaces[index]
					val clazz = tinyClasses.last() ?: continue
					names.getOrNull(index)?.let { (descriptor, name) ->
						clazz.methods as MutableList += TinyMethod(namespace, name, descriptor)
					} ?: (clazz.methods as MutableList + null)
				}
			}
		}
	}

	return TinyMapping(namespaces, classes)
}

fun deserializeTinyV2Jar(jarFile: File): TinyMapping {
	return JarFile(jarFile).use { jar ->
		val bytes = jar.getInputStream(jar.getJarEntry("mappings/mappings.tiny")).readBytes()
		deserializeTinyV2(bytes.toString(Charsets.UTF_8))
	}
}