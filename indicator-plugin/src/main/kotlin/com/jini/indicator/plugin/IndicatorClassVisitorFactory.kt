package com.jini.indicator.plugin

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.jini.indicator.plugin.weaver.HttpUrlConnectionWeaver
import com.jini.indicator.plugin.weaver.OkHttpWeaver
import org.objectweb.asm.ClassVisitor

abstract class IndicatorClassVisitorFactory :
    AsmClassVisitorFactory<InstrumentationParameters.None> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        var visitor = nextClassVisitor

        visitor = HttpUrlConnectionWeaver.createClassVisitor(visitor)

        if (classContext.currentClassData.className == "okhttp3.OkHttpClient\$Builder") {
            visitor = OkHttpWeaver.createClassVisitor(visitor)
        }

        return visitor
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val name = classData.className
        return name == "okhttp3.OkHttpClient\$Builder" ||
            (!name.startsWith("com.jini.indicator.") &&
                !name.startsWith("android.") &&
                !name.startsWith("androidx.") &&
                !name.startsWith("kotlin.") &&
                !name.startsWith("kotlinx.") &&
                !name.startsWith("org.objectweb.asm."))
    }
}
