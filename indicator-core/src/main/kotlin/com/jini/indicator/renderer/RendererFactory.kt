package com.jini.indicator.renderer

import android.content.Context
import com.jini.indicator.config.ImageConfig
import com.jini.indicator.config.ImageType

fun resolveRenderer(context: Context, config: ImageConfig): IndicatorRenderer = when {
    config.type == ImageType.LOTTIE && config.file != null ->
        runCatching { LottieRenderer(context, config.file) }.getOrElse { DefaultSpinnerRenderer(context) }
    config.type == ImageType.IMAGE && config.file != null ->
        runCatching { ImageRenderer(context, config.file) }.getOrElse { DefaultSpinnerRenderer(context) }
    else -> DefaultSpinnerRenderer(context)
}
