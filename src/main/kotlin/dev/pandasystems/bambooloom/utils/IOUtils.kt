package dev.pandasystems.bambooloom.utils

import java.io.File
import java.net.URI
import java.net.URL

// Todo: Revert overwrite default to false
fun File.downloadFrom(url: String, overwrite: Boolean = true): File = downloadFrom(URI(url).toURL(), overwrite)

fun File.downloadFrom(url: URL, overwrite: Boolean = true): File {
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
	// Todo: Revert
	consumer(this)
//	if (!this.exists()) {
//		consumer(this)
//	}
	return this
}