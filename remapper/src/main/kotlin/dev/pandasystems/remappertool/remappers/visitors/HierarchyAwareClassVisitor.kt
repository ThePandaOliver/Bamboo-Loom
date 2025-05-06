package dev.pandasystems.remappertool.remappers.visitors

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.util.LinkedList
import java.util.Queue

class HierarchyAwareClassVisitor : ClassVisitor(Opcodes.ASM9) {
	private var visitingClass: String? = null

	private val classes = mutableMapOf<String, ClassEntry>()

	override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>) {
		visitingClass = name;
		classes[name] = ClassEntry(name, interfaces.toList() + superName?.let { listOf(it) }.orEmpty())
		super.visit(version, access, name, signature, superName, interfaces)
	}

	override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
		classes[visitingClass!!]?.methods?.add("$name.$descriptor")
		return super.visitMethod(access, name, descriptor, signature, exceptions)
	}

	fun getRealOwnerOfMethod(owner: String, name: String, descriptor: String): String {
		val entries = mutableListOf<ClassEntry>()
		val toVisit: Queue<String> = LinkedList()
		val visited = mutableSetOf<String>()
		toVisit.add(owner)

		while (toVisit.isNotEmpty()) {
			val current = toVisit.poll()
			if (visited.add(current)) {
				classes[current]?.let {
					entries += it
					toVisit.addAll(it.parents)
				}
			}
		}

		for (entry in entries.reversed()) {
			if (entry.methods.contains("$name.$descriptor")) {
				return entry.name
			}
		}
		return owner
	}

	private data class ClassEntry(val name: String, val parents: List<String>) {
		val methods = mutableListOf<String>()
	}
}