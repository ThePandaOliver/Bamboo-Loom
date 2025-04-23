package dev.pandasystems.bambooloom.utils

import org.gradle.api.Project

class LoomPaths(private val project: Project) {
	val cacheDir = project.gradle.gradleUserHomeDir.resolve("caches/bamboo-loom")

	val versionDir = cacheDir.resolve("versions")
	val versionsManifestFile = versionDir.resolve("version_manifest_v2.json")
	fun versionFile(version: String) = versionDir.resolve("$version-meta.json")
	val mappingDir = cacheDir.resolve("mappings")

	val libraryCacheDir = cacheDir.resolve("libraries")
	val mojangLibraryCacheDir = libraryCacheDir.resolve("com/mojang")

	val mappings = Mappings(this)
	class Mappings(paths: LoomPaths) {
		/**
		 * TXT file with mappings from obfuscated to official in tiny format.
		 */
		val obfuscated2OfficialTxt = paths.mappingDir.resolve("minecraft-obfuscated-2-official.txt")

		/**
		 * Jar file with mappings from intermediary to official in tiny format.
		 */
		val intermediary2OfficialJar = paths.mappingDir.resolve("minecraft-intermediary-2-official.jar")

		/**
		 * Tiny file with mappings from obfuscated to intermediary in tiny format.
		 */
		val obfuscated2IntermediaryTiny = paths.mappingDir.resolve("minecraft-obfuscated-2-intermediary.tiny")

		/**
		 * Jar file with mappings from obfuscated to intermediary in jar format.
		 */
		val obfuscated2IntermediaryJar = paths.mappingDir.resolve("minecraft-obfuscated-2-intermediary.jar")
	}
}