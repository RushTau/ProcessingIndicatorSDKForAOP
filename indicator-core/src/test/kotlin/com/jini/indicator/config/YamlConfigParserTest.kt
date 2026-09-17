package com.jini.indicator.config

import org.junit.Assert.*
import org.junit.Test

class YamlConfigParserTest {

    @Test
    fun `parses complete yaml`() {
        val yaml = """
            indicator:
              timeout: 15000
              overlay:
                blur: true
                dim_alpha: 0.7
              image:
                type: lottie
                file: assets/loading.json
            scopes:
              include:
                - package: com.example.feature
                - class: com.example.HomeActivity
                - class: com.example.ApiService
                  methods:
                    - fetchUser
              exclude:
                - class: com.example.LoggingService
        """.trimIndent()
        val config = YamlConfigParser.parseFromString(yaml)
        assertEquals(15000L, config.timeout)
        assertTrue(config.overlay.blur)
        assertEquals(0.7f, config.overlay.dimAlpha, 0.001f)
        assertEquals(ImageType.LOTTIE, config.image.type)
        assertEquals("assets/loading.json", config.image.file)
        assertEquals(3, config.scopes.include.size)
        assertEquals(1, config.scopes.exclude.size)
    }

    @Test
    fun `uses defaults when fields omitted`() {
        val config = YamlConfigParser.parseFromString("indicator:\n  timeout: 5000")
        assertEquals(5000L, config.timeout)
        assertTrue(config.overlay.blur)
        assertEquals(0.6f, config.overlay.dimAlpha, 0.001f)
        assertEquals(ImageType.DEFAULT, config.image.type)
        assertTrue(config.scopes.include.isEmpty())
    }

    @Test
    fun `parses package scope rule`() {
        val config = YamlConfigParser.parseFromString(
            "scopes:\n  include:\n    - package: com.example.app"
        )
        val rule = config.scopes.include[0]
        assertTrue(rule is ScopeRule.PackageRule)
        assertEquals("com.example.app", (rule as ScopeRule.PackageRule).name)
    }

    @Test
    fun `parses class rule with methods`() {
        val yaml = """
            scopes:
              include:
                - class: com.example.Service
                  methods:
                    - doWork
                    - fetchData
        """.trimIndent()
        val rule = YamlConfigParser.parseFromString(yaml).scopes.include[0] as ScopeRule.ClassRule
        assertEquals("com.example.Service", rule.name)
        assertEquals(listOf("doWork", "fetchData"), rule.methods)
    }
}
