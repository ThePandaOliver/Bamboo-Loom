package dev.pandasystems.bambooloom.extensions

import dev.pandasystems.bambooloom.BambooLoomPlugin
import dev.pandasystems.bambooloom.utils.downloadFrom
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection

fun Project.minecraft(version: String): ConfigurableFileCollection {
	val plugin = BambooLoomPlugin.instances[project]!!
	val meta = plugin.versionMetas[version] ?: throw IllegalArgumentException("Unknown version: $version")

	val clientFile = plugin.loomPaths.mojangLibraryCacheDir.resolve("minecraft/minecraft-client-$version.jar").downloadFrom(meta.downloads.client.url)
	return files(clientFile)
}