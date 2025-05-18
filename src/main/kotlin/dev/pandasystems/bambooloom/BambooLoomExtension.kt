package dev.pandasystems.bambooloom

import dev.pandasystems.bambooloom.utils.LoomFiles
import org.gradle.api.Project

open class BambooLoomExtension(val project: Project) {
	open lateinit var minecraftVersion: String
	

	val files = LoomFiles(project)
}