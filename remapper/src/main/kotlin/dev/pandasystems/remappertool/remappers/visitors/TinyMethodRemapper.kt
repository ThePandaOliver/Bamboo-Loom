package dev.pandasystems.remappertool.remappers.visitors

import dev.pandasystems.remappertool.remappers.TinyMappingsRemapper
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.MethodRemapper

class TinyMethodRemapper(
	delegate: MethodVisitor?,
	val access: Int,
	val owner: String,
	val originalMethodName: String,
	val originalDescriptor: String,
	val remapper: TinyMappingsRemapper
) : MethodRemapper(delegate, remapper) {

	override fun visitLocalVariable(
		name: String,
		descriptor: String,
		signature: String?,
		start: Label,
		end: Label,
		index: Int
	) {
		super.visitLocalVariable(
			remapper.mapLocalVariableName(
				owner,
				originalMethodName,
				originalDescriptor,
				index,
				name
			),
			remapper.mapDesc(descriptor),
			remapper.mapSignature(signature, true),
			start,
			end,
			index
		)
	}
}
