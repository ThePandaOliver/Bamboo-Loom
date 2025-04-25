package dev.pandasystems.bambooloom.extensions

import dev.pandasystems.bambooloom.BambooLoomPlugin
import dev.pandasystems.bambooloom.remapping.LoomRemapper
import dev.pandasystems.bambooloom.utils.downloadFrom
import dev.pandasystems.bambooloom.utils.notExists
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

@Suppress("unused")
fun Project.minecraft(version: String): ConfigurableFileCollection {
	val plugin = BambooLoomPlugin.instances[project]!!
	val meta = plugin.versionMetas[version] ?: throw IllegalArgumentException("Unknown version: $version")

	val clientFile = plugin.loomPaths.libraryCacheDir.resolve("com.mojang.minecraft/minecraft-client-$version.jar").notExists { file ->
		file.downloadFrom(meta.downloads.client.url)

		val mappingJarFile = getIntermediaryJarFile(plugin, version)
		val remapper = LoomRemapper.parseTinyJar(JarFile(mappingJarFile))
		remapper.remap(JarFile(file), file)
	}



	return project.files(clientFile)
}

@Suppress("unused")
fun Project.officialMappings(version: String): ConfigurableFileCollection {
	val plugin = BambooLoomPlugin.instances[project]!!
	val meta = plugin.versionMetas[version] ?: throw IllegalArgumentException("Unknown version: $version")

	// Download the obfuscated to official mappings file
	val officialMappingFile = plugin.loomPaths.mappings.obfuscated2OfficialTxt.downloadFrom(meta.downloads.clientMappings.url)

	// Download the obfuscated to intermediary mappings jar file
	val intermediaryMapping = plugin.loomPaths.mappings.obfuscated2IntermediaryTiny.notExists {
		getIntermediaryJarFile(plugin, version).let { file ->
				JarFile(file).use { jar ->
					val inputStream = jar.getInputStream(jar.getJarEntry("mappings/mappings.tiny"))
					it.writeBytes(inputStream.readBytes())
					inputStream.close()
				}
			}
	}

	// Remappers
	val officialRemapper = LoomRemapper.parseOfficialFile(officialMappingFile).reverse()
	val intermediaryRemapper = LoomRemapper.parseTinyFile(intermediaryMapping)

	val classes = mutableMapOf<String, String>()
	val fields = mutableMapOf<String, String>()
	val methods = mutableMapOf<String, String>()

	// Combine the mappings
	for ((obfuscated, intermediary) in intermediaryRemapper.classes) {
		val official = officialRemapper.classes[obfuscated]
		if (official != null)
			classes[intermediary] = official
	}

	for ((obfuscated, intermediary) in intermediaryRemapper.fields) {
		val (obfuscatedOwner, _/*obfuscatedName*/, obfuscatedDescriptor) = obfuscated.split(".")
		val officialName = officialRemapper.fields[obfuscated]
		val intermediaryOwner = intermediaryRemapper.classes[obfuscatedOwner]
		val intermediaryName = intermediary
		val intermediaryDescriptor = intermediaryRemapper.remapDescriptor(obfuscatedDescriptor)

		if (officialName != null && intermediaryOwner != null)
			fields["$intermediaryOwner.$intermediaryName.$intermediaryDescriptor"] = officialName
	}

	for ((obfuscated, intermediary) in intermediaryRemapper.methods) {
		val (obfuscatedOwner, _/*obfuscatedName*/, obfuscatedDescriptor) = obfuscated.split(".")
		val officialName = officialRemapper.methods[obfuscated]
		val intermediaryOwner = intermediaryRemapper.classes[obfuscatedOwner]
		val intermediaryName = intermediary
		val intermediaryDescriptor = intermediaryRemapper.remapDescriptor(obfuscatedDescriptor)

		if (officialName != null && intermediaryOwner != null)
			methods["$intermediaryOwner.$intermediaryName.$intermediaryDescriptor"] = officialName
	}

	val newRemapper = LoomRemapper(classes, fields, methods)

	// Write the new mappings to a jar file
	val mappingJar = plugin.loomPaths.mappings.intermediary2OfficialJar.notExists { file ->
		JarOutputStream(file.outputStream()).use { jar ->
			// Create manifest
			jar.putNextEntry(JarEntry("META-INF/MANIFEST.MF"))
			jar.write("Manifest-Version: 1.0\n".toByteArray())
			jar.write("Created-By: Bamboo Loom\n".toByteArray())
			jar.closeEntry()

			// Create Mapping file
			jar.putNextEntry(JarEntry("mappings/mappings.tiny"))
			jar.write(newRemapper.serialize().toByteArray())
			jar.closeEntry()
		}
	}

	return project.files(mappingJar)
}

private fun getIntermediaryJarFile(plugin: BambooLoomPlugin, version: String): File {
	return plugin.loomPaths.mappings.obfuscated2IntermediaryJar
		.downloadFrom("https://maven.fabricmc.net/net/fabricmc/intermediary/$version/intermediary-$version-v2.jar")
}