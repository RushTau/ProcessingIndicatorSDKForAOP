package com.jini.testapp.scenarios

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

class ExcludeScenario {
    private val client = OkHttpClient.Builder().build()

    // YAML exclude에 이 클래스 등록됨 → 인디케이터 미표시
    fun run() = CoroutineScope(Dispatchers.IO).launch {
        client.newCall(Request.Builder().url("https://httpbin.org/delay/2").build()).execute().close()
    }
}
