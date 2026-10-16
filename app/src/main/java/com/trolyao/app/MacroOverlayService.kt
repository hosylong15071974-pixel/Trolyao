package com.trolyao.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.MotionEvent
import java.util.concurrent.atomic.AtomicBoolean
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MacroOverlayService : Service() {
    private lateinit var wm: WindowManager
    private var root: LinearLayout? = null
    private var menuOpen = false
    private val multiplyHeld = AtomicBoolean(false)
    private var multiplyThread: Thread? = null
    private val channel = "trolyao_overlay"
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() { super.onCreate(); startAsForeground(); wm = getSystemService(WINDOW_SERVICE) as WindowManager; showHandle() }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { if (root == null) showHandle(); return START_STICKY }
    private fun startAsForeground() {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(channel, "Trolyao floating controls", NotificationManager.IMPORTANCE_LOW))
        val launch = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, channel) else Notification.Builder(this)
        startForeground(22, notification.setContentTitle("Trolyao đang chạy").setContentText("Nút macro nổi đang bật").setSmallIcon(android.R.drawable.ic_media_play).setContentIntent(launch).build())
    }
    private fun params() = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
        if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.END; x = 12; y = 240 }
    private fun bg(stroke: Int = 0xFF00E5FF.toInt()) = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(0xFF171A2A.toInt(), 0xFF10121D.toInt())).apply { cornerRadius = 32f; setStroke(3, stroke) }
    private fun text(label: String, stroke: Int = 0xFF00E5FF.toInt(), onClick: () -> Unit): TextView = TextView(this).apply {
        text = label; setTextColor(-1); textSize = 14f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(22, 15, 22, 15); background = bg(stroke); alpha = .94f
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); lp.setMargins(6, 5, 6, 5); layoutParams = lp; setOnClickListener { onClick() }
    }
    private fun showHandle() {
        val handle = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(text("T", 0xFFFF36C7.toInt()) { toggleMenu() }) }
        root = handle
        try { wm.addView(handle, params()) } catch (_: Exception) { stopSelf() }
    }
    private fun toggleMenu() { if (menuOpen) { showHandle(); menuOpen = false } else { root?.let { try { wm.removeView(it) } catch (_: Exception) {} }; val panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = bg(0xFFFF36C7.toInt()); setPadding(5, 5, 5, 5)
        addView(text("Macro Runner", 0xFF00E5FF.toInt()) { runMacroOnce() })
        val multiplyButton = text("Multiply Button Runner", 0xFFFF36C7.toInt()) { }
        multiplyButton.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startMultiplyWhileHeld()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stopMultiplyWhileHeld()
                    true
                }
                else -> true
            }
        }
        addView(multiplyButton)
        addView(text("Tab", 0xFF9A72FF.toInt()) { startActivity(Intent(this@MacroOverlayService, MacroEditorActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); closeMenu() })
    }; root = panel; try { wm.addView(panel, params()) } catch (_: Exception) {}; menuOpen = true } }
    private fun closeMenu() { root?.let { try { wm.removeView(it) } catch (_: Exception) {} }; root = null; menuOpen = false; showHandle() }
    private fun runMacroOnce() {
        val prefs = getSharedPreferences("trolyao", 0); val x = prefs.getInt("x1", 500); val y = prefs.getInt("y1", 800)
        val ok = MacroEngine.tap(x, y); Toast.makeText(this, if (ok) "Macro Runner: đã gửi lệnh" else "Cần cấp quyền Shizuku", Toast.LENGTH_SHORT).show()
    }
    private fun startMultiplyWhileHeld() {
        if (!MacroEngine.shizukuGranted()) {
            Toast.makeText(this, "Cần cấp quyền Shizuku", Toast.LENGTH_SHORT).show()
            return
        }
        if (!multiplyHeld.compareAndSet(false, true)) return
        multiplyThread = Thread {
            val prefs = getSharedPreferences("trolyao", 0)
            val x = prefs.getInt("x1", 500)
            val y = prefs.getInt("y1", 800)
            while (multiplyHeld.get() && !Thread.currentThread().isInterrupted) {
                MacroEngine.tap(x, y)
                try {
                    Thread.sleep(2L)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }.apply { name = "Trolyao-Multiply"; isDaemon = true; start() }
    }

    private fun stopMultiplyWhileHeld() {
        multiplyHeld.set(false)
        multiplyThread?.interrupt()
        multiplyThread = null
    }
    override fun onDestroy() { stopMultiplyWhileHeld(); root?.let { try { wm.removeView(it) } catch (_: Exception) {} }; root = null; super.onDestroy() }
}
