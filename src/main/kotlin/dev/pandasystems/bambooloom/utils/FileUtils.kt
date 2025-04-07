package dev.pandasystems.bambooloom.utils

import java.io.File
import java.net.URL

fun downloadFileTo(url: URL, output: File) {
	if (!output.parentFile.exists())
		output.parentFile.mkdirs()

	url.openStream().use { input ->
		output.outputStream().use { output ->
			input.copyTo(output)
		}
	}
}