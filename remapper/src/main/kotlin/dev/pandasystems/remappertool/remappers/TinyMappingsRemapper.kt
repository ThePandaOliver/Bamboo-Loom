package dev.pandasystems.remappertool.remappers

import dev.pandasystems.remappertool.data.FieldMapping
import dev.pandasystems.remappertool.data.MethodMapping
import dev.pandasystems.remappertool.data.TinyMappings
import org.objectweb.asm.commons.Remapper

class TinyMappingsRemapper(
	mappings: TinyMappings,
	fromNamespace: String,
	toNamespace: String
) : Remapper() {
	private val classes = mappings.content.associate { it.getName(fromNamespace) to it.getName(toNamespace) }
	private val fields = mappings.content.map { classMapping ->
		val className = classMapping.getName(fromNamespace)
		classMapping.fields.mapNotNull { fieldMapping ->
			val key = "$className.${fieldMapping.names[fromNamespace] ?: return@mapNotNull null}"
			val value = fieldMapping.names[toNamespace]?.split(".")?.first() ?: return@mapNotNull null
			key to value
		}
	}.flatten().toMap()
	private val methods = mappings.content.map { classMapping ->
		val className = classMapping.getName(fromNamespace)
		classMapping.methods.mapNotNull { methodMapping ->
			val key = "$className.${methodMapping.names[fromNamespace] ?: return@mapNotNull null}"
			val value = methodMapping.names[toNamespace]?.split(".")?.first() ?: return@mapNotNull null
			key to value
		}
	}.flatten().toMap()
	private val localVariables = mappings.content.map { classMapping ->
		val className = classMapping.getName(fromNamespace)
		classMapping.methods.map { methodMapping ->
			val methodName = methodMapping.getName(fromNamespace)
			methodMapping.parameters.mapIndexedNotNull { paramIndex, paramMapping ->
				val key = "$className.$methodName.${paramMapping.names[fromNamespace] ?: return@mapIndexedNotNull null}"
				val value = paramMapping.names[toNamespace]?.split(".")?.first() ?: return@mapIndexedNotNull null
				key to value
			}
		}.flatten()
	}.flatten().toMap()

	override fun map(internalName: String): String {
		return classes[internalName] ?: internalName
	}

	override fun mapFieldName(owner: String, name: String, descriptor: String): String {
		return fields["$owner.$name.$descriptor"] ?: name
	}

	override fun mapMethodName(owner: String, name: String, descriptor: String): String {
		return methods["$owner.$name.$descriptor"] ?: name
	}

	fun mapLocalVariableName(owner: String, method: String, descriptor: String, slot: Int, fallback: String): String {
		return localVariables["$owner.$method.$descriptor.$slot"] ?: fallback
	}
}