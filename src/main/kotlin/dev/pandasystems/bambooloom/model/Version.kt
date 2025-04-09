package dev.pandasystems.bambooloom.model

data class Version(
	val id: String,
	val type: String,
	val url: String,
	val time: String,
	val releaseTime: String,
	val sha1: String,
	val complianceLevel: Int
)