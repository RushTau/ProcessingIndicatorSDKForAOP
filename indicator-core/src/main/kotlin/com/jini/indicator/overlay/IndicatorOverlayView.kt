package com.jini.indicator.overlay

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import com.jini.indicator.config.ImageConfig
import com.jini.indicator.renderer.resolveRenderer

class IndicatorOverlayView(context: Context, imageConfig: ImageConfig) : FrameLayout(context) {
    init {
        addView(
            resolveRenderer(context, imageConfig).view,
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        )
    }
}
