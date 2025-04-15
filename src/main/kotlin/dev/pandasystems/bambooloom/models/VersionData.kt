package dev.pandasystems.bambooloom.models

/**
 * Data class representing the version data.
 * The class is based on the JSON structure of the Minecraft version launcher data.
 */
data class VersionData(
	// arguments
	val assetIndex: AssetIndex,
	val assets: Int,
	val complianceLevel: Int,
	val id: String,
	val downloads: Map<String, Download>,
	val javaVersion: JavaVersion,
	// libraries
	// Logging
	val mainClass: String,
	val minimumLauncherVersion: Int,
	val releaseTime: String,
	val time: String,
	val type: String,
) {
	data class AssetIndex(
		val id: String,
		val sha1: String,
		val size: Int,
		val totalSize: Int,
		val url: String
	)

	data class Download(
		val sha1: String,
		val size: Int,
		val url: String
	)

	data class JavaVersion(
		val component: String,
		val majorVersion: Int,
	)
}