package com.jini.indicator.network

import com.jini.indicator.config.ScopeConfig
import com.jini.indicator.context.IndicatorContext
import com.jini.indicator.scope.ScopeMatcher
import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

class IndicatorOkHttpInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!ScopeMatcher.isInScope()) return chain.proceed(chain.request())
        val callId = UUID.randomUUID().toString()
        IndicatorContext.acquire(callId, ScopeConfig.current.timeout)
        return try {
            chain.proceed(chain.request())
        } finally {
            IndicatorContext.release(callId)
        }
    }
}
