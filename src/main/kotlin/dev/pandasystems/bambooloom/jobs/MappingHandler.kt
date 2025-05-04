package dev.pandasystems.bambooloom.jobs

import dev.pandasystems.bambooloom.BambooLoomPlugin
import dev.pandasystems.bambooloom.mapping.TinyMappings
import dev.pandasystems.bambooloom.mapping.TinyMappingsSerializer
import dev.pandasystems.bambooloom.mapping.applyMappings
import dev.pandasystems.bambooloom.mapping.createLayered
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import java.util.jar.JarFile

class MappingHandler(private val plugin: BambooLoomPlugin) {
	init {
		val project = plugin.project
		val loomPaths = plugin.loomPaths
		try {
			// Make a layered mapping
			require(project.configurations.getByName("mapping").resolve().isNotEmpty()) { "No mappings found!" }
			val mappings: TinyMappings = project.configurations.getByName("mapping").resolve().map { file ->
				val bytes = JarFile(file).use { jar -> jar.getInputStream(jar.getJarEntry("mappings/mappings.tiny")).readBytes() }
				TinyMappingsSerializer.deserialize(bytes.toString(Charsets.UTF_8))
			}.reduce { acc, mappings ->  acc.createLayered(mappings)}

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
					mappings.applyMappings("official", "named", file, outputFile)
					project.dependencies.add("implementation", project.files(outputFile))
					project.logger.lifecycle("Successfully remapped: ${file.toURI()}")
				} catch (e: Exception) {
					project.logger.error("Failed to remap ${file.toURI()}: ${e.message}", e)
					throw e
				}
			}
		} catch (e: Exception) {
			project.logger.error("Mapping failed: ${e.message}", e)
		}
	}
}