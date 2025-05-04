package dev.pandasystems.bambooloom.utils

import org.gradle.api.Project

class LoomPaths(private val project: Project) {
	val projectGradleDir = project.rootDir.resolve(".gradle")
	val cacheDir = project.gradle.gradleUserHomeDir.resolve("caches/bamboo-loom")
	val projectCacheDir = projectGradleDir.resolve("bamboo-loom-cache")

	val versionDir = cacheDir.resolve("versions")
	val versionsManifestFile = versionDir.resolve("version_manifest_v2.json")
	fun versionFile(version: String) = versionDir.resolve("$version-meta.json")
	val mappingDir = cacheDir.resolve("mappings")

	val libraryCacheDir = cacheDir.resolve("libraries")
	val mappedLibrariesDir = projectCacheDir.resolve("remapped-libraries")

	val mappings = Mappings(this)
	class Mappings(paths: LoomPaths) {
	}
}