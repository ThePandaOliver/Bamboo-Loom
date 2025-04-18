package dev.pandasystems.bambooloom

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.pandasystems.bambooloom.data.VersionManifestModel
import dev.pandasystems.bambooloom.jobs.ConfigurationProvider
import dev.pandasystems.bambooloom.jobs.MappingsProvider
import dev.pandasystems.bambooloom.jobs.MinecraftProvider
import dev.pandasystems.bambooloom.utils.LoomPaths
import dev.pandasystems.bambooloom.utils.downloadFrom
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.maven

class BambooLoomPlugin : Plugin<Project> {
	companion object {
		val gson: Gson = GsonBuilder().setLenient().setPrettyPrinting().create()
	}

	lateinit var project: Project

	val versionManifest: VersionManifestModel by lazy {
		val file = loomPaths.versionsManifestFile.downloadFrom("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
		gson.fromJson(file.readText(), VersionManifestModel::class.java)
	}

	lateinit var loomPaths: LoomPaths

	lateinit var configurationProvider: ConfigurationProvider

	lateinit var minecraftProvider: MinecraftProvider
	lateinit var mappingsProvider: MappingsProvider

	override fun apply(project: Project) {
		this.project = project
		this.loomPaths = LoomPaths(project)

		project.plugins.apply("java-library")

		// Register repositories
		project.repositories.maven("https://libraries.minecraft.net/")

		// Setup
		configurationProvider = ConfigurationProvider(this)

		project.afterEvaluate {
			minecraftProvider = MinecraftProvider(this@BambooLoomPlugin)
			mappingsProvider = MappingsProvider(this@BambooLoomPlugin)
		}
	}
}