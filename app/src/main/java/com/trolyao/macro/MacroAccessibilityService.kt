package com.trolyao.macro

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import java.util.concurrent.atomic.AtomicBoolean

class MacroAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val running = AtomicBoolean(false)
    private var commandIndex = 0
    private var repeatIndex = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        syncOverlay(rootInActiveWindow?.packageName?.toString())
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString()
        syncOverlay(pkg)
    }

    private fun syncOverlay(foregroundPackage: String?) {
        val selected = MacroStore.selectedPackage(this)
        if (selected.isNotBlank() && selected == foregroundPackage) {
            if (android.provider.Settings.canDrawOverlays(this)) {
                startService(Intent(this, MacroOverlayService::class.java).setAction(MacroOverlayService.ACTION_SHOW))
            }
        } else {
            startService(Intent(this, MacroOverlayService::class.java).setAction(MacroOverlayService.ACTION_HIDE))
            stopMacro()
        }
    }

    fun isMacroRunning(): Boolean = running.get()

    fun startMacro() {
        if (running.get()) return
        val commands = MacroStore.commands(this)
        if (commands.isEmpty()) return
        running.set(true)
        commandIndex = 0
        repeatIndex = 0
        runNext(commands)
    }

    fun stopMacro() {
        running.set(false)
        handler.removeCallbacksAndMessages(null)
    }

    private fun runNext(commands: List<MacroCommand>) {
        if (!running.get()) return
        if (commandIndex >= commands.size) {
            running.set(false)
            return
        }
        val cmd = commands[commandIndex]
        when (cmd.type) {
            "DELAY" -> handler.postDelayed({
                commandIndex++
                runNext(commands)
            }, cmd.durationMs.coerceIn(1L, 60000L))
            "SWIPE" -> {
                val path = Path().apply { moveTo(cmd.x, cmd.y); lineTo(cmd.x2, cmd.y2) }
                dispatch(path, cmd.durationMs.coerceIn(1L, 60000L)) {
                    commandIndex++
                    runNext(commands)
                }
            }
            "LONG_CLICK" -> {
                val path = Path().apply { moveTo(cmd.x, cmd.y) }
                dispatch(path, cmd.durationMs.coerceIn(500L, 60000L)) {
                    commandIndex++
                    runNext(commands)
                }
            }
            "MULTICLICK" -> {
                if (repeatIndex < cmd.repeat.coerceIn(1, 20)) {
                    repeatIndex++
                    val path = Path().apply { moveTo(cmd.x, cmd.y) }
                    dispatch(path, 45L) {
                        handler.postDelayed({ runNext(commands) }, 65L)
                    }
                } else {
                    repeatIndex = 0
                    commandIndex++
                    runNext(commands)
                }
            }
            else -> {
                val path = Path().apply { moveTo(cmd.x, cmd.y) }
                dispatch(path, 45L) {
                    commandIndex++
                    runNext(commands)
                }
            }
        }
    }

    private fun dispatch(path: Path, duration: Long, done: () -> Unit) {
        if (!running.get()) return
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, duration))
            .build()
        val accepted = dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) { done() }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                running.set(false)
            }
        }, handler)
        if (!accepted) running.set(false)
    }

    override fun onInterrupt() { stopMacro() }

    override fun onDestroy() {
        stopMacro()
        stopService(Intent(this, MacroOverlayService::class.java))
        super.onDestroy()
    }

    companion object {
        @Volatile var instance: MacroAccessibilityService? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
