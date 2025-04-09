package dev.pandasystems.bambooloom

import dev.pandasystems.bambooloom.model.VersionListManifest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository

class MCGradlePlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.plugins.apply("java-library")

		// Register repositories
		project.repositories.maven { repo: MavenArtifactRepository ->
			repo.setUrl("https://libraries.minecraft.net/")
		}

		// Create mapping configurations
		val mappedImplementation = project.configurations.create("mappedImplementation") {
			it.isCanBeConsumed = false
			it.isCanBeResolved = true
		}
		project.configurations.getByName("implementation") { it.extendsFrom(mappedImplementation) }
		val mappedCompileOnly = project.configurations.create("mappedCompileOnly") {
			it.isCanBeConsumed = false
			it.isCanBeResolved = true
		}
		project.configurations.getByName("compileOnly") { it.extendsFrom(mappedCompileOnly) }
		val mappedRuntimeOnly = project.configurations.create("mappedRuntimeOnly") {
			it.isCanBeConsumed = false
			it.isCanBeResolved = true
		}
		project.configurations.getByName("runtimeOnly") { it.extendsFrom(mappedRuntimeOnly) }

		// Create Minecraft configuration
		val minecraft = project.configurations.create("minecraft") {
			it.isCanBeConsumed = false
			it.isCanBeResolved = true
		}
		val mapping = project.configurations.create("mapping") {
			it.isCanBeConsumed = false
			it.isCanBeResolved = true
		}

		// Runs after build scripts has been evaluated.
		project.afterEvaluate {
			VersionListManifest.initialize(project)
		}
	}
}