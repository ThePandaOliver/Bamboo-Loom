package dev.pandasystems.bambooloom.extensions

import dev.pandasystems.bambooloom.BambooLoomPlugin
import dev.pandasystems.bambooloom.remapping.LoomRemapperV2
import dev.pandasystems.bambooloom.remapping.RemapperToolV2
import dev.pandasystems.bambooloom.utils.downloadFrom
import dev.pandasystems.bambooloom.utils.notExists
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.sequences.forEach

fun Project.minecraft(version: String): ConfigurableFileCollection {
	val plugin = BambooLoomPlugin.instances[project]!!
	val meta = plugin.versionMetas[version] ?: throw IllegalArgumentException("Unknown version: $version")

	val clientFile = plugin.loomPaths.mojangLibraryCacheDir.resolve("minecraft/minecraft-client-$version.jar").notExists { file ->
		file.downloadFrom(meta.downloads.client.url)

		val mappingJarFile = getIntermediaryJarFile(plugin, version)
		val mapping = LoomRemapperV2.parseTinyJar(JarFile(mappingJarFile))
		RemapperToolV2(mapping).remap(file)
	}

	return files(clientFile)
}

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
	}.readText()

	// Parse the official mappings to hashmap
	val officialMappings = parseOfficial(officialMappingFile.readText())

	// Create an intermediary to official mappings file in tiny format
	val stringBuilder = StringBuilder()
	stringBuilder.appendLine("tiny\t2\t0\tintermediary\tofficial")

	var currentClass: String? = null
	intermediaryMapping.lines().forEach { line ->
		val parts = line.trim().split("\t".toRegex())

		val type = parts[0]

		when {
			type == "c" -> {
				val oldName = parts[1]
				val intermediaryName = parts[2]
				val mappedName = officialMappings["c.$oldName"]
				currentClass = oldName

				stringBuilder.appendLine("c\t$intermediaryName\t$mappedName")
			}

			type == "f" && currentClass != null -> {
				val descriptor = parts[1]
				val oldName = parts[2]
				val intermediaryName = parts[3]
				val mappedName = officialMappings["f.$currentClass.$oldName"]

				stringBuilder.appendLine("\tf\t$descriptor\t$intermediaryName\t$mappedName")
			}

			type == "m" && currentClass != null -> {
				val descriptor = parts[1]
				val oldName = parts[2]
				val intermediaryName = parts[3]
				val mappedName = officialMappings["m.$currentClass.$oldName"]

				stringBuilder.appendLine("\tm\t$descriptor\t$intermediaryName\t$mappedName")
			}
		}
	}

	// Write the new mappings to a jar file
	val mappingJar = plugin.loomPaths.mappingDir.resolve("minecraft-client-$version-official.jar").notExists { file ->
		JarOutputStream(file.outputStream()).use { jar ->
			// Create manifest
			jar.putNextEntry(JarEntry("META-INF/MANIFEST.MF"))
			jar.write("Manifest-Version: 1.0\n".toByteArray())
			jar.write("Created-By: Bamboo Loom\n".toByteArray())
			jar.closeEntry()

			// Create Mapping file
			jar.putNextEntry(JarEntry("mappings/mappings.tiny"))
			jar.write(stringBuilder.toString().toByteArray())
			jar.closeEntry()
		}
	}

	return files(mappingJar)
}

private fun parseOfficial(text: String): MutableMap<String, String> {
	var currentClass: String? = null
	val map = mutableMapOf<String, String>()

	text.lineSequence().forEach { line ->
		val trimmedLine = line.trim()

		// Skip empty lines and comments that don't contain metadata
		if (trimmedLine.isEmpty() || (trimmedLine.startsWith("#") && !trimmedLine.contains("{"))) {
			return@forEach
		}

		when {
			// Class mapping line (e.g., "com.mojang.blaze3d.Blaze3D -> fib:")
			trimmedLine.contains("->") && trimmedLine.endsWith(":") -> {
				// Parse new class
				val (to, from) = trimmedLine.split("->").map { it.trim().replace('.', '/') }
				currentClass = from.removeSuffix(":")
				map["c.${from.removeSuffix(":")}"] = to
			}

			// Method mapping (e.g., "9:10:void youJustLostTheGame() -> a")
			trimmedLine.contains("->") && !trimmedLine.endsWith(":") -> {
				val parts = trimmedLine.split("->").map { it.trim() }
				val entryName = parts[1]

				// Parse method signature
				val methodParts = parts[0].split(":")
				val lastPart = methodParts.last().substringAfterLast(" ")

				if (lastPart.contains("(") || lastPart.contains(")")) {
					// This is a method
					val mappedName = lastPart.substring(lastPart.lastIndexOf(" ") + 1, lastPart.indexOf("("))
					map["m.$currentClass.$entryName"] = mappedName
				} else {
					// This is a field
					val mappedName = lastPart
					map["f.$currentClass.$entryName"] = mappedName
				}
			}
		}
	}

	return map
}

private fun getIntermediaryJarFile(plugin: BambooLoomPlugin, version: String): File {
	return plugin.loomPaths.mappings.obfuscated2IntermediaryJar
		.downloadFrom("https://maven.fabricmc.net/net/fabricmc/intermediary/$version/intermediary-$version-v2.jar")
}