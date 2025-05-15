package dev.pandasystems.bambooloom.jobs

import dev.pandasystems.bambooloom.BambooLoomPlugin
import dev.pandasystems.bambooloom.utils.notExists
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import net.fabricmc.tinyremapper.TinyUtils
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import java.util.jar.JarFile

class MappingHandler(private val plugin: BambooLoomPlugin) {
	init {
		val project = plugin.project
		val loomPaths = plugin.loomPaths
		try {
			val mappingProviders = project.configurations.getByName("mapping").resolve().map { file ->
				// Check if we have already extracted the mapping file from the jar
				val extractedMappingFile = file.parentFile.resolve(file.nameWithoutExtension + ".tiny").notExists { extractedMappings ->
					JarFile(file).use { jar -> 
						val tinyBytes = jar.getInputStream(jar.getJarEntry("mappings/mappings.tiny")).readBytes()
						extractedMappings.outputStream().use { it.write(tinyBytes) }
					}
				}
				
				TinyUtils.createTinyMappingProvider(extractedMappingFile.toPath(), "intermediary", "named")
			}
			
			project.configurations.getByName("mappedImplementation").incoming.artifacts.artifacts.forEach { artifact ->
				val file = artifact.file
				val outputFile = loomPaths.mappedLibrariesDir.let { outputFile ->
					val moduleVersionId = artifact.id.componentIdentifier
					if (moduleVersionId is ModuleComponentIdentifier) {
						val group = moduleVersionId.group
						val name = moduleVersionId.module
						val version = moduleVersionId.version

						outputFile.resolve("$group/$name/$version")
					} else {
						outputFile.resolve(file.nameWithoutExtension)
					}
				}.resolve(file.name)

				project.logger.lifecycle("Remapping dependency: ${file.toURI()}")
				try {
					for (provider in mappingProviders) {
						val tinyRemapper = TinyRemapper.newRemapper()
							.withMappings(provider)
							.build()
						tinyRemapper.readInputs(file.toPath())

						OutputConsumerPath.Builder(outputFile.toPath()).build().use { outputConsumer ->
							outputConsumer.addNonClassFiles(file.toPath())
							tinyRemapper.apply(outputConsumer)
						}
						tinyRemapper.finish()
					}
					
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