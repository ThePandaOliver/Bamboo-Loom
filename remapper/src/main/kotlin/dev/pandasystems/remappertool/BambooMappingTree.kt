package dev.pandasystems.remappertool

import net.fabricmc.mappingio.MappedElementKind
import net.fabricmc.mappingio.MappingVisitor
import net.fabricmc.mappingio.tree.MappingTree
import net.fabricmc.mappingio.tree.MemoryMappingTree
import net.fabricmc.mappingio.tree.VisitOrder
import net.fabricmc.mappingio.tree.VisitableMappingTree

class BambooMappingTree : VisitableMappingTree {
	private val tree = MemoryMappingTree()
	
	override fun setSrcNamespace(namespace: String?): String? {
		return tree.setSrcNamespace(namespace)
	}

	override fun setDstNamespaces(namespaces: List<String?>?): List<String?>? {
		return tree.setDstNamespaces(namespaces)
	}

	override fun getMetadata(): List<MappingTree.MetadataEntry?>? {
		return tree.getMetadata()
	}

	override fun getMetadata(key: String?): List<MappingTree.MetadataEntry?>? {
		return tree.getMetadata(key)
	}

	override fun addMetadata(entry: MappingTree.MetadataEntry?) {
		tree.addMetadata(entry)
	}

	override fun removeMetadata(key: String?): Boolean {
		return tree.removeMetadata(key)
	}

	override fun getClasses(): Collection<MappingTree.ClassMapping?>? {
		return tree.getClasses()
	}

	override fun getClass(srcName: String?): MappingTree.ClassMapping? {
		return tree.getClass(srcName)
	}

	override fun addClass(cls: MappingTree.ClassMapping?): MappingTree.ClassMapping? {
		return tree.addClass(cls)
	}

	override fun removeClass(srcName: String?): MappingTree.ClassMapping? {
		return tree.removeClass(srcName)
	}

	override fun getSrcNamespace(): String? {
		return tree.getSrcNamespace()
	}

	override fun getDstNamespaces(): List<String?>? {
		return tree.getDstNamespaces()
	}

	override fun accept(visitor: MappingVisitor?, order: VisitOrder?) {
		tree.accept(visitor, order)
	}

	override fun visitNamespaces(srcNamespace: String?, dstNamespaces: List<String?>?) {
		tree.visitNamespaces(srcNamespace, dstNamespaces)
	}

	override fun visitClass(srcName: String?): Boolean {
		return tree.visitClass(srcName)
	}

	override fun visitField(srcName: String?, srcDesc: String?): Boolean {
		return tree.visitField(srcName, srcDesc)
	}

	override fun visitMethod(srcName: String?, srcDesc: String?): Boolean {
		return tree.visitMethod(srcName, srcDesc)
	}

	override fun visitMethodArg(argPosition: Int, lvIndex: Int, srcName: String?): Boolean {
		return tree.visitMethodArg(argPosition, lvIndex, srcName)
	}

	override fun visitMethodVar(
		lvtRowIndex: Int,
		lvIndex: Int,
		startOpIdx: Int,
		endOpIdx: Int,
		srcName: String?
	): Boolean {
		return tree.visitMethodVar(lvtRowIndex, lvIndex, startOpIdx, endOpIdx, srcName)
	}

	override fun visitDstName(targetKind: MappedElementKind?, namespace: Int, name: String?) {
		return tree.visitDstName(targetKind, namespace, name)
	}

	override fun visitComment(targetKind: MappedElementKind?, comment: String?) {
		return tree.visitComment(targetKind, comment)
	}
}