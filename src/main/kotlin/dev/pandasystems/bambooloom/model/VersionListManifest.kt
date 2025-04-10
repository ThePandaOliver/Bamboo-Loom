package dev.pandasystems.bambooloom.model

import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.pandasystems.bambooloom.BambooLoomPlugin
import java.net.URI

object VersionListManifest {
	val versionManifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"

	lateinit var latest: Latest
	lateinit var versions: Map<String, Version>

	init {
		if (!BambooLoomPlugin.versionManifestFile.exists()) {
			download()
		} else {
			initVersions()
		}
	}

	fun getVersion(version: String): Version {
		if (!::versions.isInitialized) {
			throw IllegalStateException("VersionManifest not initialized. Call initialize() first.")
		}

		if (!versions.contains(version)) download()
		val versionData = versions[version]

		return versionData ?: throw IllegalArgumentException("Version $version not found in manifest.")
	}

	fun download() {
		val output = BambooLoomPlugin.versionManifestFile
		if (!output.parentFile.exists())
			output.parentFile.mkdirs()

		URI(versionManifestUrl).toURL().openStream().use { input ->
			output.outputStream().use { output ->
				input.copyTo(output)
			}
		}

		initVersions()
	}

	private fun initVersions() {
		val versionManifest = BambooLoomPlugin.versionManifestFile.readText()

		val manifestObj = Gson().fromJson(versionManifest, JsonObject::class.java)
		versions = manifestObj.getAsJsonArray("versions")
			.associateBy { Gson().fromJson(it, Version::class.java).id }
			.mapValues { Gson().fromJson(it.value, Version::class.java) }

		val latestObj = Gson().fromJson(manifestObj.get("latest"), JsonObject::class.java)
		latest = Latest(
			release = versions[latestObj.get("release").asString] ?: throw IllegalArgumentException("Latest release version not found in manifest."),
			snapshot = versions[latestObj.get("snapshot").asString] ?: throw IllegalArgumentException("Latest snapshot version not found in manifest.")
		)
	}

	data class Latest(
		val release: Version,
		val snapshot: Version,
	)
}
