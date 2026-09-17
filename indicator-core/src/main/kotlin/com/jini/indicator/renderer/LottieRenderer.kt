package com.jini.indicator.renderer

import android.animation.ValueAnimator
import android.content.Context
import android.view.View

class LottieRenderer(context: Context, file: String) : IndicatorRenderer {
    override val view: View = try {
        val cls = Class.forName("com.airbnb.lottie.LottieAnimationView")
        val v = cls.getConstructor(Context::class.java).newInstance(context)
        cls.getMethod("setAnimation", String::class.java).invoke(v, file.removePrefix("assets/"))
        cls.getMethod("setRepeatCount", Int::class.java).invoke(v, ValueAnimator.INFINITE)
        cls.getMethod("playAnimation").invoke(v)
        v as View
    } catch (e: ClassNotFoundException) {
        throw IllegalStateException("Lottie dependency not found. Add lottie to build.gradle.")
    }
}
