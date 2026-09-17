package com.jini.indicator.plugin

import org.junit.Assert.*
import org.junit.Test

class ProguardRuleGeneratorTest {

    @Test
    fun `generates keep for package scope`() {
        val rules = ProguardRuleGenerator.generate(
            listOf(mapOf("package" to "com.example.feature")),
            emptyList()
        )
        assertTrue(rules.contains("-keep class com.example.feature.**"))
    }

    @Test
    fun `generates keep for class scope`() {
        val rules = ProguardRuleGenerator.generate(
            listOf(mapOf("class" to "com.example.HomeActivity")),
            emptyList()
        )
        assertTrue(rules.contains("-keep class com.example.HomeActivity"))
    }

    @Test
    fun `generates keepclassmembers for method scope`() {
        val rules = ProguardRuleGenerator.generate(
            listOf(mapOf<String, Any>(
                "class" to "com.example.ApiService",
                "methods" to listOf("fetchUser")
            )),
            emptyList()
        )
        assertTrue(rules.contains("-keepclassmembers class com.example.ApiService"))
        assertTrue(rules.contains("*** fetchUser("))
    }

    @Test
    fun `always includes SDK internal rules`() {
        val rules = ProguardRuleGenerator.generate(emptyList(), emptyList())
        assertTrue(rules.contains("-keep class com.jini.indicator.**"))
        assertTrue(rules.contains("OkHttpClient"))
        assertTrue(rules.contains("addInterceptor"))
    }

    @Test
    fun `empty scopes produces only SDK rules`() {
        val rules = ProguardRuleGenerator.generate(emptyList(), emptyList())
        assertFalse(rules.contains("-keep class com.example"))
        assertTrue(rules.contains("-keep class com.jini.indicator.**"))
    }
}
