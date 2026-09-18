package com.jini.testapp.scenarios

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

class TimeoutScenario {
    private val client = OkHttpClient.Builder().build()

    // 30초 delay → SDK 10초 타임아웃으로 인디케이터 자동 해제 확인
    fun run() = CoroutineScope(Dispatchers.IO).launch {
        runCatching {
            client.newCall(
                Request.Builder().url("https://httpbin.org/delay/30").build()
            ).execute().close()
        }
    }
}
