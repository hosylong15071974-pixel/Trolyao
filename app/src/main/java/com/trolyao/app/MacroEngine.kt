package com.trolyao.app

import android.os.IBinder
import rikka.shizuku.Shizuku

object MacroEngine {
    fun shizukuGranted(): Boolean = try { Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED } catch (_: Throwable) { false }
    fun requestShizukuPermission() { try { if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) Shizuku.requestPermission(1001) } catch (_: Throwable) {} }
    fun command(command: String): Boolean {
        if (!shizukuGranted()) return false
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            process.waitFor() == 0
        } catch (_: Throwable) { false }
    }
    fun tap(x: Int, y: Int) = command("input tap $x $y")
    fun longPress(x: Int, y: Int, ms: Int) = command("input swipe $x $y $x $y ${ms.coerceIn(100, 5000)}")
    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, ms: Int) = command("input swipe $x1 $y1 $x2 $y2 ${ms.coerceIn(50, 10000)}")
}
