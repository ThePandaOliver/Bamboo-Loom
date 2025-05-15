package dev.pandasystems.bambooloom.utils

import dev.pandasystems.bambooloom.BambooLoomPlugin
import dev.pandasystems.bambooloom.data.VersionManifest
import dev.pandasystems.bambooloom.data.VersionMeta
import org.gradle.api.Project
import java.io.File
import kotlin.jvm.java

open class LoomFiles(private val project: Project) {
	// Gradle
	val projectGradleDir = project.rootDir.resolve(".gradle")
	val gradleDir = project.gradle.gradleUserHomeDir
	
	// Cache directory
	val projectCacheDir = projectGradleDir.resolve("bamboo-loom-cache")
	val cacheDir = gradleDir.resolve("caches/bamboo-loom")
	
	// Manifest
	val versionManifestFile by lazy { 
		cacheDir.resolve("versions/version_manifest_v2.json").apply {
			if (!exists()) {
				downloadFrom("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
			}
		}
	}
	val versionManifest: VersionManifest by lazy {
		requireNotNull(BambooLoomPlugin.gson.fromJson(versionManifestFile.readText(), VersionManifest::class.java)) { "Failed to parse version manifest" }
	}
	val versionMetaFiles = LazyMap<String, File> { version ->
		val meta = versionManifestFile.readText().let { BambooLoomPlugin.gson.fromJson(it, VersionManifest::class.java) }
			.versions.find { it.id == version } ?: return@LazyMap null
		cacheDir.resolve("versions/${version}_meta.json").apply {
			if (!exists()) {
				downloadFrom(meta.url)
			}
		}
	}
	val versionMetas = LazyMap<String, VersionMeta> { version ->
		versionMetaFiles[version]?.let { BambooLoomPlugin.gson.fromJson(it.readText(), VersionMeta::class.java) }
	}
	
	// Library
	val libraryCacheDir = cacheDir.resolve("libraries")
	fun libraryFile(group: String, name: String, version: String, fileName: String = "$name-$version.jar") = libraryCacheDir.resolve("$group/$name/$version/$fileName")
	val mappedLibrariesDir = projectCacheDir.resolve("remapped-libraries")
	fun mappedLibraryFile(group: String, name: String, version: String, fileName: String = "$name-$version.jar") = mappedLibrariesDir.resolve("$group/$name/$version/$fileName")

	// Mappings
	val mappings = Mappings()
	inner class Mappings {
		fun official2Intermediary(version: String) = libraryFile("net.fabricmc", "intermediary", version, "intermediary-$version-v2.tiny")
	}
}