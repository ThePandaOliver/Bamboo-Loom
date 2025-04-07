package dev.pandasystems.mcgradle.tasks

import dev.pandasystems.mcgradle.Constants
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class AddMinecraftDependenciesTask : DefaultTask() {
    init {
        group = "MC Gradle"
        description = "Adds Minecraft dependencies for the specified version"
    }

    @TaskAction
    fun addDependencies() {
        val versionPath = Constants.cacheVersionsPath(project).resolve(Constants.TEMP_MC_VERSION)
        val clientJar = versionPath.resolve("jars/client.jar")
        val serverJar = versionPath.resolve("jars/server.jar")

        project.configurations.getByName("minecraft").dependencies.forEach { dependency ->
			println(dependency)
			project.dependencies.add("implementation", project.files(clientJar, serverJar))
        }
    }
}