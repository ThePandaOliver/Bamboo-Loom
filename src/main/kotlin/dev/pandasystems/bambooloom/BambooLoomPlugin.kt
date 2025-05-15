package dev.pandasystems.bambooloom

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.pandasystems.bambooloom.jobs.LoomConfigurationHandler
import dev.pandasystems.bambooloom.jobs.MappingHandler
import dev.pandasystems.bambooloom.utils.LoomFiles
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.maven

class BambooLoomPlugin : Plugin<Project> {
	companion object {
		val instances = mutableMapOf<Project, BambooLoomPlugin>()
		val gson: Gson = GsonBuilder().setLenient().setPrettyPrinting().create()
	}

	lateinit var project: Project

	lateinit var configurationProvider: LoomConfigurationHandler

	override fun apply(project: Project) {
		try {
			instances[project] = this
			this.project = project
			
			// Extensions
			project.extensions.create<LoomFiles>("loomFiles", project)

			// Plugins
			project.plugins.apply("java-library")

			// Repositories
			project.repositories.maven("https://maven.fabricmc.net/")

			// Setup
			configurationProvider = LoomConfigurationHandler(this)
			project.afterEvaluate {
				try {
					MappingHandler(this@BambooLoomPlugin)
				} catch (e: Exception) {
					project.logger.error("Failed to apply Bamboo Loom plugin after evaluation", e)
					throw e
				}
			}
		} catch (e: Exception) {
			project.logger.error("Failed to apply Bamboo Loom plugin", e)
			throw e
		}
	}
}