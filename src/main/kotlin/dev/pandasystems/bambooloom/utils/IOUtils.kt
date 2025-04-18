package dev.pandasystems.bambooloom.utils

import java.io.File
import java.net.URI
import java.net.URL

fun File.downloadFrom(url: String, overwrite: Boolean = false): File = downloadFrom(URI(url).toURL(), overwrite)

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

fun File.exists(consumer: (File) -> Unit): File {
	if (this.exists()) {
		consumer(this)
	}
	return this
}

fun File.notExists(consumer: (File) -> Unit): File {
	if (!this.exists()) {
		consumer(this)
	}
	return this
}