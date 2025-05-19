package dev.pandasystems.bambooloom

import dev.pandasystems.bambooloom.utils.LoomFiles
import org.gradle.api.Project

open class BambooLoomExtension(val project: Project) {
	open var minecraftVersion: String = "1.21.5"
	

	val files = LoomFiles(project)
}