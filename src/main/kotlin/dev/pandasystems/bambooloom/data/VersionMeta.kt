package dev.pandasystems.bambooloom.data

data class VersionMeta(
	val downloads: Downloads
) {
	data class Downloads(
		val client: Download,
		val clientMappings: Download,
		val server: Download,
		val serverMappings: Download
	)

	data class Download(
		val sha1: String,
		val size: Int,
		val url: String
	)
}
