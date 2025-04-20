package dev.pandasystems.bambooloom.data

import com.google.gson.annotations.SerializedName

data class VersionMeta(
	val downloads: Downloads
) {
	data class Downloads(
		val client: Download,
		@SerializedName("client_mappings")
		val clientMappings: Download,
		val server: Download,
		@SerializedName("server_mappings")
		val serverMappings: Download
	)

	data class Download(
		val sha1: String,
		val size: Int,
		val url: String
	)
}
