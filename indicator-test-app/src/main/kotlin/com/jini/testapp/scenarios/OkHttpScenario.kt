package com.jini.testapp.scenarios

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

class OkHttpScenario {
    private val client = OkHttpClient.Builder().build()

    fun runSingle() = CoroutineScope(Dispatchers.IO).launch {
        client.newCall(Request.Builder().url("https://httpbin.org/delay/2").build()).execute().close()
    }

    fun runConcurrent() {
        val scope = CoroutineScope(Dispatchers.IO)
        repeat(3) { i ->
            scope.launch {
                client.newCall(
                    Request.Builder().url("https://httpbin.org/delay/${i + 1}").build()
                ).execute().close()
            }
        }
    }
}
