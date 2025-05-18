package dev.pandasystems.bambooloom

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
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
	}
}