package dev.pandasystems.bambooloom.jobs

import dev.pandasystems.bambooloom.BambooLoomPlugin

class MappingHandler(private val plugin: BambooLoomPlugin) {
	init {
		val project = plugin.project
		println("test")
		project.configurations.getByName("mapping").resolve().forEach { file ->
			println(file.path)
		}
	}
}