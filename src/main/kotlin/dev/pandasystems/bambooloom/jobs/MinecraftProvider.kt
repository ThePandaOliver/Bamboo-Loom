package dev.pandasystems.bambooloom.jobs

import dev.pandasystems.bambooloom.BambooLoomPlugin
import dev.pandasystems.bambooloom.data.VersionMeta
import dev.pandasystems.bambooloom.utils.downloadFrom
import dev.pandasystems.bambooloom.utils.notExists
import java.net.URI

class MinecraftProvider(private val plugin: BambooLoomPlugin) {
	init {
		val project = plugin.project
		val versionManifest = plugin.versionManifest
		val loomPaths = plugin.loomPaths
		val gson = BambooLoomPlugin.gson

		val minecraft = project.configurations.getByName("minecraft")
		minecraft.dependencies.forEach { dependency ->
			if (dependency.group != "net.minecraft" && dependency.name != "client" && dependency.name != "server" && dependency.name != "full") {
				throw IllegalArgumentException("Minecraft dependency must be in the form of net.minecraft:[client | server | full]:<version>")
			}

			// Set desired version
			val version = when (dependency.version) {
				"latest_release", "latest", "release" -> versionManifest.latest.release
				"latest_snapshot", "snapshot" -> versionManifest.latest.snapshot
				else -> {dependency.version}
			} ?: throw IllegalArgumentException("Unknown version: ${dependency.version}")

			// Get data
			val versionData = versionManifest.versions.find { it.id == version } ?: throw IllegalArgumentException("Unknown version: $version")
			val versionJson = loomPaths.versionFile(version).downloadFrom(URI(versionData.url).toURL()).run {
				gson.fromJson(readText(), VersionMeta::class.java)
			}

			// Add the game as dependencies
			val clientFile = loomPaths.versionJarsDir(version).resolve("minecraft-client.jar").notExists {
				project.logger.lifecycle("Downloading Minecraft ${dependency.name} for version $version")
				it.downloadFrom(URI(versionJson.downloads.client.url).toURL())
			}

			project.dependencies.add("mappedImplementation", project.files(clientFile))
		}
	}
}
