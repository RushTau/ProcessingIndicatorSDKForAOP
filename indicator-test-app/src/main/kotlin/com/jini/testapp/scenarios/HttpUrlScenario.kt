package com.jini.testapp.scenarios

import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class HttpUrlScenario {
    fun run() = CoroutineScope(Dispatchers.IO).launch {
        val conn = URL("https://httpbin.org/delay/2").openConnection() as HttpURLConnection
        conn.connect()
        conn.inputStream.close()
        conn.disconnect()
    }
}
