package dev.pandasystems.remappertool.remappers.visitors

import dev.pandasystems.remappertool.remappers.TinyMappingsRemapper
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.ClassRemapper

class TinyClassRemapper(
	val classVisitor: HierarchyAwareClassVisitor,
	delegate: ClassVisitor,
	val remapper: TinyMappingsRemapper
) : ClassRemapper(delegate, remapper) {
	override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
		val realOwner = classVisitor.getRealOwnerOfMethod(className, name, descriptor)
		val methodVisitor = super.visitMethod(
			access,
			remapper.mapMethodName(realOwner, name, descriptor),
			remapper.mapMethodDesc(descriptor),
			remapper.mapSignature(signature, false),
			exceptions?.let(remapper::mapTypes)
		)
		return createMethodRemapper(methodVisitor)
	}
}