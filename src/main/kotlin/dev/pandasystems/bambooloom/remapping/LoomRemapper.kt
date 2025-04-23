package dev.pandasystems.bambooloom.remapping

import dev.pandasystems.bambooloom.data.Mapping
import org.objectweb.asm.commons.Remapper

@Deprecated("Use V2 instead")
class LoomRemapper(private val mapping: Mapping) : Remapper() {
	override fun map(internalName: String): String {
		return mapping[internalName]?.to ?: internalName
	}

	override fun mapFieldName(owner: String, name: String, descriptor: String): String {
		return mapping[owner]?.getFieldName(name, descriptor) ?: mapping[owner]?.getFieldName(name) ?: name
	}

	override fun mapMethodName(owner: String, name: String, descriptor: String): String {
		return mapping[owner]?.getMethodName(name, descriptor) ?: mapping[owner]?.getMethodName(name) ?: name
	}
}