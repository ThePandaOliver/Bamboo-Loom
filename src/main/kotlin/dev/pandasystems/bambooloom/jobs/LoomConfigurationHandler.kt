package dev.pandasystems.bambooloom.jobs

import dev.pandasystems.bambooloom.BambooLoomPlugin

class LoomConfigurationHandler(val plugin: BambooLoomPlugin) {
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

		createConfiguration("mappedImplementation")
		createConfiguration("mapping")
	}
}