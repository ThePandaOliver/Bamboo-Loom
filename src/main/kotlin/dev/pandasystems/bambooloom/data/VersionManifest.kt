package dev.pandasystems.bambooloom.data

import dev.pandasystems.bambooloom.utils.LoomPaths
import dev.pandasystems.bambooloom.utils.downloadFrom
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.api.Project
import java.net.URI

class VersionManifest private constructor(private val project: Project) {
	companion object {
		private val instances = mutableMapOf<Project, VersionManifest>()

		fun get(project: Project): VersionManifest {
			return instances.getOrPut(project) { VersionManifest(project) }
		}
	}

	private val versionManifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"

	init {
		if (!LoomPaths.versionsManifestFile(project).exists()) {
			download(project)
		} else {
			initVersions(project)
		}
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

		val json: JsonObject = Json.parseToJsonElement(versionManifestFile.readText()).jsonObject
		versions = json["versions"]!!.jsonArray
			.map { it.jsonObject }
			.associate {
				val version = Version(
					id = it.jsonObject["id"]!!.jsonPrimitive.content,
					type = it.jsonObject["type"]!!.jsonPrimitive.content,
					url = it.jsonObject["url"]!!.jsonPrimitive.content,
					time = it.jsonObject["time"]!!.jsonPrimitive.content,
					releaseTime = it.jsonObject["releaseTime"]!!.jsonPrimitive.content,
				)

				version.id to version
			}

		latest = Latest(
			release = versions[json["latest"]!!.jsonObject["release"]!!.jsonPrimitive.content]!!,
			snapshot = versions[json["latest"]!!.jsonObject["snapshot"]!!.jsonPrimitive.content]!!,
		)
	}

	lateinit var versions: Map<String, Version>
	lateinit var latest: Latest

	fun getVersion(project: Project, version: String): Version {
		if (!versions.contains(version)) download(project)
		val versionData = versions[version]

		return versionData ?: throw IllegalArgumentException("Version $version not found in manifest.")
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
	) {
		val manifest by lazy {
			val file = LoomPaths.versionFile(project, id)
			if (!file.exists()) {
				file.downloadFrom(URI(url).toURL())
			}
			VersionData(
				json = Json.decodeFromString(file.readText()),
				project = project,
				versionManifest = this@VersionManifest,
			)
		}
	}
}