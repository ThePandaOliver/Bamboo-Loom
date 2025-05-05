package dev.pandasystems.mapping

data class TinyMappings(
	val header: TinyHeader,
	val content: List<ClassMapping>
) {
	fun findClass(namespace: String, name: String): ClassMapping? {
		return content.find { it.names[namespace] == name }
	}
}

data class TinyHeader(
	val majorVersion: Int,
	val minorVersion: Int,
	val namespaces: List<String>
)

data class ClassMapping(
	/**
	 * Namespace -> name
	 */
	val names: Map<String, String>,

	val fields: List<FieldMapping>,
	val methods: List<MethodMapping>
) {
	fun getName(toNamespace: String): String {
		return names[toNamespace] ?: names.values.last()
	}

	fun findField(namespace: String, name: String): FieldMapping? {
		return fields.find { it.names[namespace] == name }
	}

	fun findMethod(namespace: String, name: String): MethodMapping? {
		return methods.find { it.names[namespace] == name }
	}
}

data class FieldMapping(
	/**
	 * Namespace -> name
	 */
	val names: Map<String, String>
) {
	fun getName(toNamespace: String): String {
		return names[toNamespace] ?: names.values.last()
	}
}

data class MethodMapping(
	/**
	 * Namespace -> name
	 */
	val names: Map<String, String>
) {
	fun getName(toNamespace: String): String {
		return names[toNamespace] ?: names.values.last()
	}
}