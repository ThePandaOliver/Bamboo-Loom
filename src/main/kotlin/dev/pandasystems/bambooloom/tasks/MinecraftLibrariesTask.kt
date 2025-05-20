package dev.pandasystems.bambooloom.tasks

import dev.pandasystems.bambooloom.BambooLoomExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import kotlin.jvm.java

abstract class MinecraftLibrariesTask : DefaultTask() {
	@TaskAction
	fun applyLibraries() {
		val loom = project.extensions.getByType(BambooLoomExtension::class.java)
		val meta = requireNotNull(loom.files.versionMetas[loom.minecraftVersion]) { "Failed to find metadata for ${loom.minecraftVersion}" }

		for (library in meta.libraries) {
			project.dependencies.add("implementation", library.name)
		}
	}
}