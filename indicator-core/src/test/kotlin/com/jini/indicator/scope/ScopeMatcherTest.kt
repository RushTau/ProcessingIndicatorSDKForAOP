package com.jini.indicator.scope

import com.jini.indicator.config.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ScopeMatcherTest {

    @Before fun setUp() = ScopeConfig.init(IndicatorConfig())

    @Test
    fun `returns false when include list is empty`() {
        ScopeConfig.init(IndicatorConfig(scopes = ScopesConfig(include = emptyList())))
        val frames = arrayOf(StackTraceElement("com.example.HomeActivity", "onCreate", "HomeActivity.kt", 10))
        assertFalse(ScopeMatcher.isInScopeForFrames(frames))
    }

    @Test
    fun `matches package rule`() {
        ScopeConfig.init(IndicatorConfig(scopes = ScopesConfig(
            include = listOf(ScopeRule.PackageRule("com.example.feature"))
        )))
        val frames = arrayOf(StackTraceElement("com.example.feature.HomeActivity", "fetchData", "HomeActivity.kt", 20))
        assertTrue(ScopeMatcher.isInScopeForFrames(frames))
    }

    @Test
    fun `matches class rule without methods`() {
        ScopeConfig.init(IndicatorConfig(scopes = ScopesConfig(
            include = listOf(ScopeRule.ClassRule("com.example.HomeActivity"))
        )))
        val frames = arrayOf(StackTraceElement("com.example.HomeActivity", "anyMethod", "HomeActivity.kt", 5))
        assertTrue(ScopeMatcher.isInScopeForFrames(frames))
    }

    @Test
    fun `matches class rule with specific method only`() {
        ScopeConfig.init(IndicatorConfig(scopes = ScopesConfig(
            include = listOf(ScopeRule.ClassRule("com.example.ApiService", listOf("fetchUser")))
        )))
        assertTrue(ScopeMatcher.isInScopeForFrames(arrayOf(
            StackTraceElement("com.example.ApiService", "fetchUser", "ApiService.kt", 10)
        )))
        assertFalse(ScopeMatcher.isInScopeForFrames(arrayOf(
            StackTraceElement("com.example.ApiService", "otherMethod", "ApiService.kt", 20)
        )))
    }

    @Test
    fun `exclude overrides include at class level`() {
        ScopeConfig.init(IndicatorConfig(scopes = ScopesConfig(
            include = listOf(ScopeRule.PackageRule("com.example.feature")),
            exclude = listOf(ScopeRule.ClassRule("com.example.feature.LoggingService"))
        )))
        assertTrue(ScopeMatcher.isInScopeForFrames(arrayOf(
            StackTraceElement("com.example.feature.HomeActivity", "onClick", "HomeActivity.kt", 10)
        )))
        assertFalse(ScopeMatcher.isInScopeForFrames(arrayOf(
            StackTraceElement("com.example.feature.LoggingService", "log", "LoggingService.kt", 5)
        )))
    }

    @Test
    fun `exclude method overrides include class`() {
        ScopeConfig.init(IndicatorConfig(scopes = ScopesConfig(
            include = listOf(ScopeRule.ClassRule("com.example.AnalyticsService")),
            exclude = listOf(ScopeRule.ClassRule("com.example.AnalyticsService", listOf("trackEvent")))
        )))
        assertTrue(ScopeMatcher.isInScopeForFrames(arrayOf(
            StackTraceElement("com.example.AnalyticsService", "init", "AnalyticsService.kt", 5)
        )))
        assertFalse(ScopeMatcher.isInScopeForFrames(arrayOf(
            StackTraceElement("com.example.AnalyticsService", "trackEvent", "AnalyticsService.kt", 10)
        )))
    }
}
