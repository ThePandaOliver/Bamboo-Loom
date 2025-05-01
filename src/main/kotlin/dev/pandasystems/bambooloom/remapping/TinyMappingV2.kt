package dev.pandasystems.bambooloom.remapping

data class TinyMapping(
	val namespaces: List<Namespace>,
	val classes: List<List<TinyClass?>>
)

data class Namespace(val name: String, val source: Namespace?) {
	val isSource: Boolean get() = source != null
}

data class TinyClass(
	val namespace: Namespace,
	val name: String
) {
	val isSource: Boolean get() = namespace.isSource

	val fields: List<TinyField> = mutableListOf()
	val methods: List<TinyMethod> = mutableListOf()
}

data class TinyField(val namespace: Namespace, val name: String, val descriptor: String) {
	val isSource: Boolean get() = namespace.isSource
}

data class TinyMethod(val namespace: Namespace, val name: String, val descriptor: String) {
	val isSource: Boolean get() = namespace.isSource
}