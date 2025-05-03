package dev.pandasystems.bambooloom.jobs

import dev.pandasystems.bambooloom.BambooLoomPlugin
import dev.pandasystems.bambooloom.remapping.TinyMapping
import dev.pandasystems.bambooloom.remapping.deserializeTinyV2Jar
import dev.pandasystems.bambooloom.remapping.layerTinyMapping
import dev.pandasystems.bambooloom.remapping.remapJarWithTinyMapping
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import java.util.jar.JarFile

class MappingHandler(private val plugin: BambooLoomPlugin) {
	init {
		val project = plugin.project
		val loomPaths = plugin.loomPaths

		var mappings: TinyMapping? = null
		project.configurations.getByName("mapping").resolve().map { file ->
			mappings = if (mappings == null) {
				deserializeTinyV2Jar(file)
			} else {
				layerTinyMapping(mappings as TinyMapping, deserializeTinyV2Jar(file))
			}
		}

		project.configurations.getByName("mappedImplementation").incoming.artifacts.artifacts.forEach { artifact ->
			val file = artifact.file
			var outputFile = loomPaths.mappedLibrariesDir

			val moduleVersionId = artifact.id.componentIdentifier
			if (moduleVersionId is ModuleComponentIdentifier) {
				val group = moduleVersionId.group
				val name = moduleVersionId.module
				val version = moduleVersionId.version

				outputFile = outputFile.resolve("$group/$name/$version")
			} else {
				outputFile = outputFile.resolve(file.nameWithoutExtension)
			}
			outputFile = file.copyTo(outputFile.resolve(file.name), overwrite = true)

			project.logger.lifecycle("Remapping dependency: ${file.toURI()}")

			try {
				remapJarWithTinyMapping(mappings!!, "official", "named", file, outputFile = outputFile)
				project.logger.lifecycle("Successfully remapped: ${file.toURI()}")
			} catch (e: Exception) {
				project.logger.error("Failed to remap ${file.toURI()}: ${e.message}", e)
				throw e
			}
		}
	}
}