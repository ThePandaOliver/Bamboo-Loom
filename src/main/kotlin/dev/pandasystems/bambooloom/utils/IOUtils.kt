package dev.pandasystems.bambooloom.utils

import java.io.File
import java.net.URL

fun File.downloadFrom(url: URL, overwrite: Boolean = false): File {
	if (this.exists() && !overwrite) {
		return this
	}

	if (!this.parentFile.exists()) {
		this.parentFile.mkdirs()
	}

	url.openStream().use { input ->
		this.outputStream().use { output ->
			input.copyTo(output)
		}
	}
	return this
}