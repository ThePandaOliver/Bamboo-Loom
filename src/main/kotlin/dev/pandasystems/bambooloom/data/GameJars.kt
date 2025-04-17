package dev.pandasystems.bambooloom.data

import dev.pandasystems.bambooloom.utils.LoomPaths
import dev.pandasystems.bambooloom.utils.Remapper
import dev.pandasystems.bambooloom.utils.downloadFrom
import org.gradle.api.Project
import java.io.File
import java.net.URI
import java.util.jar.JarFile

class GameJars(data: VersionData, project: Project) {
	val clientJarFile: File
	val serverJarFile: File

	init {
		val clientFile = LoomPaths.versionJarsDir(project, data.id).resolve("unmodified/client.jar")
		val serverFile = LoomPaths.versionJarsDir(project, data.id).resolve("unmodified/server.jar")

		// Download client jar
		clientFile.downloadFrom(URI(data.downloads.clientJar.url).toURL())

		// Download and Extract Server jar
		if (!serverFile.exists()) {
			val unextractedServerFile = LoomPaths.versionJarsDir(project, data.id).resolve("unmodified/unextracted_server.jar")
			unextractedServerFile.downloadFrom(URI(data.downloads.serverJar.url).toURL())

			// Extract server jar
			JarFile(unextractedServerFile).use { jarFile ->
				jarFile.getJarEntry("META-INF/versions/${data.id}/server-${data.id}.jar")?.let { entry ->
					jarFile.getInputStream(entry).use { inputStream ->
						serverFile.outputStream().use { outputStream ->
							outputStream.write(inputStream.readBytes())
						}
					}
				}
			}
		}

		// Map client and server with intermediary mappings
		val clientRemapper = Remapper.intermediary(project, data.id, clientFile)
		val serverRemapper = Remapper.intermediary(project, data.id, serverFile)

		clientJarFile = clientRemapper.outputFile
		serverJarFile = serverRemapper.outputFile
	}
}