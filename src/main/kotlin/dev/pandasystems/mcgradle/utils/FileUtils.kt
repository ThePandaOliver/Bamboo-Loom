package dev.pandasystems.mcgradle.utils

import dev.pandasystems.mcgradle.Constants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

val logger: Logger = LoggerFactory.getLogger("FileUtils")

fun downloadFileTo(url: URL, output: File, forceDownload: Boolean = false) {
	if (forceDownload && output.exists())
		return

	output.parentFile.mkdirs()
	url.openStream().use { inputStream ->
		BufferedInputStream(inputStream).use { bufferedInputStream ->
			FileOutputStream(output).use { fileOutputStream ->
				bufferedInputStream.copyTo(fileOutputStream)
			}
		}
	}
}

fun downloadFileToWithCache(url: URL, output: File, alwaysDownload: Boolean = false) {
	if (output.exists() && output.isFile && System.currentTimeMillis() - output.lastModified() < Constants.FILE_EXPIRATION_TIME)
		return

	try {
		downloadFileTo(url, output, alwaysDownload)
	} catch (e: Exception) {
		logger.warn("Failed to download version manifest", e)
		if (output.exists())
			throw e
	}
}