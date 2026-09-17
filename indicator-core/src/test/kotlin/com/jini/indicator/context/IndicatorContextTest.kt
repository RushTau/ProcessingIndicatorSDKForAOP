package com.jini.indicator.context

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IndicatorContextTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() {
        Dispatchers.setMain(testDispatcher)
        IndicatorContext.reset()
        IndicatorContext.onShow = {}
        IndicatorContext.onHide = {}
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `acquire shows overlay on first call`() {
        var showCount = 0
        IndicatorContext.onShow = { showCount++ }
        IndicatorContext.acquire("call1", 30_000L)
        assertEquals(1, showCount)
    }

    @Test
    fun `second acquire does not show again`() {
        var showCount = 0
        IndicatorContext.onShow = { showCount++ }
        IndicatorContext.acquire("call1", 30_000L)
        IndicatorContext.acquire("call2", 30_000L)
        assertEquals(1, showCount)
    }

    @Test
    fun `release hides only when all calls complete`() {
        var hideCount = 0
        IndicatorContext.onHide = { hideCount++ }
        IndicatorContext.acquire("call1", 30_000L)
        IndicatorContext.acquire("call2", 30_000L)
        IndicatorContext.release("call1")
        assertEquals(0, hideCount)
        IndicatorContext.release("call2")
        assertEquals(1, hideCount)
    }

    @Test
    fun `depth does not go below zero on excess release`() {
        var hideCount = 0
        IndicatorContext.onHide = { hideCount++ }
        IndicatorContext.release("nonexistent")
        IndicatorContext.release("nonexistent2")
        // depth가 0일 때는 hide 호출 안 함 (클램프만)
        assertEquals(0, hideCount)
    }

    @Test
    fun `reset clears all state`() {
        var showCount = 0
        IndicatorContext.onShow = { showCount++ }
        IndicatorContext.acquire("call1", 30_000L)
        IndicatorContext.reset()
        IndicatorContext.acquire("call2", 30_000L)
        assertEquals(2, showCount)
    }
}
