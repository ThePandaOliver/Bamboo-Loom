package dev.pandasystems.remappertool.data

data class TinyMappings(
	val header: TinyHeader,
	val content: List<ClassMapping>
)

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
	fun getName(toNamespace: String): String? {
		return names[toNamespace]
	}
}

data class FieldMapping(
	/**
	 * Namespace -> name
	 */
	val names: Map<String, String>
) {
	@Deprecated("Use the names variable instead", ReplaceWith("names[toNamespace]"))
	fun getName(toNamespace: String): String? {
		return names[toNamespace]
	}
}

data class MethodMapping(
	/**
	 * Namespace -> name
	 */
	val names: Map<String, String>,

	val parameters: List<MethodParameterMapping>
) {
	fun getName(toNamespace: String): String? {
		return names[toNamespace]
	}
}

data class MethodParameterMapping(
	/**
	 * Namespace -> name
	 */
	val names: Map<String, String>
) {
	fun getName(toNamespace: String): String? {
		return names[toNamespace]
	}
}