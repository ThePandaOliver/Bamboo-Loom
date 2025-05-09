package dev.pandasystems.remappertool.testmain

import dev.pandasystems.remappertool.applyMappings
import dev.pandasystems.remappertool.remappers.TinyMappingsSerializer
import dev.pandasystems.remappertool.remappers.createLayered
import java.nio.file.Paths
import kotlin.io.path.readText

fun main() {
	val path = Paths.get("remapper/testFiles/minecraft")

	val intermediaryMapping = TinyMappingsSerializer.deserialize(path.resolve("intermediary.tiny").readText(Charsets.UTF_8))
	val yarnMapping = TinyMappingsSerializer.deserialize(path.resolve("yarn.tiny").readText(Charsets.UTF_8))
	val mappings = intermediaryMapping.createLayered(yarnMapping)

	mappings.applyMappings(
		"official",
		"named",
		path.resolve("minecraft.jar").toFile(),
		path.resolve("remapped.jar").toFile()
	)
}