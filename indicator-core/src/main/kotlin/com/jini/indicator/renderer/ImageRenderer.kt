package com.jini.indicator.renderer

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.widget.ImageView

class ImageRenderer(context: Context, file: String) : IndicatorRenderer {
    override val view: View = ImageView(context).apply {
        context.assets.open(file.removePrefix("assets/")).use { stream ->
            setImageDrawable(android.graphics.drawable.Drawable.createFromStream(stream, null))
        }
        (drawable as? AnimationDrawable)?.start()
    }
}
