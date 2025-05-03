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

	val fields: List<TinyField?> = mutableListOf()
	val methods: List<TinyMethod?> = mutableListOf()
}

data class TinyField(val namespace: Namespace, val name: String, val descriptor: String) {
	val isSource: Boolean get() = namespace.isSource
}

data class TinyMethod(val namespace: Namespace, val name: String, val descriptor: String) {
	val isSource: Boolean get() = namespace.isSource
}

fun layerTinyMapping(
	sourceMapping: TinyMapping,
	layerMapping: TinyMapping
): TinyMapping {
	// 1. Identify the namespace shared by both mappings (the "pivot").
	val pivotNamespace = layerMapping.namespaces.first()
	val pivotIndexInSource = sourceMapping.namespaces.indexOfFirst { it.name == pivotNamespace.name }
	require(pivotIndexInSource != -1) {
		"Source mapping does not contain the pivot namespace '${pivotNamespace.name}'."
	}

	// 2. Compose the resulting namespace list.
	val resultNamespaces = buildList {
		addAll(sourceMapping.namespaces.take(pivotIndexInSource + 1)) // include pivot
		addAll(layerMapping.namespaces.drop(1))                       // exclude duplicate pivot
	}

	// 3. Index layer mapping rows by their class name in the pivot namespace.
	val layerIndex: Map<String, List<TinyClass?>> = buildMap {
		for (layerRow in layerMapping.classes) {
			val pivotClass = layerRow.firstOrNull()   // 0 == pivot in layer mapping
			if (pivotClass != null) put(pivotClass.name, layerRow)
		}
	}

	val combinedClasses = mutableListOf<List<TinyClass?>>()

	// 3a. Merge rows that exist in the source mapping.
	for (sourceRow in sourceMapping.classes) {
		val newRow = MutableList<TinyClass?>(resultNamespaces.size) { null }

		// Copy everything that exists up to and including the pivot column.
		for (i in 0..pivotIndexInSource) {
			if (i < sourceRow.size) newRow[i] = sourceRow[i]
		}

		// Look-up layer row that matches on the pivot class name.
		val pivotClass = sourceRow.getOrNull(pivotIndexInSource)
		val matchingLayerRow = pivotClass?.let { layerIndex[it.name] }

		// Copy additional columns from the layer row (skipping its own pivot column).
		matchingLayerRow?.let { row ->
			for (j in 1 until layerMapping.namespaces.size) {
				val targetIndex = pivotIndexInSource + j
				if (targetIndex < newRow.size) {
					newRow[targetIndex] = row[j]
				}
			}
		}

		combinedClasses.add(newRow)
	}

	// 3b. Add rows that are present only in the layer mapping.
	for ((className, layerRow) in layerIndex) {
		val alreadyPresent = combinedClasses.any { row ->
			val cls = row.getOrNull(pivotIndexInSource)
			cls?.name == className
		}
		if (alreadyPresent) continue

		val newRow = MutableList<TinyClass?>(resultNamespaces.size) { null }
		// layerRow already starts with pivot column; prepend nulls to align.
		for (j in layerRow.indices) {
			val targetIndex = pivotIndexInSource + j
			if (targetIndex < newRow.size) {
				newRow[targetIndex] = layerRow[j]
			}
		}
		combinedClasses.add(newRow)
	}

	return TinyMapping(
		namespaces = resultNamespaces,
		classes = combinedClasses
	)
}
