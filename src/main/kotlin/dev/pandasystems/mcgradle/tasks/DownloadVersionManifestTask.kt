package dev.pandasystems.mcgradle.tasks

import dev.pandasystems.mcgradle.Constants
import dev.pandasystems.mcgradle.utils.downloadFileToWithCache
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.net.URI

abstract class DownloadVersionManifestTask : DefaultTask() {
	init {
		group = "MC Gradle"
		description = "Downloads the Minecraft version manifest file"
	}

	@TaskAction
	fun downloadManifest() {
		val outputFile = Constants.cacheVersionsPath(project).resolve("version_manifest.json")
		downloadFileToWithCache(URI(Constants.VERSION_MANIFEST_URL).toURL(), outputFile, true)
	}
}