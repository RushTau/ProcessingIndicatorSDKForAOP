package com.jini.indicator.scope

import com.jini.indicator.config.ScopeConfig
import com.jini.indicator.config.ScopeRule

object ScopeMatcher {

    fun isInScope(): Boolean = isInScopeForFrames(Thread.currentThread().stackTrace)

    fun isInScopeForFrames(frames: Array<StackTraceElement>): Boolean {
        val config = ScopeConfig.current.scopes
        if (config.include.isEmpty()) return false
        val included = frames.any { frame -> config.include.any { it.matches(frame) } }
        if (!included) return false
        return frames.none { frame -> config.exclude.any { it.matches(frame) } }
    }

    private fun ScopeRule.matches(frame: StackTraceElement): Boolean = when (this) {
        is ScopeRule.PackageRule -> frame.className.startsWith(name)
        is ScopeRule.ClassRule   -> frame.className == name &&
            (methods.isEmpty() || frame.methodName in methods)
    }
}
