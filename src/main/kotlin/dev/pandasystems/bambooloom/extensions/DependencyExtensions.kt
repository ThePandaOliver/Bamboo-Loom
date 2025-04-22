package dev.pandasystems.bambooloom.extensions

import dev.pandasystems.bambooloom.BambooLoomPlugin
import dev.pandasystems.bambooloom.data.Mapping
import dev.pandasystems.bambooloom.utils.downloadFrom
import dev.pandasystems.bambooloom.utils.notExists
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

fun Project.minecraft(version: String): ConfigurableFileCollection {
	val plugin = BambooLoomPlugin.instances[project]!!
	val meta = plugin.versionMetas[version] ?: throw IllegalArgumentException("Unknown version: $version")

	val clientFile = plugin.loomPaths.mojangLibraryCacheDir.resolve("minecraft/minecraft-client-$version.jar").notExists { file ->
		file.downloadFrom(meta.downloads.client.url)

//		val mapping = plugin.loomPaths.mappingDir.resolve("minecraft-client-$version-official.txt")
//			.downloadFrom(meta.downloads.clientMappings.url).let { mappingFile ->
//				Mapping.parseOfficial(mappingFile.readText())
//			}

//		RemapperTool(mapping).remap(file)
	}

	return files(clientFile)
}

fun Project.officialMappings(version: String): ConfigurableFileCollection {
	val plugin = BambooLoomPlugin.instances[project]!!
	val meta = plugin.versionMetas[version] ?: throw IllegalArgumentException("Unknown version: $version")

	val officialMappingFile = plugin.loomPaths.mappingDir.resolve("minecraft-client-$version-official.txt").downloadFrom(meta.downloads.clientMappings.url)

	val intermediaryFile = plugin.loomPaths.mappingDir.resolve("minecraft-client-$version-intermediary.tiny").notExists {
		plugin.loomPaths.mappingDir.resolve("minecraft-client-$version-intermediary.jar")
			.downloadFrom(meta.downloads.clientMappings.url).let { file ->
				JarFile(file).use { jar ->
					val inputStream = jar.getInputStream(jar.getJarEntry("mappings/mappings.tiny"))
					it.writeBytes(inputStream.readBytes())
					inputStream.close()
				}
			}
	}

	val mappingJar = plugin.loomPaths.mappingDir.resolve("minecraft-client-$version-official.jar").notExists { file ->
		JarOutputStream(file.outputStream()).use { jar ->
			// Create manifest
			jar.putNextEntry(JarEntry("META-INF/MANIFEST.MF"))
			jar.write("Manifest-Version: 1.0\n".toByteArray())
			jar.write("Created-By: Bamboo Loom\n".toByteArray())
			jar.closeEntry()

			// Create Mapping file
			jar.putNextEntry(JarEntry("mappings/mappings.tiny"))
//			jar.write(mapping.toString().toByteArray())
			jar.closeEntry()
		}
	}

	return files(mappingJar)
}