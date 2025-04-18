package dev.pandasystems.bambooloom.jobs

import dev.pandasystems.bambooloom.BambooLoomPlugin
import org.gradle.api.Project
import javax.inject.Inject

class ConfigurationProvider(val plugin: BambooLoomPlugin) {
	init {
		val project = plugin.project

		fun createConfiguration(name: String, vararg extends: String = emptyArray()) {
			val config = project.configurations.create(name) {
				isCanBeConsumed = false
				isCanBeResolved = true
			}
			extends.forEach {
				project.configurations.getByName(it) { extendsFrom(config) }
			}
		}

		createConfiguration("mappedImplementation", "implementation")
		createConfiguration("mappedCompileOnly", "compileOnly")
		createConfiguration("mappedRuntimeOnly", "runtimeOnly")

		createConfiguration("minecraft")
		createConfiguration("mapping")
	}
}