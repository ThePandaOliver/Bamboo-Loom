package dev.pandasystems.bambooloom.utils

import org.gradle.api.Project

object LoomPaths {
	/**
	 * Returns the cache directory for the Loom plugin.
	 */
	fun cacheDir(project: Project) = project.gradle.gradleUserHomeDir.resolve("caches/bamboo-loom")

	/**
	 * Returns the file path where Loom will store the version manifest.
	 */
	fun versionsManifestFile(project: Project) = cacheDir(project).resolve("versions/version_manifest_v2.json")

	/**
	 * Returns the directory where Loom will store the version-specific files
	 */
	fun versionCacheDir(project: Project, version: String) = cacheDir(project).resolve("versions/$version")

	/**
	 * Returns the file path where Loom will store the version-specific manifest
	 */
	fun versionFile(project: Project, version: String) = versionCacheDir(project, version).resolve("manifest.json")

	/**
	 * Returns the file path where Loom will store the version-specific jar files
	 */
	fun versionJarsDir(project: Project, version: String) = versionCacheDir(project, version).resolve("jars")

	/**
	 * Returns the file path where Loom will store the version-specific mappings
	 */
	fun versionMappingsDir(project: Project, version: String) = versionCacheDir(project, version).resolve("mappings")

	/**
	 * Returns the file path where Loom will store all libraries
	 */
	fun libraryCacheDir(project: Project) = cacheDir(project).resolve("libraries")
}