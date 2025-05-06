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
	override fun visit(
		version: Int,
		access: Int,
		name: String,
		signature: String?,
		superName: String?,
		interfaces: Array<out String>
	) {
		val json = JsonObject()
		json.addProperty("visitType", "class")
		json.addProperty("version", version)
		json.addProperty("access", access)
		json.addProperty("name", name)
		json.addProperty("signature", signature)
		json.addProperty("superName", superName)
		json.addProperty("interfaces", interfaces.joinToString(","))
		println(json.toString())
		super.visit(
			version,
			access,
			remapper.mapClassName(name),
			signature,
			superName?.let { remapper.mapClassName(it) },
			interfaces.map { remapper.mapClassName(it) }.toTypedArray()
		)
	}

	override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) {
		val json = JsonObject()
		json.addProperty("visitType", "innerClass")
		json.addProperty("name", name)
		json.addProperty("outerName", outerName)
		json.addProperty("innerName", innerName)
		json.addProperty("access", access)
		println(json.toString())
		super.visitInnerClass(name, outerName, innerName, access)
	}

	override fun visitOuterClass(owner: String?, name: String?, descriptor: String?) {
		val json = JsonObject()
		json.addProperty("visitType", "outerClass")
		json.addProperty("owner", owner)
		json.addProperty("name", name)
		json.addProperty("descriptor", descriptor)
		println(json.toString())
		super.visitOuterClass(owner, name, descriptor)
	}

	override fun visitNestHost(nestHost: String?) {
		val json = JsonObject()
		json.addProperty("visitType", "nestHost")
		json.addProperty("nestHost", nestHost)
		println(json.toString())
		super.visitNestHost(nestHost)
	}

	override fun visitModule(name: String?, access: Int, version: String?): ModuleVisitor? {
		val json = JsonObject()
		json.addProperty("visitType", "module")
		json.addProperty("name", name)
		json.addProperty("access", access)
		json.addProperty("version", version)
		println(json.toString())
		return super.visitModule(name, access, version)
	}

	override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
		val json = JsonObject()
		json.addProperty("visitType", "annotation")
		json.addProperty("descriptor", descriptor)
		json.addProperty("visible", visible)
		println(json.toString())
		return super.visitAnnotation(descriptor, visible)
	}

	override fun visitAttribute(attribute: Attribute?) {
		val json = JsonObject()
		json.addProperty("visitType", "attribute")
		json.addProperty("attribute", attribute?.toString())
		println(json.toString())
		super.visitAttribute(attribute)
	}

	override fun visitEnd() {
		println("visitEnd")
		super.visitEnd()
	}

	override fun visitField(access: Int, name: String?, descriptor: String?, signature: String?, value: Any?): FieldVisitor? {
		val json = JsonObject()
		json.addProperty("visitType", "field")
		json.addProperty("access", access)
		json.addProperty("name", name)
		json.addProperty("descriptor", descriptor)
		json.addProperty("signature", signature)
		json.addProperty("value", value.toString())
		println(json.toString())
		return super.visitField(access, name, descriptor, signature, value)
	}

	override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String?>?): MethodVisitor? {
		val json = JsonObject()
		json.addProperty("visitType", "method")
		json.addProperty("access", access)
		json.addProperty("name", name)
		json.addProperty("descriptor", descriptor)
		json.addProperty("signature", signature)
		json.addProperty("exceptions", exceptions?.joinToString(","))
		println(json.toString())
		return super.visitMethod(access, name, descriptor, signature, exceptions)
	}

	override fun visitNestMember(nestMember: String?) {
		val json = JsonObject()
		json.addProperty("visitType", "nestMember")
		json.addProperty("nestMember", nestMember)
		println(json.toString())
		super.visitNestMember(nestMember)
	}

	override fun visitPermittedSubclass(permittedSubclass: String?) {
		val json = JsonObject()
		json.addProperty("visitType", "permittedSubclass")
		json.addProperty("permittedSubclass", permittedSubclass)
		println(json.toString())
		super.visitPermittedSubclass(permittedSubclass)
	}

	override fun visitRecordComponent(name: String?, descriptor: String?, signature: String?): RecordComponentVisitor? {
		val json = JsonObject()
		json.addProperty("visitType", "recordComponent")
		json.addProperty("name", name)
		json.addProperty("descriptor", descriptor)
		json.addProperty("signature", signature)
		println(json.toString())
		return super.visitRecordComponent(name, descriptor, signature)
	}

	override fun visitSource(source: String?, debug: String?) {
		val json = JsonObject()
		json.addProperty("visitType", "source")
		json.addProperty("source", source)
		json.addProperty("debug", debug)
		println(json.toString())
		super.visitSource(source, debug)
	}

	override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean): AnnotationVisitor? {
		val json = JsonObject()
		json.addProperty("visitType", "typeAnnotation")
		json.addProperty("typeRef", typeRef)
		json.addProperty("typePath", typePath?.toString())
		json.addProperty("descriptor", descriptor)
		json.addProperty("visible", visible)
		println(json.toString())
		return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible)
	}
}