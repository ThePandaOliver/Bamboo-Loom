package dev.pandasystems.remappertool

import dev.pandasystems.remappertool.remappers.TinyMappingsSerializer
import java.io.File

fun main() {
	val mappings = TinyMappingsSerializer.deserialize(File("remapper/testFiles/mapping.tiny").readText(Charsets.UTF_8))
	mappings.applyMappings("named", "obfuscated", File("remapper/testFiles/simple-test-1.0-SNAPSHOT.jar"), File("remapper/testFiles/simple-test-1.0-REMAPPED.jar"))
}