package dev.pandasystems.bambooloom

import dev.pandasystems.bambooloom.models.VersionManifest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.maven

class BambooLoomPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.plugins.apply("java-library")

		// Register repositories
		project.repositories.maven("https://libraries.minecraft.net/")

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

			val version = VersionManifest.get(project).getVersion(project, dependency.version!!)
			val manifest = version.manifest

			if (wantsClient) {
				val clientDownload = manifest.downloads.clientJar
				project.dependencies.add("mappedImplementation", project.files(clientDownload.file))
			}

			if (wantsServer) {
				val serverDownload = manifest.downloads.serverJar
				project.dependencies.add("mappedImplementation", project.files(serverDownload.file))
			}

			// Add libraries to the classpath
			manifest.libraries.forEach { library ->
				val libraryDownload = library.file
				if (libraryDownload.exists()) {
					project.dependencies.add("mappedImplementation", project.files(libraryDownload))
				} else {
					project.logger.warn("Library ${libraryDownload.name} does not exist, skipping.")
				}
			}
		}
	}
}