package dev.pandasystems.bambooloom.jobs

import dev.pandasystems.bambooloom.BambooLoomPlugin
import dev.pandasystems.bambooloom.remapping.LoomRemapperV2
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import java.util.jar.JarFile

class MappingHandler(private val plugin: BambooLoomPlugin) {
	init {
		val project = plugin.project
		val loomPaths = plugin.loomPaths

		val remappers = project.configurations.getByName("mapping").resolve().map { file ->
			project.logger.lifecycle("Parsing mapping file: ${file.name}")
			LoomRemapperV2.parseTinyJar(JarFile(file))
		}
		project.configurations.getByName("mappedImplementation").incoming.artifacts.artifacts.forEach { artifact ->
			val file = artifact.file
			val jar = JarFile(file)
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
				remappers.forEach {
					it.remap(jar, outputFile)
					project.dependencies.add("implementation", project.files(outputFile))
				}
				project.logger.lifecycle("Successfully remapped: ${file.toURI()}")
			} catch (e: Exception) {
				project.logger.error("Failed to remap ${file.toURI()}: ${e.message}", e)
				throw e
			}
		}
	}
}