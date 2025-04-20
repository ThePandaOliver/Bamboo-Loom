package dev.pandasystems.bambooloom.utils

import org.gradle.api.Project

class LoomPaths(project: Project) {
	val cacheDir = project.gradle.gradleUserHomeDir.resolve("caches/bamboo-loom")

	val versionDir = cacheDir.resolve("versions")
	val versionsManifestFile = versionDir.resolve("version_manifest_v2.json")
	fun versionFile(version: String) = versionDir.resolve("$version-meta.json")
	val mappingDir = cacheDir.resolve("mappings")

	val libraryCacheDir = cacheDir.resolve("libraries")
	val mojangLibraryCacheDir = libraryCacheDir.resolve("com/mojang")
}