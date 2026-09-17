package com.jini.indicator.plugin.weaver

import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter

object OkHttpWeaver {
    private const val TARGET = "okhttp3/OkHttpClient\$Builder"
    private const val INTERCEPTOR = "com/jini/indicator/network/IndicatorOkHttpInterceptor"

    fun weave(classBytes: ByteArray): ByteArray {
        val reader = ClassReader(classBytes)
        if (reader.className != TARGET) return classBytes
        val writer = ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
        reader.accept(createClassVisitor(writer), ClassReader.EXPAND_FRAMES)
        return writer.toByteArray()
    }

    fun createClassVisitor(next: ClassVisitor): ClassVisitor =
        OkHttpBuildClassVisitor(next)

    private class OkHttpBuildClassVisitor(
        next: ClassVisitor
    ) : ClassVisitor(Opcodes.ASM9, next) {

        override fun visitMethod(
            access: Int, name: String, desc: String,
            sig: String?, ex: Array<String>?
        ): MethodVisitor {
            val mv = super.visitMethod(access, name, desc, sig, ex)
            if (name != "build" || desc != "()Lokhttp3/OkHttpClient;") return mv
            return BuildMethodAdapter(mv, access, name, desc)
        }
    }

    private class BuildMethodAdapter(
        mv: MethodVisitor, access: Int, name: String, desc: String
    ) : AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {

        override fun onMethodEnter() {
            // this.addInterceptor(new IndicatorOkHttpInterceptor())
            loadThis()
            visitTypeInsn(Opcodes.NEW, INTERCEPTOR)
            visitInsn(Opcodes.DUP)
            visitMethodInsn(
                Opcodes.INVOKESPECIAL, INTERCEPTOR, "<init>", "()V", false
            )
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, TARGET, "addInterceptor",
                "(Lokhttp3/Interceptor;)Lokhttp3/OkHttpClient\$Builder;", false
            )
            visitInsn(Opcodes.POP)
        }
    }
}
