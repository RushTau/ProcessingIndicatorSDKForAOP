package com.jini.indicator.init

import android.content.Context
import com.jini.indicator.config.ScopeConfig
import com.jini.indicator.config.YamlConfigParser

object IndicatorSDK {
    fun initialize(context: Context) {
        val config = YamlConfigParser.parse(context)
        ScopeConfig.init(config)
        // IndicatorOverlayManager.init(context, config) — Task 5 구현 후 연결
    }
}
