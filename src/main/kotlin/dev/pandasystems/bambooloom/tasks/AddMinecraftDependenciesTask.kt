package dev.pandasystems.bambooloom.tasks

import dev.pandasystems.bambooloom.minecraft.MinecraftRepository
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class AddMinecraftDependenciesTask : DefaultTask() {
    @TaskAction
    fun addDependencies() {
        project.configurations.getByName("minecraft").dependencies.forEach { dependency ->
			val group = dependency.group
			val name = dependency.name
			val version = dependency.version

			if (group != "net.minecraft" && group != "com.mojang") {
				logger.error("Invalid dependency group: $group. Expected 'net.minecraft' or 'com.mojang'.")
				return@forEach
			}

			if (name != "minecraft") {
				logger.error("Invalid dependency name: $name. Expected 'minecraft'.")
				return@forEach
			}

			if (version == null) {
				logger.error("Dependency version is null. Expected a valid version.")
				return@forEach
			}

			val clientJar = MinecraftRepository.getClientJar(project, version)
			val serverJar = MinecraftRepository.getServerJar(project, version)

			project.dependencies.add("mappedImplementation", project.files(clientJar, serverJar))

			MinecraftRepository.applyLibraries(project, version)
        }
    }
}