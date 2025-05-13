package dev.pandasystems.remappertool

import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.MappingUtil
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.tree.MappingTree
import net.fabricmc.mappingio.tree.MemoryMappingTree
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import net.fabricmc.tinyremapper.TinyUtils
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

// * Old iteration
// * It worked but it doesn't apply the intermediary mappings to the classes, fields, methods and parameters if their named names are missing.
// BUG: Doesn't remap parameters, the new iteration does though.
/*fun main() {
	val intermediaryMappingFile: Path = Paths.get("remapper/testFiles/minecraft/intermediary.tiny")
	val yarnMappingFile: Path = Paths.get("remapper/testFiles/minecraft/yarn.tiny")
	val inputFile: Path = Paths.get("remapper/testFiles/minecraft/minecraft.jar")
	val outputFile: Path = Paths.get("remapper/testFiles/minecraft/minecraft-remapper.jar")
	outputFile.deleteIfExists()

	val mappingTree = MemoryMappingTree()
	MappingReader.read(intermediaryMappingFile, MappingFormat.TINY_2_FILE, mappingTree)
	MappingReader.read(yarnMappingFile, MappingFormat.TINY_2_FILE,mappingTree)

	val tinyRemapper = TinyRemapper.newRemapper()
		.withMappings(TinyUtils.createMappingProvider(mappingTree, "official", "named"))
		.build()
	tinyRemapper.readInputs(inputFile)

	OutputConsumerPath.Builder(outputFile).build().use { outputConsumer ->
		outputConsumer.addNonClassFiles(inputFile)
		tinyRemapper.apply(outputConsumer)
	}
	tinyRemapper.finish()
}*/

// * New iteration 
// * I'm not too happy with this since it remaps the jar 2 times.
fun main() {
	val intermediaryMappingFile: Path = Paths.get("remapper/testFiles/minecraft/intermediary.tiny")
	val yarnMappingFile: Path = Paths.get("remapper/testFiles/minecraft/yarn.tiny")
	val outputPath: Path = Paths.get("remapper/testFiles/minecraft/outputs/")
	if (outputPath.exists())
		outputPath.toFile().deleteRecursively()
	
	val intermediaryMappingProvider = TinyUtils.createTinyMappingProvider(intermediaryMappingFile, "official", "intermediary")
	val yarnMappingProvider = TinyUtils.createTinyMappingProvider(yarnMappingFile, "intermediary", "named")

	var inputFile: Path = Paths.get("remapper/testFiles/minecraft/minecraft.jar")
	for ((index, provider) in listOf(intermediaryMappingProvider, yarnMappingProvider).withIndex()) {
		val outputFile = outputPath.resolve("minecraft-remapped-$index.jar")
		val tinyRemapper = TinyRemapper.newRemapper()
			.withMappings(provider)
			.build()
		tinyRemapper.readInputs(inputFile)

		OutputConsumerPath.Builder(outputFile).build().use { outputConsumer ->
			outputConsumer.addNonClassFiles(inputFile)
			tinyRemapper.apply(outputConsumer)
		}
		tinyRemapper.finish()
		inputFile = outputFile
	}
}