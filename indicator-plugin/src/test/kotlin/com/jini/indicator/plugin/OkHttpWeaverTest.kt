package com.jini.indicator.plugin

import com.jini.indicator.plugin.weaver.OkHttpWeaver
import org.junit.Assert.*
import org.junit.Test
import org.objectweb.asm.ClassReader

class OkHttpWeaverTest {

    @Test
    fun `does not modify non-OkHttpClient classes`() {
        val classBytes = String::class.java
            .getResourceAsStream("/java/lang/String.class")!!.readBytes()
        assertArrayEquals(classBytes, OkHttpWeaver.weave(classBytes))
    }

    @Test
    fun `weave injects addInterceptor into OkHttpClient Builder build`() {
        val stream = javaClass.classLoader
            .getResourceAsStream("okhttp3/OkHttpClient\$Builder.class")
            ?: return // okhttp not on test classpath → skip
        val original = stream.readBytes()
        val woven = OkHttpWeaver.weave(original)
        assertFalse("woven bytes should differ from original", original.contentEquals(woven))
        val wovenStr = String(woven, Charsets.ISO_8859_1)
        assertTrue(
            "should reference IndicatorOkHttpInterceptor",
            "IndicatorOkHttpInterceptor" in wovenStr
        )
    }

    @Test
    fun `weave produces valid bytecode`() {
        val stream = javaClass.classLoader
            .getResourceAsStream("okhttp3/OkHttpClient\$Builder.class")
            ?: return
        val woven = OkHttpWeaver.weave(stream.readBytes())
        val reader = ClassReader(woven)
        assertEquals("okhttp3/OkHttpClient\$Builder", reader.className)
    }
}
