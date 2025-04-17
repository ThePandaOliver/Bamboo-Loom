package dev.pandasystems.bambooloom

import dev.pandasystems.bambooloom.data.VersionManifest
import dev.pandasystems.bambooloom.jobs.MinecraftProvider
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

		listOf(
			MinecraftProvider::class.java
		).forEach {
			project.objects.newInstance(it).run()
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
				val clientDownload = manifest.gameJars.clientJarFile
				project.dependencies.add("mappedImplementation", project.files(clientDownload))
			}

			if (wantsServer) {
				val serverDownload = manifest.gameJars.serverJarFile
				project.dependencies.add("mappedImplementation", project.files(serverDownload))
			}

			// Add libraries to the classpath
			manifest.libraries.forEach { library ->
				project.dependencies.add("mappedImplementation", project.files(library.file))
			}
		}
	}
}