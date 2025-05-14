package dev.pandasystems.remappertool

import net.fabricmc.mappingio.MappedElementKind
import net.fabricmc.mappingio.MappingVisitor
import net.fabricmc.mappingio.tree.MappingTree
import net.fabricmc.mappingio.tree.MemoryMappingTree
import net.fabricmc.mappingio.tree.VisitOrder
import net.fabricmc.mappingio.tree.VisitableMappingTree

class BambooMappingTree : MemoryMappingTree() {
	override fun setSrcNamespace(namespace: String?): String? {
		TODO("Not yet implemented")
	}

	override fun setDstNamespaces(namespaces: List<String?>?): List<String?>? {
		TODO("Not yet implemented")
	}

	override fun getMetadata(): List<MappingTree.MetadataEntry?>? {
		TODO("Not yet implemented")
	}

	override fun getMetadata(key: String?): List<MappingTree.MetadataEntry?>? {
		TODO("Not yet implemented")
	}

	override fun addMetadata(entry: MappingTree.MetadataEntry?) {
		TODO("Not yet implemented")
	}

	override fun removeMetadata(key: String?): Boolean {
		TODO("Not yet implemented")
	}

	override fun getClasses(): Collection<MappingTree.ClassMapping?>? {
		TODO("Not yet implemented")
	}

	override fun getClass(srcName: String?): MappingTree.ClassMapping? {
		TODO("Not yet implemented")
	}

	override fun addClass(cls: MappingTree.ClassMapping?): MappingTree.ClassMapping? {
		TODO("Not yet implemented")
	}

	override fun removeClass(srcName: String?): MappingTree.ClassMapping? {
		TODO("Not yet implemented")
	}

	override fun getSrcNamespace(): String? {
		TODO("Not yet implemented")
	}

	override fun getDstNamespaces(): List<String?>? {
		TODO("Not yet implemented")
	}

	override fun accept(visitor: MappingVisitor?, order: VisitOrder?) {
		TODO("Not yet implemented")
	}

	override fun visitNamespaces(srcNamespace: String?, dstNamespaces: List<String?>?) {
		TODO("Not yet implemented")
	}

	override fun visitClass(srcName: String?): Boolean {
		TODO("Not yet implemented")
	}

	override fun visitField(srcName: String?, srcDesc: String?): Boolean {
		TODO("Not yet implemented")
	}

	override fun visitMethod(srcName: String?, srcDesc: String?): Boolean {
		TODO("Not yet implemented")
	}

	override fun visitMethodArg(argPosition: Int, lvIndex: Int, srcName: String?): Boolean {
		TODO("Not yet implemented")
	}

	override fun visitMethodVar(
		lvtRowIndex: Int,
		lvIndex: Int,
		startOpIdx: Int,
		endOpIdx: Int,
		srcName: String?
	): Boolean {
		TODO("Not yet implemented")
	}

	override fun visitDstName(targetKind: MappedElementKind?, namespace: Int, name: String?) {
		TODO("Not yet implemented")
	}

	override fun visitComment(targetKind: MappedElementKind?, comment: String?) {
		TODO("Not yet implemented")
	}
}