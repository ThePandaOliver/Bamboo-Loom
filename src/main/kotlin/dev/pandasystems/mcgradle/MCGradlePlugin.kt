package dev.pandasystems.mcgradle

import dev.pandasystems.mcgradle.tasks.ApplyMinecraftTask
import dev.pandasystems.mcgradle.tasks.DownloadMinecraftVersionTask
import dev.pandasystems.mcgradle.tasks.DownloadVersionManifestTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository

class MCGradlePlugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.plugins.apply("java-library")

		// Register repositories
		target.repositories.maven { repo: MavenArtifactRepository ->
			repo.setUrl("https://libraries.minecraft.net/")
		}

		// Register tasks
		target.tasks.register("downloadVersionManifest", DownloadVersionManifestTask::class.java)
		target.tasks.register("downloadMinecraftVersion", DownloadMinecraftVersionTask::class.java)
		target.tasks.register("applyMinecraft", ApplyMinecraftTask::class.java)

		target.tasks.named("prepareKotlinBuildScriptModel").configure { it.dependsOn("applyMinecraft") }

//		javaClass.getClassLoader().getResourceAsStream("1.21.5.json").use {
//			val jsonObject: JsonObject = gson.fromJson<JsonObject>(InputStreamReader(it!!), JsonObject::class.java)
//
//			// Implement libraries
//			jsonObject.getAsJsonArray("libraries").forEach {
//				target.dependencies.add("implementation", it!!.getAsJsonObject().get("name").asString)
//			}
//
//			// Download Minecraft
//			val downloadsObject = jsonObject.getAsJsonObject("downloads")
//			val clientUrl = downloadsObject.getAsJsonObject("client").get("url").asString
//			val clientMappingsUrl = downloadsObject.getAsJsonObject("client_mappings").get("url").asString
//			val serverUrl = downloadsObject.getAsJsonObject("server").get("url").asString
//			val serverMappingsUrl = downloadsObject.getAsJsonObject("server_mappings").get("url").asString
//
//
//			// Implement Minecraft and apply mappings
//		}
	}
}