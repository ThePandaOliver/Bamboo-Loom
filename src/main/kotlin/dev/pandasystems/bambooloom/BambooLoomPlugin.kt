package dev.pandasystems.bambooloom

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.pandasystems.bambooloom.tasks.MinecraftLibrariesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.DefaultTaskExecutionRequest
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.register

class BambooLoomPlugin : Plugin<Project> {
	companion object {
		val instances = mutableMapOf<Project, BambooLoomPlugin>()
		val gson: Gson = GsonBuilder().setLenient().setPrettyPrinting().create()
	}

	override fun apply(project: Project) {
		instances[project] = this

		// Extensions
		project.extensions.create<BambooLoomExtension>("bambooLoom", project)

		// Repositories
		project.repositories.maven("https://libraries.minecraft.net/")
		project.repositories.maven("https://maven.fabricmc.net/")
		
		// Configurations
		project.configurations.create("minecraftLibrary") {
			isCanBeConsumed = false
			isCanBeResolved = true
		}
		
		// Tasks
		project.tasks.register<MinecraftLibrariesTask>("minecraftLibraries")
		
		// Setup startup tasks
		val startParameter = project.gradle.startParameter
		val taskRequests = ArrayList(startParameter.taskRequests)
		taskRequests.add(DefaultTaskExecutionRequest(listOf("minecraftLibraries")))
		startParameter.setTaskRequests(taskRequests)
	}
}