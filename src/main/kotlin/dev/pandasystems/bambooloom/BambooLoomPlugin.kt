package dev.pandasystems.bambooloom

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.pandasystems.bambooloom.data.VersionManifest
import dev.pandasystems.bambooloom.data.VersionMeta
import dev.pandasystems.bambooloom.jobs.LoomConfigurationHandler
import dev.pandasystems.bambooloom.jobs.MappingHandler
import dev.pandasystems.bambooloom.utils.LazyMap
import dev.pandasystems.bambooloom.utils.LoomPaths
import dev.pandasystems.bambooloom.utils.downloadFrom
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URI

class BambooLoomPlugin : Plugin<Project> {
	companion object {
		val instances = mutableMapOf<Project, BambooLoomPlugin>()
		val gson: Gson = GsonBuilder().setLenient().setPrettyPrinting().create()
	}

	lateinit var project: Project

	val versionManifest: VersionManifest by lazy {
		val file = loomPaths.versionsManifestFile.downloadFrom("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
		gson.fromJson(file.readText(), VersionManifest::class.java)
	}
	val versionMetas: Map<String, VersionMeta> = LazyMap { key ->
		val version = versionManifest.versions.find { it.id == key } ?: return@LazyMap null

		loomPaths.versionFile(key).downloadFrom(URI(version.url).toURL()).run {
			gson.fromJson(readText(), VersionMeta::class.java)
		}
	}

	lateinit var loomPaths: LoomPaths

	lateinit var configurationProvider: LoomConfigurationHandler

	override fun apply(project: Project) {
		instances[project] = this
		this.project = project
		this.loomPaths = LoomPaths(project)

		project.plugins.apply("java-library")

		// Setup
		configurationProvider = LoomConfigurationHandler(this)
		project.afterEvaluate {
			MappingHandler(this@BambooLoomPlugin)
		}
	}
}