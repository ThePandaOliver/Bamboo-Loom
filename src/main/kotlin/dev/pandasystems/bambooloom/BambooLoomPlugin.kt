package dev.pandasystems.bambooloom

import com.google.gson.Gson
import dev.pandasystems.bambooloom.model.VersionListManifest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.kotlin.dsl.maven

class BambooLoomPlugin : Plugin<Project> {
	companion object {
		val GSON = Gson()
	}

	override fun apply(project: Project) {
		project.plugins.apply("java-library")

		// Register repositories
		project.repositories.maven { repo: MavenArtifactRepository ->
			repo.setUrl("https://libraries.minecraft.net/")
		}

		// Create mapping configurations
		val mappedImplementation = project.configurations.create("mappedImplementation") {
			isCanBeConsumed = false
			isCanBeResolved = true
		}
		project.configurations.getByName("implementation") { extendsFrom(mappedImplementation) }
		val mappedCompileOnly = project.configurations.create("mappedCompileOnly") {
			isCanBeConsumed = false
			isCanBeResolved = true
		}
		project.configurations.getByName("compileOnly") { extendsFrom(mappedCompileOnly) }
		val mappedRuntimeOnly = project.configurations.create("mappedRuntimeOnly") {
			isCanBeConsumed = false
			isCanBeResolved = true
		}
		project.configurations.getByName("runtimeOnly") { extendsFrom(mappedRuntimeOnly) }

		// Create Minecraft configuration
		val minecraft = project.configurations.create("minecraft") {
			isCanBeConsumed = false
			isCanBeResolved = true
		}
		val mapping = project.configurations.create("mapping") {
			isCanBeConsumed = false
			isCanBeResolved = true
		}

		// Runs after build scripts has been evaluated.
		project.afterEvaluate {
			applyMinecraftDependency(project, minecraft)
		}
	}

	fun applyMinecraftDependency(project: Project, minecraftConfig: Configuration) {
		minecraftConfig.dependencies.forEach { dependency ->
			if (dependency.group != "net.minecraft" && dependency.name != "client" && dependency.name != "server" && dependency.name != "full") {
				throw IllegalArgumentException("Minecraft dependency must be in the form of net.minecraft:[client | server | full]:<version>")
			}
			val wantsClient = dependency.name != "server"
			val wantsServer = dependency.name != "client"

			val version = VersionListManifest.get(project).getVersion(dependency.version!!)
			val manifest = version.manifest
			val clientDownload = manifest.downloads.client
			val serverDownload = manifest.downloads.server

			if (wantsClient)
				project.dependencies.add("mappedImplementation", project.files(clientDownload.file))

			if (wantsServer)
				project.dependencies.add("mappedImplementation", project.files(serverDownload.file))
		}
	}
}