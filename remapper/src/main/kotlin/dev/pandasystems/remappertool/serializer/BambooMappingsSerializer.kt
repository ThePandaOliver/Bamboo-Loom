package dev.pandasystems.remappertool.serializer

import com.google.gson.Gson
import dev.pandasystems.remappertool.data.BambooMappings

object BambooMappingsSerializer {
	fun deserialize(string: String): BambooMappings {
		val gson = Gson()
		return gson.fromJson(string, BambooMappings::class.java)
	}
	
	fun serialize(mappings: BambooMappings): String {
		val gson = Gson()
		return gson.toJson(mappings)
	}
}