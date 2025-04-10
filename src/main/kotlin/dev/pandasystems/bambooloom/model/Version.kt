package dev.pandasystems.bambooloom.model

import com.google.gson.Gson
import dev.pandasystems.bambooloom.BambooLoomPlugin
import java.net.URI

data class Version(
	val id: String,
	val type: String,
	val url: String,
	val time: String,
	val releaseTime: String,
	val sha1: String,
	val complianceLevel: Int
) {
	val manifest: VersionManifest by lazy {
		val manifestFile = BambooLoomPlugin.versionCacheDir.resolve("$id/manifest.json")
		if (!manifestFile.exists()) {
			if (!manifestFile.parentFile.exists()) {
				manifestFile.parentFile.mkdirs()
			}

			URI(url).toURL().openStream().use { input ->
				manifestFile.outputStream().use { output ->
					input.copyTo(output)
				}
			}
		}

		Gson().fromJson(manifestFile.readText(), VersionManifest::class.java)
	}
}