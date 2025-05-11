package dev.pandasystems.remappertool.testmain

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.pandasystems.remappertool.applyMappings
import dev.pandasystems.remappertool.remappers.TinyMappingsSerializer
import dev.pandasystems.remappertool.remappers.createLayered
import java.nio.file.Paths
import kotlin.io.path.readText

fun main() {
	val path = Paths.get("remapper/testFiles/minecraft")
	val gson = GsonBuilder().setPrettyPrinting().create()

//	val intermediaryMapping = TinyMappingsSerializer.deserialize(path.resolve("intermediary.tiny").readText(Charsets.UTF_8))
//	val yarnMapping = TinyMappingsSerializer.deserialize(path.resolve("yarn.tiny").readText(Charsets.UTF_8))
	val intermediaryMapping = TinyMappingsSerializer.deserialize(path.resolve("intermediary_small.tiny").readText(Charsets.UTF_8))
	val yarnMapping = TinyMappingsSerializer.deserialize(path.resolve("yarn_small.tiny").readText(Charsets.UTF_8))
	val mappings = intermediaryMapping.createLayered(yarnMapping)
	
	val classes = mappings.content
	path.resolve("log/mapping_data.json").toFile().also { it.parentFile.mkdirs() }.writeText(gson.toJson(classes))

	mappings.applyMappings(
		"official",
		"named",
		path.resolve("minecraft.jar").toFile(),
		path.resolve("remapped.jar").toFile()
	)
}