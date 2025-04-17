package dev.pandasystems.bambooloom.jobs

import dev.pandasystems.bambooloom.data.VersionManifestModel
import dev.pandasystems.bambooloom.data.VersionModel
import dev.pandasystems.bambooloom.utils.LoomPaths
import dev.pandasystems.bambooloom.utils.downloadFrom
import dev.pandasystems.bambooloom.utils.notExists
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import java.net.URI
import javax.inject.Inject

abstract class MinecraftProvider @Inject constructor(
	private val project: Project
) : Runnable {
	val versionManifest: VersionManifestModel by lazy {
		val file = LoomPaths.versionsManifestFile(project)
			.downloadFrom(URI("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").toURL())

		Json.decodeFromString(file.readText())
	}

	override fun run() {
		project.configurations.getByName("minecraft").dependencies.forEach { dependency ->
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
			val versionJson = LoomPaths.versionFile(project, version).downloadFrom(URI(versionData.url).toURL()).run {
				Json.decodeFromString<VersionModel>(readText())
			}

			val clientFile = LoomPaths.versionJarsDir(project, version).resolve("client.jar").notExists {
				project.logger.lifecycle("Downloading Minecraft ${dependency.name} for version $version")
				it.downloadFrom(URI(versionJson.downloads.client.url).toURL())
			}

			project.dependencies.add("mappedImplementation", project.files(clientFile))
		}
	}
}