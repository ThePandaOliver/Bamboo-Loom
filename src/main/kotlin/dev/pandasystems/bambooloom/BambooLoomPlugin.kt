package dev.pandasystems.bambooloom

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.lang.Newify
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.DefaultTaskExecutionRequest
import org.gradle.internal.impldep.org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.maven

class BambooLoomPlugin : Plugin<Project> {
	companion object {
		val instances = mutableMapOf<Project, BambooLoomPlugin>()
		val gson: Gson = GsonBuilder().setLenient().setPrettyPrinting().create()
	}

	lateinit var project: Project


	override fun apply(project: Project) {
		instances[project] = this
		this.project = project

		// Extensions
		project.extensions.create<BambooLoomExtension>("bambooLoom", project)

		// Repositories
		project.repositories.maven("https://libraries.minecraft.net/")
		project.repositories.maven("https://maven.fabricmc.net/")
		
		val startParameter = project.gradle.startParameter
		val taskRequests = ArrayList(startParameter.taskRequests)
		taskRequests.add(DefaultTaskExecutionRequest(listOf(/* Register tasks to run on sync */)))
		startParameter.setTaskRequests(taskRequests)
	}
}