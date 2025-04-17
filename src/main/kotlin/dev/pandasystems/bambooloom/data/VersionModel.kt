package dev.pandasystems.bambooloom.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
@JsonIgnoreUnknownKeys
data class VersionModel(
	val downloads: Downloads
) {
	@Serializable
	@JsonIgnoreUnknownKeys
	data class Downloads(
		val client: Download,
		val server: Download
	)

	@Serializable
	@JsonIgnoreUnknownKeys
	data class Download(
		val sha1: String,
		val size: Int,
		val url: String
	)
}