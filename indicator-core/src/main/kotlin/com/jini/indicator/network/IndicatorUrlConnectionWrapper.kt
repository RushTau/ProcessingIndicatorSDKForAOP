package com.jini.indicator.network

import com.jini.indicator.config.ScopeConfig
import com.jini.indicator.context.IndicatorContext
import com.jini.indicator.scope.ScopeMatcher
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URLConnection
import java.util.UUID

class IndicatorUrlConnectionWrapper(
    private val delegate: HttpURLConnection
) : HttpURLConnection(delegate.url) {

    companion object {
        @JvmStatic
        fun wrap(conn: URLConnection): URLConnection {
            if (conn !is HttpURLConnection || !ScopeMatcher.isInScope()) return conn
            return IndicatorUrlConnectionWrapper(conn)
        }
    }

    private val callId = UUID.randomUUID().toString()

    override fun connect() {
        IndicatorContext.acquire(callId, ScopeConfig.current.timeout)
        try { delegate.connect() } catch (e: Exception) { IndicatorContext.release(callId); throw e }
    }

    override fun getInputStream(): InputStream = try {
        delegate.inputStream
    } finally {
        IndicatorContext.release(callId)
    }

    override fun disconnect() = delegate.disconnect()
    override fun usingProxy(): Boolean = delegate.usingProxy()
    override fun getResponseCode(): Int = delegate.responseCode
    override fun getResponseMessage(): String = delegate.responseMessage
}
