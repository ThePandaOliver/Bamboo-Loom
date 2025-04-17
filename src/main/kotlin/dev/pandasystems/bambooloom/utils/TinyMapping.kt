package dev.pandasystems.bambooloom.utils

import java.io.File

class TinyMapping(
	tinyFile: File,
) {
	val classes: List<Class> = mutableListOf()

	init {
		val lineIterator = tinyFile.readLines().iterator()
		var current: Class? = null
		while (lineIterator.hasNext()) {
			val line = lineIterator.next().trim().split("\\s+".toRegex())

			// Interpret the class line
			if (line.first() == "c") {
				current = Class(
					oldName = line[1],
					newName = line[2],
				)
				classes + current
			}

			if (current == null)
				continue

			// Interpret the field line
			if (line.first() == "f") {
				current.fields + Field(
					descriptor = line[1],
					oldName = line[2],
					newName = line[3],
				)
			}

			// Interpret the method line
			if (line.first() == "m") {
				current.methods + Method(
					descriptor = line[1],
					oldName = line[2],
					newName = line[3],
				)
			}
		}
	}

	class Class(
		val oldName: String,
		val newName: String
	) {
		val fields: List<Field> = mutableListOf()
		val methods: List<Method> = mutableListOf()
	}

	class Field(
		val descriptor: String,
		val oldName: String,
		val newName: String,
	)

	class Method(
		val descriptor: String,
		val oldName: String,
		val newName: String,
	)
}