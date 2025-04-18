package dev.pandasystems.bambooloom.jobs

import dev.pandasystems.bambooloom.BambooLoomPlugin

class MappingsProvider(val plugin: BambooLoomPlugin) {
	init {
		val project = plugin.project
	}
}