package dev.pandasystems.bambooloom.utils

import java.io.File
import java.net.URI

object IOUtils {
	fun downloadFileTo(url: String, outputFile: File, overwrite: Boolean = false): File {
		if (outputFile.exists() && !overwrite) {
			return outputFile
		}

		if (!outputFile.parentFile.exists()) {
			outputFile.parentFile.mkdirs()
		}

		URI(url).toURL().openStream().use { input ->
			outputFile.outputStream().use { output ->
				input.copyTo(output)
			}
		}
		return outputFile
	}
}