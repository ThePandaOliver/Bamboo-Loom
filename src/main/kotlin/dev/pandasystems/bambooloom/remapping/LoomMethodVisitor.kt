package dev.pandasystems.bambooloom.remapping

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import java.util.concurrent.ConcurrentHashMap

class LoomMethodVisitor(api: Int, methodVisitor: MethodVisitor) : MethodVisitor(api, methodVisitor) {
	private val variableCountMap = ConcurrentHashMap<String, Int>()

	override fun visitLocalVariable(
		name: String?,
		descriptor: String?,
		signature: String?,
		start: Label?,
		end: Label?,
		index: Int
	) {
		if (name == "this")
			super.visitLocalVariable(name, descriptor, signature, start, end, index)

		val newName = if (descriptor != null) {
			mapDescriptorToName(descriptor).let {
				val count = variableCountMap.merge(it, 1) { a, b -> a + b }
				if ((count ?: 0) > 1)
					it + count
				else
					it
			}
		} else name
		super.visitLocalVariable(newName, descriptor, signature, start, end, index)
	}

	private fun mapDescriptorToName(descriptor: String): String {
		return when (descriptor) {
			"Z" -> "bool" // boolean
			"B" -> "byte"
			"C" -> "char"
			"D" -> "d"    // double
			"F" -> "f"    // float
			"I" -> "i"    // int
			"J" -> "l"    // long
			"S" -> "s"    // short
			"V" -> "void" // void doesn't apply here, but for completeness
			else -> {
				if (descriptor.startsWith("L") && descriptor.endsWith(";")) { // This means it's a class
					// Extract the class name from the descriptor, e.g., `Ljava/lang/String;` -> `string`
					descriptor.substring(1, descriptor.length - 1).substringAfterLast('/').replaceFirstChar { it.lowercaseChar() }
				} else if (descriptor.startsWith("[")) {
					// Handle arrays, e.g., `[I` -> `iArray`, `[Ljava/lang/String;` -> `stringArray`
					val elementType = mapDescriptorToName(descriptor.substring(1))
					"${elementType}Array"
				} else {
					"obj" // Fallback for unknown descriptors
				}
			}
		}
	}

}