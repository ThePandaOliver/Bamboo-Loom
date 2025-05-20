package dev.pandasystems.bambooloom

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.the

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
		
		project.afterEvaluate { 
			setupMinecraft(project)
		}
	}
	
	fun setupMinecraft(project: Project) {
		val loom = project.the<BambooLoomExtension>()
		val meta = requireNotNull(loom.files.versionMetas[loom.minecraftVersion]) { "Failed to find metadata for ${loom.minecraftVersion}" }

		for (library in meta.libraries) {
			project.dependencies.add("implementation", library.name)
		}
	}
}