package dev.pandasystems.bambooloom.remapping

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper

class LoomClassRemapper(classVisitor: ClassVisitor, remapper: Remapper) : ClassRemapper(classVisitor, remapper) {
	override fun visitMethod(
		access: Int,
		name: String?,
		descriptor: String?,
		signature: String?,
		exceptions: Array<out String?>?
	): MethodVisitor? {
		val originalMethodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
		if (originalMethodVisitor == null) {
			return null
		}
		// Wrap the original MethodVisitor with CustomMethodVisitor
		return createMethodRemapper(LoomMethodVisitor(this.api, originalMethodVisitor))
	}
}