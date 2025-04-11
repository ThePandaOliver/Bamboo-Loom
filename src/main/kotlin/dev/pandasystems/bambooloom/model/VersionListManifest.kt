package dev.pandasystems.bambooloom.model

import com.google.gson.JsonObject
import dev.pandasystems.bambooloom.BambooLoomPlugin
import dev.pandasystems.bambooloom.utils.IOUtils
import dev.pandasystems.bambooloom.utils.LoomPaths
import org.gradle.api.Project
import java.io.File
import java.net.URI

class VersionListManifest(private val project: Project) {
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
			download(project)
		} else {
			initVersions(project)
		}
	}

	fun getVersion(project: Project, version: String): Version {
		if (!versions.contains(version)) download(project)
		val versionData = versions[version]

		return versionData ?: throw IllegalArgumentException("Version $version not found in manifest.")
	}

	fun download(project: Project) {
		val output = LoomPaths.versionsManifestFile(project)
		if (!output.parentFile.exists())
			output.parentFile.mkdirs()

		URI(versionManifestUrl).toURL().openStream().use { input ->
			output.outputStream().use { output ->
				input.copyTo(output)
			}
		}

		initVersions(project)
	}

	private fun initVersions(project: Project) {
		val versionManifestFile = LoomPaths.versionsManifestFile(project)

		val manifestObj: JsonObject =
			BambooLoomPlugin.GSON.fromJson(versionManifestFile.readText(), JsonObject::class.java)
		versions = manifestObj["versions"].asJsonArray
			.map { it.asJsonObject }
			.associate {
				val version = Version(it)
				version.id to version
			}

		val latestObj: JsonObject = manifestObj["latest"]!!.asJsonObject
		latest = Latest(
			release = versions[latestObj["release"].asString]
				?: throw IllegalArgumentException("Latest release version not found in manifest."),
			snapshot = versions[latestObj["snapshot"].asString]
				?: throw IllegalArgumentException("Latest snapshot version not found in manifest.")
		)
	}

	inner class Latest(
		val release: Version,
		val snapshot: Version,
	)

	inner class Version(json: JsonObject) {
		val id: String = json["id"].asString
		val type: String = json["type"].asString
		val url: String = json["url"].asString
		val time: String = json["time"].asString
		val releaseTime: String = json["releaseTime"].asString
		val sha1: String = json["sha1"].asString
		val complianceLevel: Int = json["complianceLevel"].asInt

		val manifest: VersionManifest by lazy {
			val file =
				IOUtils.downloadFileTo(url, LoomPaths.versionCacheDir(project).resolve("$id/manifest.json"))

			VersionManifest(BambooLoomPlugin.GSON.fromJson(file.readText(), JsonObject::class.java))
		}

		inner class VersionManifest(json: JsonObject) {
			val complianceLevel: Int = json["complianceLevel"].asInt
			val downloads: Downloads = Downloads(
				client = Download(
					path = "jars/client.jar",
					sha1 = json["downloads"].asJsonObject["client"].asJsonObject["sha1"].asString,
					size = json["downloads"].asJsonObject["client"].asJsonObject["size"].asInt,
					url = json["downloads"].asJsonObject["client"].asJsonObject["url"].asString
				),
				client_mappings = Download(
					path = "mappings/client.txt",
					sha1 = json["downloads"].asJsonObject["client_mappings"].asJsonObject["sha1"].asString,
					size = json["downloads"].asJsonObject["client_mappings"].asJsonObject["size"].asInt,
					url = json["downloads"].asJsonObject["client_mappings"].asJsonObject["url"].asString
				),
				server = Download(
					path = "jars/server.jar",
					sha1 = json["downloads"].asJsonObject["server"].asJsonObject["sha1"].asString,
					size = json["downloads"].asJsonObject["server"].asJsonObject["size"].asInt,
					url = json["downloads"].asJsonObject["server"].asJsonObject["url"].asString
				),
				server_mappings = Download(
					path = "mappings/server.txt",
					sha1 = json["downloads"].asJsonObject["server_mappings"].asJsonObject["sha1"].asString,
					size = json["downloads"].asJsonObject["server_mappings"].asJsonObject["size"].asInt,
					url = json["downloads"].asJsonObject["server_mappings"].asJsonObject["url"].asString
				)
			)
			val id: String = json["id"].asString
			val library: List<Library> = json["libraries"].asJsonArray.map {
				val libObj = it.asJsonObject
				Library(
					downloads = LibraryDownload(
						path = libObj["downloads"].asJsonObject["artifact"].asJsonObject["path"].asString,
						url = libObj["downloads"].asJsonObject["artifact"].asJsonObject["url"].asString,
						sha1 = libObj["downloads"].asJsonObject["artifact"].asJsonObject["sha1"].asString,
						size = libObj["downloads"].asJsonObject["artifact"].asJsonObject["size"].asInt
					),
					name = libObj["name"].asString
				)
			}
			val mainClass: String = json["mainClass"].asString
			val minimumLauncherVersion: Int = json["minimumLauncherVersion"].asInt
			val releaseTime: String = json["releaseTime"].asString
			val time: String = json["time"].asString
			val type: String = json["type"].asString

			inner class Downloads(
				val client: Download,
				val client_mappings: Download,
				val server: Download,
				val server_mappings: Download
			)

			inner class Download(
				val path: String,
				val sha1: String,
				val size: Int,
				val url: String
			) {
				val file: File by lazy {
					val file = LoomPaths.versionCacheDir(project)
						.resolve("${id}/$path")
					if (!file.exists()) {
						if (!file.parentFile.exists())
							file.parentFile.mkdirs()

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
				val downloads: LibraryDownload,
				val name: String,
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
						if (!file.parentFile.exists())
							file.parentFile.mkdirs()

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
