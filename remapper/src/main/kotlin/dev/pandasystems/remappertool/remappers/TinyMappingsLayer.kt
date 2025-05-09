package dev.pandasystems.remappertool.remappers

import dev.pandasystems.remappertool.data.ClassMapping
import dev.pandasystems.remappertool.data.FieldMapping
import dev.pandasystems.remappertool.data.MethodMapping
import dev.pandasystems.remappertool.data.MethodParameterMapping
import dev.pandasystems.remappertool.data.TinyHeader
import dev.pandasystems.remappertool.data.TinyMappings

fun TinyMappings.createLayered(layer: TinyMappings): TinyMappings {
	val namespaces = (this.header.namespaces + layer.header.namespaces).distinct()
	val matchingNamespace: String = requireNotNull(this.header.namespaces.find { it == layer.header.namespaces.first() }) { "No matching namespace found for layering!" }

	val oldContent = layer.content.toMutableList()
	val newContent = mutableListOf<ClassMapping>()

	for (mapping in this.content) {
		val layerMapping = oldContent.find { it.names[matchingNamespace] == mapping.names[matchingNamespace] } ?: continue
		oldContent.remove(layerMapping)

		newContent += ClassMapping(
			names = mapping.names,
			fields = mapping.fields,
			methods = mapping.methods,
		)
	}
	oldContent.forEach { layerClass -> newContent += layerClass}
	
	return TinyMappings(
		header = TinyHeader(this.header.majorVersion, this.header.minorVersion, namespaces),
		content = layerClasses(matchingNamespace, this.content, layer.content)
	)
}

private fun layerClasses(sourceNamespace: String, classList: List<ClassMapping>, layerClassList: List<ClassMapping>): List<ClassMapping> {
	val result = mutableListOf<ClassMapping>()
	val tempLayerClassList = layerClassList.toMutableList()
	for (mapping in classList) {
		val layerMapping = tempLayerClassList.find { it.names[sourceNamespace] == mapping.names[sourceNamespace] } ?: continue
		tempLayerClassList.remove(layerMapping)

		result += ClassMapping(
			names = mapping.names.toMutableMap() + layerMapping.names.toMutableMap().filter { !mapping.names.containsKey(it.key) },
			fields = layerFields(sourceNamespace, mapping.fields, layerMapping.fields),
			methods = layerMethods(sourceNamespace, mapping.methods, layerMapping.methods),
		)
	}
	tempLayerClassList.forEach { layerClass -> result += layerClass}
	return result
}

private fun layerFields(sourceNamespace: String, fieldList: List<FieldMapping>, layerFieldList: List<FieldMapping>): List<FieldMapping> {
	val result = mutableListOf<FieldMapping>()
	val tempLayerFieldList = layerFieldList.toMutableList()
	for (mapping in fieldList) {
		val layerMapping = tempLayerFieldList.find { it.names[sourceNamespace] == mapping.names[sourceNamespace] } ?: continue
		tempLayerFieldList.remove(layerMapping)

		result += FieldMapping(
			names = mapping.names.toMutableMap() + layerMapping.names.toMutableMap().filter { !mapping.names.containsKey(it.key) },
		)
	}
	tempLayerFieldList.forEach { layerField -> result += layerField}
	return result
}

private fun layerMethods(sourceNamespace: String, methodList: List<MethodMapping>, layerMethodList: List<MethodMapping>): List<MethodMapping> {
	val result = mutableListOf<MethodMapping>()
	val tempLayerMethodList = layerMethodList.toMutableList()
	for (mapping in methodList) {
		val layerMapping = tempLayerMethodList.find { it.names[sourceNamespace] == mapping.names[sourceNamespace] } ?: continue
		tempLayerMethodList.remove(layerMapping)

		result += MethodMapping(
			names = mapping.names.toMutableMap() + layerMapping.names.toMutableMap().filter { !mapping.names.containsKey(it.key) },
			parameters = layerMethodParameters(sourceNamespace, mapping.parameters, layerMapping.parameters),
		)
	}
	tempLayerMethodList.forEach { layerMethod -> result += layerMethod}
	return result
}

private fun layerMethodParameters(sourceNamespace: String, paramList: List<MethodParameterMapping>, layerParamList: List<MethodParameterMapping>): List<MethodParameterMapping> {
	val result = mutableListOf<MethodParameterMapping>()
	val tempLayerParamList = layerParamList.toMutableList()
	for (mapping in paramList) {
		val layerMapping = tempLayerParamList.find { it.names[sourceNamespace] == mapping.names[sourceNamespace] } ?: continue
		tempLayerParamList.remove(layerMapping)

		result += MethodParameterMapping(
			names = mapping.names.toMutableMap() + layerMapping.names.toMutableMap().filter { !mapping.names.containsKey(it.key) },
		)
	}
	tempLayerParamList.forEach { layerParam -> result += layerParam}
	return result
}