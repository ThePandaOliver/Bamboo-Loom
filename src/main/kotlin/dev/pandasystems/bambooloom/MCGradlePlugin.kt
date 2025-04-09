package dev.pandasystems.bambooloom

import dev.pandasystems.bambooloom.model.VersionListManifest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import java.io.File

class MCGradlePlugin : Plugin<Project> {
	override fun apply(project: Project) {
		initialize(project)

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

			val version = VersionListManifest.getVersion(dependency.version!!)
			val manifest = version.manifest
			val clientDownload = manifest.downloads.client
			val serverDownload = manifest.downloads.server

			if (wantsClient)
				project.dependencies.add("mappedImplementation", project.files(clientDownload.file))

			if (wantsServer)
				project.dependencies.add("mappedImplementation", project.files(serverDownload.file))
		}
	}

	companion object {
		lateinit var versionCacheDir: File
		lateinit var versionManifestFile: File

		private fun initialize(project: Project) {
			versionCacheDir = project.gradle.gradleUserHomeDir.resolve("caches/bamboo-loom/versions")
			versionManifestFile = versionCacheDir.resolve("version_manifest.json")
		}
	}
}