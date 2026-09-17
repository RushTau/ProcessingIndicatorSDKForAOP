package com.jini.indicator.context

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object IndicatorContext {
    private val depth = AtomicInteger(0)
    private val timeoutJobs = ConcurrentHashMap<String, Job>()
    // Dispatchers.Main은 JVM 테스트에서 미지원 → Default 사용 (UI 전환은 onShow/onHide 콜백에서)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    internal var onShow: () -> Unit = { /* Task 5에서 IndicatorOverlayManager.show() 연결 */ }
    internal var onHide: () -> Unit = { /* Task 5에서 IndicatorOverlayManager.hide() 연결 */ }

    fun acquire(callId: String, timeoutMs: Long) {
        if (depth.getAndIncrement() == 0) onShow()
        timeoutJobs[callId] = scope.launch {
            delay(timeoutMs)
            release(callId)
        }
    }

    fun release(callId: String) {
        timeoutJobs.remove(callId)?.cancel()
        val prev = depth.get()
        if (prev <= 0) return   // 이미 0 — 클램프만, hide 호출 안 함
        val remaining = depth.decrementAndGet()
        if (remaining == 0) onHide()
    }

    fun reset() {
        timeoutJobs.values.forEach { it.cancel() }
        timeoutJobs.clear()
        depth.set(0)
    }
}
