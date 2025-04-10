package dev.pandasystems.bambooloom.utils

import org.gradle.api.Project

object LoomPaths {
	fun versionCacheDir(project: Project) = project.gradle.gradleUserHomeDir.resolve("caches/bamboo-loom/versions")
	fun versionsManifestFile(project: Project) = versionCacheDir(project).resolve("version_manifest_v2.json")
	fun versionManifestFile(project: Project, version: String) = versionCacheDir(project).resolve("$version/$version.json")
}