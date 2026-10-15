package com.trolyao.macro

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable

class MacroOverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var bubble: TextView? = null
    private var params: WindowManager.LayoutParams? = null
    private var running = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_HIDE -> removeBubble()
            ACTION_TOGGLE -> toggleMacro()
            else -> showBubble()
        }
        return START_NOT_STICKY
    }

    private fun showBubble() {
        if (!Settings.canDrawOverlays(this) || bubble != null) return
        val view = TextView(this).apply {
            text = "▶"
            textSize = 18f
            setTextColor(AndroidColor.BLACK)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(AndroidColor.rgb(49, 215, 232))
                setStroke(3, AndroidColor.rgb(255, 79, 163))
            }
            elevation = 12f
            contentDescription = "Trolyao Macro: chạm để chạy hoặc dừng; giữ để kéo"
        }
        val lp = WindowManager.LayoutParams(
            dp(54), dp(54), WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(18); y = dp(220)
        }
        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0
        var moved = false
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX; downY = event.rawY
                    startX = lp.x; startY = lp.y; moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - downX).toInt()
                    val dy = (event.rawY - downY).toInt()
                    if (kotlin.math.abs(dx) > dp(4) || kotlin.math.abs(dy) > dp(4)) moved = true
                    if (moved) {
                        lp.x = startX + dx; lp.y = startY + dy
                        try { windowManager.updateViewLayout(view, lp) } catch (_: Exception) {}
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) toggleMacro()
                    true
                }
                else -> true
            }
        }
        try {
            windowManager.addView(view, lp)
            bubble = view
            params = lp
        } catch (_: Exception) {
            bubble = null
        }
    }

    private fun toggleMacro() {
        val service = MacroAccessibilityService.instance
        if (service == null) {
            android.os.Handler(mainLooper).post {
                android.widget.Toast.makeText(this, "Bật Trolyao Macro Service trong Cài đặt Trợ năng trước.", android.widget.Toast.LENGTH_LONG).show()
            }
            return
        }
        if (!running) {
            running = true
            bubble?.text = "■"
            service.startMacro()
            pollMacroState()
        } else {
            running = false
            service.stopMacro()
            bubble?.text = "▶"
        }
    }

    private fun pollMacroState() {
        android.os.Handler(mainLooper).postDelayed({
            val service = MacroAccessibilityService.instance
            if (service == null || !service.isMacroRunning()) {
                running = false
                bubble?.text = "▶"
            } else if (running) {
                pollMacroState()
            }
        }, 250L)
    }

    private fun removeBubble() {
        bubble?.let { try { windowManager.removeView(it) } catch (_: Exception) {} }
        bubble = null
        params = null
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        removeBubble()
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_SHOW = "com.trolyao.macro.SHOW"
        const val ACTION_HIDE = "com.trolyao.macro.HIDE"
        const val ACTION_TOGGLE = "com.trolyao.macro.TOGGLE"
    }
}
