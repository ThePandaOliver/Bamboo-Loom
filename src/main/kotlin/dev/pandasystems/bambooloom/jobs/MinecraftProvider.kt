package dev.pandasystems.bambooloom.jobs

import org.gradle.api.Project
import javax.inject.Inject

abstract class MinecraftProvider : Runnable {
	@Inject
	lateinit var project: Project

	val clientJar = {

	}

	override fun run() {

	}
}