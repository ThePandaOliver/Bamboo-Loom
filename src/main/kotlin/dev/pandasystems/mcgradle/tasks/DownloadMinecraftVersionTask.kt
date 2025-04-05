package dev.pandasystems.mcgradle.tasks

import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.pandasystems.mcgradle.Constants
import dev.pandasystems.mcgradle.utils.downloadFileTo
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.net.URI

abstract class DownloadMinecraftVersionTask : DefaultTask() {
	init {
		group = "MC Gradle"
		description = "Downloads the Minecraft client and server JARs"

		dependsOn("downloadVersionManifest")
	}

	@TaskAction
	fun downloadVersion() {
		val versionJson = downloadVersionJson()

		val downloads = versionJson.getAsJsonObject("downloads")
		val client = downloads.getAsJsonObject("client")
		val server = downloads.getAsJsonObject("server")
		val clientUrl = client.get("url").asString
		val serverUrl = server.get("url").asString

		val clientMapping = downloads.getAsJsonObject("client_mappings")
		val serverMapping = downloads.getAsJsonObject("server_mappings")
		val clientMappingUrl = clientMapping.get("url").asString
		val serverMappingUrl = serverMapping.get("url").asString

		val versionPath = Constants.cacheVersionsPath(project).resolve(Constants.TEMP_MC_VERSION)
		downloadFileTo(URI(clientUrl).toURL(), versionPath.resolve("jars/client.jar"))
		downloadFileTo(URI(serverUrl).toURL(), versionPath.resolve("jars/server.jar"))
		downloadFileTo(URI(clientMappingUrl).toURL(), versionPath.resolve("mappings/client.txt"))
		downloadFileTo(URI(serverMappingUrl).toURL(), versionPath.resolve("mappings/server.txt"))
	}

	@Internal
	fun getVersionManifestData(): JsonObject {
		val versionManifestFile = Constants.cacheVersionsPath(project).resolve("version_manifest.json")
		return Gson().fromJson(versionManifestFile.readText(), JsonObject::class.java)
	}

	fun downloadVersionJson(): JsonObject {
		val versionManifest = getVersionManifestData()
		val versions = versionManifest.getAsJsonArray("versions")
		val version = versions.find { it.asJsonObject.get("id").asString == Constants.TEMP_MC_VERSION }!!.asJsonObject

		val outputPath = Constants.cacheVersionsPath(project).resolve("${Constants.TEMP_MC_VERSION}/manifest.json")
		downloadFileTo(URI(version.get("url").asString).toURL(), outputPath)
		return Gson().fromJson(outputPath.readText(), JsonObject::class.java)
	}
}