package dev.pandasystems.bambooloom.model

import com.google.gson.JsonObject
import dev.pandasystems.bambooloom.BambooLoomPlugin
import dev.pandasystems.bambooloom.utils.LoomPaths
import org.gradle.api.Project
import org.gradle.internal.impldep.com.google.gson.Gson
import org.gradle.internal.lazy.Lazy
import java.io.File
import java.net.URI

class VersionListManifest private constructor(private val project: Project) {
	companion object {
		private val instances = mutableMapOf<Project, VersionListManifest>()

		fun get(project: Project): VersionListManifest {
			return instances.getOrPut(project) { VersionListManifest(project) }
		}
	}

	private val versionManifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"

	lateinit var latest: Latest
	lateinit var versions: Map<String, Version>

	init {
		if (!LoomPaths.versionsManifestFile(project).exists()) {
			download()
		} else {
			initVersions()
		}
	}

	fun getVersion(version: String): Version {
		if (!versions.contains(version)) download()
		val versionData = versions[version]

		return versionData ?: throw IllegalArgumentException("Version $version not found in manifest.")
	}

	fun download() {
		val output = LoomPaths.versionsManifestFile(project)
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
		val versionManifest = LoomPaths.versionsManifestFile(project).readText()

		val manifestObj: JsonObject = BambooLoomPlugin.GSON.fromJson(versionManifest, JsonObject::class.java)
		versions = manifestObj["versions"]?.asJsonArray
			?.map { it.asJsonObject }
			?.associate { versionObj ->
				val data = BambooLoomPlugin.GSON.fromJson(versionObj, Version::class.java)
				data.id to data
			} ?: emptyMap()

		val latestObj: JsonObject = manifestObj["latest"]!!.asJsonObject
		latest = Latest(
			release = versions[latestObj["release"]!!.asString] ?: throw IllegalArgumentException("Latest release version not found in manifest."),
			snapshot = versions[latestObj["snapshot"]!!.asString] ?: throw IllegalArgumentException("Latest snapshot version not found in manifest.")
		)
	}

	inner class Latest(
		val release: Version,
		val snapshot: Version,
	)

	inner class Version(
		val id: String,
		val type: String,
		val url: String,
		val time: String,
		val releaseTime: String,
		val sha1: String,
		val complianceLevel: Int
	) {
		val manifest: VersionManifest by lazy {
			val manifestFile = LoomPaths.versionCacheDir(project).resolve("$id/manifest.json")
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

			BambooLoomPlugin.GSON.fromJson(manifestFile.readText(), VersionManifest::class.java)
		}

		inner class VersionManifest(
			// arguments
			// assetIndex
			// assets
			val complianceLevel: Int,
			val downloads: Downloads,
			val id: String,
			// javaVersion
			val library: List<Library>,
			// logging
			val mainClass: String,
			val minimumLauncherVersion: Int,
			val releaseTime: String,
			val time: String,
			val type: String
		) {
			inner class Downloads(
				val client: Download,
				val server: Download
			)

			inner class Download(
				val sha1: String,
				val size: Int,
				val url: String
			) {
				val file: File by lazy {
					val file = LoomPaths.versionCacheDir(project)
						.resolve("${id}/${if (this == downloads.client) "client" else "server"}.jar")
					if (!file.exists()) {
						URI(url).toURL().openStream().use { input ->
							file.outputStream().use { output ->
								input.copyTo(output)
							}
						}
					}

					file
				}
			}

			inner class Library(
				val downloads: LibraryDownloads,
				val name: String,
				// rules
			)

			inner class LibraryDownloads(
				val artifact: LibraryDownload,
			)

			inner class LibraryDownload(
				val path: String,
				val url: String,
				val sha1: String,
				val size: Int
			) {
				val file: File by lazy {
					val file = LoomPaths.versionCacheDir(project).resolve("${id}/libraries/$path")
					if (!file.exists()) {
						URI(url).toURL().openStream().use { input ->
							file.outputStream().use { output ->
								input.copyTo(output)
							}
						}
					}

					file
				}
			}
		}
	}
}
