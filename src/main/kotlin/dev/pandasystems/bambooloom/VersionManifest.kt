package dev.pandasystems.bambooloom

import com.google.gson.Gson
import dev.pandasystems.bambooloom.models.VersionManifestData
import dev.pandasystems.bambooloom.utils.IOUtils
import dev.pandasystems.bambooloom.utils.LoomPaths
import org.gradle.api.Project

class VersionManifest(private val project: Project) {
	companion object {
		private val instances = mutableMapOf<Project, VersionManifest>()
		private val GSON = Gson()

		fun get(project: Project): VersionManifest {
			return instances.getOrPut(project) { VersionManifest(project) }
		}
	}

	private val versionManifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"

	var data: VersionManifestData

	init {
		val dataFile = IOUtils.downloadFileTo(versionManifestUrl, LoomPaths.versionsManifestFile(project))
		data = GSON.fromJson(dataFile.readText(), VersionManifestData::class.java)
	}

	fun getVersion(version: String): VersionManifestData.Version {
		return data.versions.firstOrNull { it.id == version }
			?: throw IllegalArgumentException("Version $version not found in manifest.")
	}

	data class VersionCache(
		val data: VersionManifestData.Version,
	)
}