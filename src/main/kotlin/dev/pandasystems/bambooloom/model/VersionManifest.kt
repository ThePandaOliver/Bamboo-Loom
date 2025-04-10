package dev.pandasystems.bambooloom.model

import dev.pandasystems.bambooloom.BambooLoomPlugin
import java.io.File
import java.net.URI

data class VersionManifest(
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
	data class Downloads(
		val client: Download,
		val server: Download
	)

	data class Download(
		val sha1: String,
		val size: Int,
		val url: String
	) {
		@Transient
		lateinit var versionManifest: VersionManifest

		val file: File by lazy {
			val file = BambooLoomPlugin.versionCacheDir.resolve("${versionManifest.id}/${if (this == versionManifest.downloads.client) "client" else "server"}.jar")
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

	data class Library(
		val downloads: LibraryDownloads,
		val name: String,
		// rules
	)

	data class LibraryDownloads(
		val artifact: LibraryDownload,
	)

	data class LibraryDownload(
		val path: String,
		val url: String,
		val sha1: String,
		val size: Int
	) {
		@Transient
		lateinit var versionManifest: VersionManifest

		val file: File by lazy {
			val file = BambooLoomPlugin.versionCacheDir.resolve("${versionManifest.id}/libraries/$path")
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