package com.jini.indicator.config

object ScopeConfig {
    @Volatile private var _current: IndicatorConfig = IndicatorConfig()
    val current: IndicatorConfig get() = _current
    fun init(config: IndicatorConfig) { _current = config }
}
