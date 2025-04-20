package dev.pandasystems.bambooloom.extensions

import dev.pandasystems.bambooloom.BambooLoomPlugin
import dev.pandasystems.bambooloom.data.Mapping
import dev.pandasystems.bambooloom.remapping.RemapperTool
import dev.pandasystems.bambooloom.utils.downloadFrom
import dev.pandasystems.bambooloom.utils.notExists
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection

fun Project.minecraft(version: String): ConfigurableFileCollection {
	val plugin = BambooLoomPlugin.instances[project]!!
	val meta = plugin.versionMetas[version] ?: throw IllegalArgumentException("Unknown version: $version")

	val clientFile = plugin.loomPaths.mojangLibraryCacheDir.resolve("minecraft/minecraft-client-$version.jar").notExists { file ->
		file.downloadFrom(meta.downloads.client.url)

		val mapping = plugin.loomPaths.mappingDir.resolve("minecraft-client-$version-official.txt")
			.downloadFrom(meta.downloads.clientMappings.url).let { mappingFile ->
				Mapping.parseOfficial(mappingFile.readText())
			}

		RemapperTool(mapping).remap(file)
	}

	return files(clientFile)
}