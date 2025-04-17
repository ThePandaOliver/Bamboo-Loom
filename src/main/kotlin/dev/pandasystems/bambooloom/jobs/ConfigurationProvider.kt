package dev.pandasystems.bambooloom.jobs

import org.gradle.api.Project
import javax.inject.Inject

abstract class ConfigurationProvider @Inject constructor(
	private val project: Project
) : Runnable {
	override fun run() {
		createConfiguration("mappedImplementation", "implementation")
		createConfiguration("mappedCompileOnly", "compileOnly")
		createConfiguration("mappedRuntimeOnly", "runtimeOnly")

		createConfiguration("minecraft")
		createConfiguration("mapping")
	}

	fun createConfiguration(name: String, vararg extends: String = emptyArray()) {
		val config = project.configurations.create(name) {
			isCanBeConsumed = false
			isCanBeResolved = true
		}
		extends.forEach {
			project.configurations.getByName(it) { extendsFrom(config) }
		}
	}
}