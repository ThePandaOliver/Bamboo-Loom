package dev.pandasystems.mcgradle

import org.gradle.api.Project

object Constants {
	const val TEMP_MC_VERSION = "1.21.5"

	const val VERSION_MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

	fun cachePath(project: Project) = project.gradle.gradleUserHomeDir.resolve("caches/mc-gradle")
	fun cacheVersionsPath(project: Project) = cachePath(project).resolve("versions")

	/**
	 * Represents the duration in milliseconds after which a cached file is considered expired
	 * and subject to re-download. Typically used to ensure that stale or outdated caches
	 * are refreshed after 24 hours (default duration specified here).
	 */
	const val FILE_EXPIRATION_TIME: Long = 24 * 60 * 60 * 1000
}

