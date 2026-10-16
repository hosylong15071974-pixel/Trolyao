package com.trolyao.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.pm.PackageManager

private val Bg = Color(0xFF0A0B12)
private val Panel = Color(0xFF151827)
private val Cyan = Color(0xFF00E5FF)
private val Pink = Color(0xFFFF36C7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { TrolyaoHome() } }
    override fun onResume() { super.onResume() }
}

@Composable fun TrolyaoHome() {
    val context = LocalContext.current
    val activity = context as Activity
    var shizukuOk by remember { mutableStateOf(MacroEngine.shizukuGranted()) }
    var overlayOk by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var backgroundEnabled by remember { mutableStateOf(context.getSharedPreferences("trolyao", 0).getBoolean("background", false)) }
    var chosenApp by remember { mutableStateOf(context.getSharedPreferences("trolyao", 0).getString("chosenApp", "") ?: "") }
    var showApps by remember { mutableStateOf(false) }
    val apps = remember {
        context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { context.packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { context.packageManager.getApplicationLabel(it).toString().lowercase() }
    }
    LaunchedEffect(Unit) { shizukuOk = MacroEngine.shizukuGranted(); overlayOk = Settings.canDrawOverlays(context) }
    MaterialTheme(colorScheme = darkColorScheme(primary = Cyan, background = Bg, surface = Panel)) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("TROLYAO", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("MACRO CONTROL", color = Cyan, fontSize = 12.sp, letterSpacing = 3.sp)
                NeonPanel {
                    Text("Trạng thái quyền", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Spacer(Modifier.height(12.dp))
                    StatusLine("Shizuku", shizukuOk, if (shizukuOk) "Đã cấp quyền" else "Chưa cấp quyền")
                    Button(onClick = { if (ShizukuAvailable()) MacroEngine.requestShizukuPermission() else context.startActivity(Intent("moe.shizuku.manager.action.REQUEST_PERMISSION")) }, modifier = Modifier.fillMaxWidth()) { Text(if (shizukuOk) "Kiểm tra Shizuku" else "Cấp quyền Shizuku") }
                    Spacer(Modifier.height(8.dp))
                    StatusLine("Hiển thị trên ứng dụng khác", overlayOk, if (overlayOk) "Đã cho phép" else "Chưa cấp quyền")
                    OutlinedButton(onClick = { context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))) }, modifier = Modifier.fillMaxWidth()) { Text("Cấp quyền nút nổi") }
                }
                NeonPanel {
                    Text("Ứng dụng / trò chơi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(if (chosenApp.isBlank()) "Chưa chọn ứng dụng" else "Đã chọn: $chosenApp", color = Color.LightGray)
                    Button(onClick = { showApps = true }, modifier = Modifier.fillMaxWidth()) { Text("Thêm ứng dụng") }
                }
                NeonPanel {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) { Text("Chạy nền", color = Color.White, fontWeight = FontWeight.Bold); Text("Giữ nút nổi hoạt động khi có thể", color = Color.LightGray, fontSize = 12.sp) }
                        Switch(checked = backgroundEnabled, onCheckedChange = { value ->
                            backgroundEnabled = value
                            context.getSharedPreferences("trolyao", 0).edit().putBoolean("background", value).apply()
                            if (value && overlayOk) { context.startForegroundService(Intent(context, MacroOverlayService::class.java)) }
                            else context.stopService(Intent(context, MacroOverlayService::class.java))
                        })
                    }
                    Text("Android có thể giới hạn chạy nền tùy cài đặt pin của máy.", color = Color(0xFF9EA3B8), fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Text("Trolyao • Dark RGB UI", color = Color(0xFF6F748A), fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
    if (showApps) AlertDialog(onDismissRequest = { showApps = false }, title = { Text("Chọn ứng dụng") }, text = {
        LazyColumn(Modifier.heightIn(max = 420.dp)) { items(apps) { app ->
            val label = context.packageManager.getApplicationLabel(app).toString()
            TextButton(onClick = {
                chosenApp = label
                context.getSharedPreferences("trolyao", 0).edit().putString("chosenApp", label).putString("chosenPackage", app.packageName).apply()
                showApps = false
                if (backgroundEnabled && Settings.canDrawOverlays(context)) context.startForegroundService(Intent(context, MacroOverlayService::class.java))
            }, modifier = Modifier.fillMaxWidth()) { Text(label, color = Color.White) }
        } }
    }, confirmButton = { TextButton(onClick = { showApps = false }) { Text("Đóng") } })
}

private fun ShizukuAvailable(): Boolean = try { rikka.shizuku.Shizuku.pingBinder() } catch (_: Throwable) { false }

@Composable private fun StatusLine(title: String, ok: Boolean, subtitle: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, color = Color.White); Text(subtitle, color = if (ok) Color(0xFF5DFFAE) else Color(0xFFFF8795), fontSize = 12.sp) }
        Text(if (ok) "✓" else "×", color = if (ok) Color(0xFF5DFFAE) else Color(0xFFFF8795), fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(8.dp))
}

@Composable private fun NeonPanel(content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Cyan, Pink))), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
    }
}
