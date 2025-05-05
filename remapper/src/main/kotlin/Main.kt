package dev.pandasystems

import dev.pandasystems.mapping.TinyMappingsSerializer
import dev.pandasystems.mapping.applyMappings
import java.io.File

fun main() {
	val mappings = TinyMappingsSerializer.deserialize(File("remapper/testFiles/mapping.tiny").readText(Charsets.UTF_8))
	mappings.applyMappings("named", "obfuscated", File("remapper/testFiles/simple-test-1.0-SNAPSHOT.jar"), File("remapper/testFiles/simple-test-1.0-REMAPPED.jar"))
}