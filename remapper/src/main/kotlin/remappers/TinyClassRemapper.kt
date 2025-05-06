package remappers

import com.google.gson.JsonObject
import dev.pandasystems.remappers.HierarchyAwareClassVisitor
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.ASM9

class TinyClassRemapper(
	val classVisitor: HierarchyAwareClassVisitor,
	delegate: ClassVisitor,
	val remapper: TinyMappingsRemapper
) : ClassVisitor(ASM9, delegate) {
	lateinit var visitingClassName: String

	override fun visit(
		version: Int,
		access: Int,
		name: String,
		signature: String?,
		superName: String?,
		interfaces: Array<out String>
	) {
		visitingClassName = name
		super.visit(
			version,
			access,
			remapper.mapClassName(name),
			signature,
			superName?.let { remapper.mapClassName(it) },
			interfaces.map { remapper.mapClassName(it) }.toTypedArray()
		)
	}

	override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
		val realOwner = classVisitor.getRealOwnerOfMethod(visitingClassName, name, descriptor)
		return super.visitMethod(access, remapper.mapMethodName(realOwner, name, descriptor), descriptor, signature, exceptions)
	}
}