package com.jini.indicator.plugin.weaver

import org.objectweb.asm.*

object HttpUrlConnectionWeaver {
    private const val URL_OWNER = "java/net/URL"
    private const val OPEN_CONNECTION = "openConnection"
    private const val OPEN_DESC = "()Ljava/net/URLConnection;"
    private const val WRAPPER = "com/jini/indicator/network/IndicatorUrlConnectionWrapper"
    private const val WRAP_DESC = "(Ljava/net/URLConnection;)Ljava/net/URLConnection;"

    fun weave(classBytes: ByteArray): ByteArray {
        val reader = ClassReader(classBytes)
        val writer = ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
        var modified = false

        reader.accept(object : ClassVisitor(Opcodes.ASM9, writer) {
            override fun visitMethod(
                access: Int, name: String, desc: String,
                sig: String?, ex: Array<String>?
            ): MethodVisitor {
                val mv = super.visitMethod(access, name, desc, sig, ex)
                return CallSiteMethodVisitor(mv) { modified = true }
            }
        }, ClassReader.EXPAND_FRAMES)

        return if (modified) writer.toByteArray() else classBytes
    }

    fun createClassVisitor(next: ClassVisitor): ClassVisitor =
        HttpUrlCallSiteClassVisitor(next)

    private class HttpUrlCallSiteClassVisitor(
        next: ClassVisitor
    ) : ClassVisitor(Opcodes.ASM9, next) {

        override fun visitMethod(
            access: Int, name: String, desc: String,
            sig: String?, ex: Array<String>?
        ): MethodVisitor {
            val mv = super.visitMethod(access, name, desc, sig, ex)
            return CallSiteMethodVisitor(mv) {}
        }
    }

    private class CallSiteMethodVisitor(
        mv: MethodVisitor,
        private val onModified: () -> Unit
    ) : MethodVisitor(Opcodes.ASM9, mv) {

        override fun visitMethodInsn(
            opcode: Int, owner: String, name: String,
            desc: String, isInterface: Boolean
        ) {
            super.visitMethodInsn(opcode, owner, name, desc, isInterface)
            if (owner == URL_OWNER && name == OPEN_CONNECTION && desc == OPEN_DESC) {
                super.visitMethodInsn(
                    Opcodes.INVOKESTATIC, WRAPPER, "wrap", WRAP_DESC, false
                )
                onModified()
            }
        }
    }
}
