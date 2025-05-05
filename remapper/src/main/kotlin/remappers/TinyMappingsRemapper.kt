package remappers

import dev.pandasystems.mapping.TinyMappings

class TinyMappingsRemapper(
	mappings: TinyMappings,
	fromNamespace: String,
	toNamespace: String
) {
	private val classes = mappings.content.associate { it.getName(fromNamespace) to it.getName(toNamespace) }
	private val fields = mappings.content.map { classMapping ->
		val className = classMapping.getName(fromNamespace)
		classMapping.fields.map { "$className.${it.getName(fromNamespace)}" to it.getName(toNamespace).split(".").first() }
	}.flatten().toMap()
	private val methods = mappings.content.map { classMapping ->
		val className = classMapping.getName(fromNamespace)
		classMapping.methods.map { "$className.${it.getName(fromNamespace)}" to it.getName(toNamespace).split(".").first() }
	}.flatten().toMap()

	fun mapClassName(internalName: String): String {
		return classes[internalName] ?: internalName
	}

	fun mapFieldName(owner: String, name: String, descriptor: String): String {
		return fields["$owner.$name.$descriptor"] ?: name
	}

	fun mapMethodName(owner: String, name: String, descriptor: String): String {
		return methods["$owner.$name.$descriptor"] ?: name
	}
}