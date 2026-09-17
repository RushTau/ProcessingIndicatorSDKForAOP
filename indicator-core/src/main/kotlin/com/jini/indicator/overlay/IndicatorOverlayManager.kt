package com.jini.indicator.overlay

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import com.jini.indicator.config.IndicatorConfig

object IndicatorOverlayManager : Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null
    private var config: IndicatorConfig = IndicatorConfig()
    private var blurLayer: View? = null
    private var indicatorLayer: IndicatorOverlayView? = null
    private val handler = Handler(Looper.getMainLooper())

    fun init(context: Context, indicatorConfig: IndicatorConfig) {
        config = indicatorConfig
        (context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(this)
    }

    fun show() = handler.post {
        val activity = currentActivity ?: return@post
        val decorView = activity.window.decorView as? FrameLayout ?: return@post
        if (blurLayer != null) return@post

        // Layer 1: blur/dim 배경 — RenderEffect는 이 View에만 적용
        val blur = View(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && config.overlay.blur) {
                setRenderEffect(RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP))
                setBackgroundColor(Color.argb(80, 0, 0, 0))
            } else {
                setBackgroundColor(Color.argb((255 * config.overlay.dimAlpha).toInt(), 0, 0, 0))
            }
        }
        decorView.addView(blur)
        blurLayer = blur

        // Layer 2: 인디케이터 — blur Layer의 형제 뷰로 추가 (블러 미적용)
        val indicator = IndicatorOverlayView(activity, config.image).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        decorView.addView(indicator)
        indicatorLayer = indicator
    }

    fun hide() = handler.post {
        val decorView = currentActivity?.window?.decorView as? FrameLayout
        blurLayer?.let { decorView?.removeView(it) }
        indicatorLayer?.let { decorView?.removeView(it) }
        blurLayer = null
        indicatorLayer = null
    }

    // ActivityLifecycleCallbacks

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) {
            blurLayer = null
            indicatorLayer = null
            currentActivity = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}
