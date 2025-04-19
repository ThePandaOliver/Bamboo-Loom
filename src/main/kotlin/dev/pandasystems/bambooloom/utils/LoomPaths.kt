package dev.pandasystems.bambooloom.utils

import org.gradle.api.Project

class LoomPaths(project: Project) {
	val cacheDir = project.gradle.gradleUserHomeDir.resolve("caches/bamboo-loom")
	val versionsManifestFile = cacheDir.resolve("versions/version_manifest_v2.json")

	// Version-specific files
	fun versionCacheDir(version: String) = cacheDir.resolve("versions/$version")
	fun versionFile(version: String) = versionCacheDir(version).resolve("manifest.json")
	fun versionJarsDir(version: String) = versionCacheDir(version).resolve("jars")
	fun versionMappingsDir(version: String) = versionCacheDir(version).resolve("mappings")

	val libraryCacheDir = cacheDir.resolve("libraries")
	val mojangLibraryCacheDir = libraryCacheDir.resolve("com/mojang")
}