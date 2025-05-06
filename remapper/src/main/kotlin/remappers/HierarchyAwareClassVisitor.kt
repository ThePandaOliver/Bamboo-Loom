package dev.pandasystems.remappers

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM9

class HierarchyAwareClassVisitor(
	classVisitor: ClassVisitor,
) : ClassVisitor(ASM9, classVisitor) {
}