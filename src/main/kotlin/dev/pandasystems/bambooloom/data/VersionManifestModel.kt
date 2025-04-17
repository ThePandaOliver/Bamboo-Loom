package dev.pandasystems.bambooloom.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
@JsonIgnoreUnknownKeys
data class VersionManifestModel(
	val latest: Latest,
	val versions: List<Version>
) {
	@Serializable
	@JsonIgnoreUnknownKeys
	data class Latest(
		val release: String,
		val snapshot: String
	)

	@Serializable
	@JsonIgnoreUnknownKeys
	data class Version(
		val id: String,
		val type: String,
		val url: String,
		val time: String,
		val releaseTime: String,
		val sha1: String,
		val complianceLevel: Int
	)
}