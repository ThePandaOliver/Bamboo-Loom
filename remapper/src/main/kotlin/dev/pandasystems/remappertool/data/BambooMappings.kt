package dev.pandasystems.remappertool.data

data class BambooMappings(
	val version: String,
	val classes: List<BambooClass>
)

data class BambooClass(
	val names: Map<String, String>,
	
	val fields: List<BambooField>,
	val methods: List<BambooMethod>,

	val comment: Map<String, String>
)

data class BambooField(
	val names: Map<String, String>,
	val descriptor: Map<String, String>,

	val comment: Map<String, String>
)

data class BambooMethod(
	val names: Map<String, String>,
	val descriptor: Map<String, String>,
	
	val parameters: Map<Int, BambooParameter>,
	
	val comment: Map<String, String>
)

data class BambooParameter(
	val names: Map<String, String>
)