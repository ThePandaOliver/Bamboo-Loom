package dev.pandasystems.bambooloom

import dev.pandasystems.bambooloom.jobs.ConfigurationProvider
import dev.pandasystems.bambooloom.jobs.MinecraftProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.maven
import org.slf4j.LoggerFactory

class BambooLoomPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.plugins.apply("java-library")

		// Register repositories
		project.repositories.maven("https://libraries.minecraft.net/")

		listOf(
			ConfigurationProvider::class.java
		).forEach {
			project.logger.lifecycle("Running ${it.simpleName}")
			project.objects.newInstance(it).run()
		}

		project.afterEvaluate {
			listOf(
				MinecraftProvider::class.java
			).forEach {
				project.logger.lifecycle("Running ${it.simpleName}")
				project.objects.newInstance(it).run()
			}
		}
	}
}