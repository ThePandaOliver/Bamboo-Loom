package dev.pandasystems.bambooloom.minecraft

import dev.pandasystems.bambooloom.utils.downloadFileTo
import org.gradle.api.Project
import org.gradle.internal.impldep.com.google.gson.Gson
import org.gradle.internal.impldep.com.google.gson.GsonBuilder
import org.gradle.internal.impldep.com.google.gson.JsonObject
import java.io.File
import java.net.URI

val GSON: Gson = GsonBuilder().setPrettyPrinting().create()

object MinecraftRepository {
	private const val versionsManifestUrl = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
	private fun versionsManifestFile(project: Project) = project.gradle.gradleUserHomeDir.resolve("caches/mc-gradle/versions/versions.json")
	private fun versionManifestFile(project: Project, version: String) = project.gradle.gradleUserHomeDir.resolve("caches/mc-gradle/versions/$version/manifest.json")

	// Getters

	fun getVersionsManifest(project: Project): JsonObject {
		val versionsManifestFile = versionsManifestFile(project)

		if (!versionsManifestFile.exists()) {
			downloadVersionsManifest(project)
		}
		return GSON.fromJson(versionsManifestFile.reader(), JsonObject::class.java)
	}

	fun getVersion(project: Project, version: String): JsonObject {
		val versionManifestFile = versionManifestFile(project, version)

		if (!versionManifestFile.exists()) {
			downloadVersionManifest(project, version)
		}

		return GSON.fromJson(versionManifestFile.reader(), JsonObject::class.java)
	}

	fun getClientJar(project: Project, version: String): File {
		val versionManifest = getVersion(project, version)
		val downloads = versionManifest.getAsJsonObject("downloads")
		val client = downloads.getAsJsonObject("client")
		val clientUrl = client.get("url").asString
		val clientSha1 = client.get("sha1").asString

		val versionPath = project.gradle.gradleUserHomeDir.resolve("caches/mc-gradle/versions/$version")
		val clientJarFile = versionPath.resolve("jars/client.jar")

		if (!clientJarFile.exists()) {
			clientJarFile.parentFile.mkdirs()
			downloadFileTo(URI(clientUrl).toURL(), clientJarFile)
		}

		return clientJarFile
	}

	fun getServerJar(project: Project, version: String): File {
		val versionManifest = getVersion(project, version)
		val downloads = versionManifest.getAsJsonObject("downloads")
		val server = downloads.getAsJsonObject("server")
		val serverUrl = server.get("url").asString
		val serverSha1 = server.get("sha1").asString

		val versionPath = project.gradle.gradleUserHomeDir.resolve("caches/mc-gradle/versions/$version")
		val serverJarFile = versionPath.resolve("jars/server.jar")

		if (!serverJarFile.exists()) {
			serverJarFile.parentFile.mkdirs()
			downloadFileTo(URI(serverUrl).toURL(), serverJarFile)
		}

		return serverJarFile
	}

	fun applyLibraries(project: Project, version: String) {
		val versionManifest = getVersion(project, version)
		val libraries = versionManifest.getAsJsonArray("libraries")

		libraries.forEach { library ->
			val libraryObject = library.asJsonObject
			val path = libraryObject.get("downloads").asJsonObject.get("artifact").asJsonObject.get("path").asString
			val url = libraryObject.get("downloads").asJsonObject.get("artifact").asJsonObject.get("url").asString
			val sha1 = libraryObject.get("downloads").asJsonObject.get("artifact").asJsonObject.get("sha1").asString

			val libraryFile = project.gradle.gradleUserHomeDir.resolve("caches/mc-gradle/libraries/$path.jar")
			if (!libraryFile.exists()) {
				libraryFile.parentFile.mkdirs()
				downloadFileTo(URI(url).toURL(), libraryFile)
			}

			project.dependencies.add("implementation", project.files(libraryFile))
		}
	}

	// Downloaders

	fun downloadVersionsManifest(project: Project) {
		val versionsManifestFile = versionsManifestFile(project)

		try {
			downloadFileTo(URI(versionsManifestUrl).toURL(), versionsManifestFile)
		} catch (e: Exception) {
			project.logger.error("Failed to download Minecraft versions manifest: ${e.message}")
			throw e
		}
	}

	fun downloadVersionManifest(project: Project, version: String) {
		val versionManifestFile = versionManifestFile(project, version)
		val versionsManifest = getVersionsManifest(project)
		val versionsArray = versionsManifest.getAsJsonArray("versions")

		val versionObject = versionsArray.find { it.asJsonObject.get("id").asString == version }?.asJsonObject
			?: throw IllegalArgumentException("Minecraft version $version not found.")

		val url = versionObject.get("url").asString

		try {
			downloadFileTo(URI(url).toURL(), versionManifestFile)
		} catch (e: Exception) {
			project.logger.error("Failed to download Minecraft version manifest: ${e.message}")
			throw e
		}
	}
}