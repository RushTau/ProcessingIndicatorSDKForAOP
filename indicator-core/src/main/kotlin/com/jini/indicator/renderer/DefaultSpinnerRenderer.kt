package com.jini.indicator.renderer

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.ProgressBar

class DefaultSpinnerRenderer(context: Context) : IndicatorRenderer {
    override val view: View = ProgressBar(context).apply {
        indeterminateTintList = ColorStateList.valueOf(Color.WHITE)
    }
}
