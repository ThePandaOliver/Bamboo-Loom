package dev.pandasystems.mcgradle.tasks

import dev.pandasystems.mcgradle.Constants
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


abstract class ApplyMinecraftTask : DefaultTask() {
	init {
		group = "MC Gradle"
		description = "Applies Minecraft as dependencies"

		dependsOn("downloadMinecraftVersion")
	}

	@TaskAction
	fun applyMinecraft() {
		val versionPath = Constants.cacheVersionsPath(project).resolve(Constants.TEMP_MC_VERSION)
		val clientJar = versionPath.resolve("jars/client.jar")
		val serverJar = versionPath.resolve("jars/server.jar")
		val clientMapping = versionPath.resolve("mappings/client.txt")
		val serverMapping = versionPath.resolve("mappings/server.txt")
		project.dependencies.add("compileOnly", project.files(clientJar, serverJar))
	}
}