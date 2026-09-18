package com.jini.testapp

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import com.jini.testapp.scenarios.*

class MainActivity : Activity() {
    private val okHttp = OkHttpScenario()
    private val httpUrl = HttpUrlScenario()
    private val exclude = ExcludeScenario()
    private val timeout = TimeoutScenario()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn_okhttp_single).setOnClickListener { okHttp.runSingle() }
        findViewById<Button>(R.id.btn_okhttp_concurrent).setOnClickListener { okHttp.runConcurrent() }
        findViewById<Button>(R.id.btn_httpurl).setOnClickListener { httpUrl.run() }
        findViewById<Button>(R.id.btn_exclude).setOnClickListener { exclude.run() }
        findViewById<Button>(R.id.btn_timeout).setOnClickListener { timeout.run() }
    }
}
