package dev.pandasystems.bambooloom.model

data class VersionManifest(
	val id: String,
	val downloads: Downloads,
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
	)
}