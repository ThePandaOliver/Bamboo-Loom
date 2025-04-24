package dev.pandasystems.bambooloom.remapping

import java.io.File
import java.util.jar.JarFile
import kotlin.io.path.createTempFile

@Deprecated("Use LoomRemapperV2.remap(jarFile) instead")
class RemapperToolV2(private val remapper: LoomRemapperV2) {
	fun remap(jarFile: File) {
		val tempFile = createTempFile(jarFile.parentFile.toPath(), jarFile.nameWithoutExtension).toFile()
		remapper.remap(JarFile(jarFile), tempFile)
		tempFile.copyTo(jarFile, true)
		tempFile.delete()
	}
}