package com.jini.indicator.network

import com.jini.indicator.config.*
import com.jini.indicator.context.IndicatorContext
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IndicatorOkHttpInterceptorTest {

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        IndicatorContext.reset()
        IndicatorContext.onShow = {}
        IndicatorContext.onHide = {}
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `acquires and releases context when in scope`() {
        var showCount = 0; var hideCount = 0
        IndicatorContext.onShow = { showCount++ }
        IndicatorContext.onHide = { hideCount++ }
        ScopeConfig.init(IndicatorConfig(scopes = ScopesConfig(
            include = listOf(ScopeRule.PackageRule("com.jini.indicator.network"))
        )))
        val chain = mockk<Interceptor.Chain>()
        val request = mockk<Request>()
        every { chain.request() } returns request
        every { chain.proceed(request) } returns mockk()

        IndicatorOkHttpInterceptor().intercept(chain)

        assertEquals(1, showCount)
        assertEquals(1, hideCount)
    }

    @Test
    fun `skips context when not in scope`() {
        var showCount = 0
        IndicatorContext.onShow = { showCount++ }
        ScopeConfig.init(IndicatorConfig(scopes = ScopesConfig(include = emptyList())))
        val chain = mockk<Interceptor.Chain>()
        val request = mockk<Request>()
        every { chain.request() } returns request
        every { chain.proceed(request) } returns mockk()

        IndicatorOkHttpInterceptor().intercept(chain)
        assertEquals(0, showCount)
    }

    @Test
    fun `releases context even when chain throws`() {
        var hideCount = 0
        IndicatorContext.onHide = { hideCount++ }
        ScopeConfig.init(IndicatorConfig(scopes = ScopesConfig(
            include = listOf(ScopeRule.PackageRule("com.jini.indicator.network"))
        )))
        val chain = mockk<Interceptor.Chain>()
        val request = mockk<Request>()
        every { chain.request() } returns request
        every { chain.proceed(request) } throws java.io.IOException("network error")

        assertThrows(java.io.IOException::class.java) { IndicatorOkHttpInterceptor().intercept(chain) }
        assertEquals(1, hideCount)
    }
}
