package com.trolyao.macro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Page = Color(0xFF0B0D12)
private val Panel = Color(0xFF151923)
private val Panel2 = Color(0xFF1D2330)
private val Cyan = Color(0xFF31D7E8)
private val Pink = Color(0xFFFF4FA3)
private val Muted = Color(0xFF9AA4B5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Cyan, secondary = Pink, background = Page, surface = Panel,
                onPrimary = Color.Black, onBackground = Color.White, onSurface = Color.White
            )) { TrolyaoHome() }
        }
    }
}

private data class InstalledApp(val label: String, val packageName: String)

@Composable
private fun TrolyaoHome() {
    val context = LocalContext.current
    var selectedLabel by remember { mutableStateOf(MacroStore.selectedLabel(context)) }
    var selectedPackage by remember { mutableStateOf(MacroStore.selectedPackage(context)) }
    var showApps by remember { mutableStateOf(false) }
    var showEditor by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    val commands = remember(refresh) { MacroStore.commands(context) }
    val apps = remember(showApps) { installedApps(context) }

    if (showEditor) {
        MacroEditorScreen(onBack = { showEditor = false; refresh++ })
        return
    }

    Surface(Modifier.fillMaxSize(), color = Page) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(46.dp).background(Panel2, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Bolt, null, tint = Cyan, modifier = Modifier.size(28.dp))
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("TROLYAO", fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("MACRO CONTROL CENTER", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
                    }
                    Icon(Icons.Filled.Settings, null, tint = Muted, modifier = Modifier.size(24.dp).clickable {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    })
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("QUYỀN TRỢ NĂNG", color = Muted, fontSize = 11.sp, letterSpacing = 1.sp)
                        Text(if (isAccessibilityEnabled(context)) "Dịch vụ đã bật" else "Cần bật dịch vụ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Để macro chạm màn hình, hãy bật Trolyao Macro Service trong Cài đặt > Trợ năng.", color = Muted, fontSize = 12.sp)
                        Button(
                            onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Panel2, contentColor = Cyan)
                        ) { Text("Mở cài đặt Trợ năng") }
                        if (!Settings.canDrawOverlays(context)) {
                            OutlinedButton(
                                onClick = { context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Cấp quyền hiển thị trên ứng dụng khác") }
                        } else {
                            Text("Quyền overlay: đã cấp", color = Cyan, fontSize = 12.sp)
                        }
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ỨNG DỤNG ĐƯỢC CHỌN", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { showApps = !showApps }) { Text(if (showApps) "Đóng" else "Thay đổi") }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(if (selectedLabel.isBlank()) "Chưa chọn game/app" else selectedLabel, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                        if (selectedPackage.isNotBlank()) Text(selectedPackage, color = Muted, fontSize = 11.sp)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { showApps = !showApps }, shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Color.Black)) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp)); Text("  Chọn ứng dụng")
                        }
                    }
                }
            }
            if (showApps) {
                items(apps, key = { it.packageName }) { app ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedLabel = app.label
                            selectedPackage = app.packageName
                            MacroStore.setSelectedApp(context, app.packageName, app.label)
                            showApps = false
                        },
                        colors = CardDefaults.cardColors(containerColor = Panel2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Apps, null, tint = Cyan)
                            Column(Modifier.weight(1f).padding(start = 10.dp)) {
                                Text(app.label, fontWeight = FontWeight.Medium)
                                Text(app.packageName, color = Muted, fontSize = 10.sp)
                            }
                            Text("CHỌN", color = Cyan, fontSize = 10.sp)
                        }
                    }
                }
            }
            item {
                Text("MACRO", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                ToolRow(Icons.Filled.Tune, "Custom Macro", "${commands.size} lệnh đã lưu", Cyan) { showEditor = true }
                ToolRow(Icons.Filled.Bolt, "RGB / Nút nổi", "Nút nổi xuất hiện khi app được chọn ở phía trước", Pink) {
                    if (Settings.canDrawOverlays(context)) {
                        context.startService(Intent(context, MacroOverlayService::class.java).setAction(MacroOverlayService.ACTION_SHOW))
                    } else {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                    }
                }
                ToolRow(Icons.Filled.Bookmarks, "Macro Saves", "Bản MVP lưu một bộ lệnh cục bộ", Cyan) {
                    android.widget.Toast.makeText(context, "Lệnh macro được lưu tự động trên thiết bị.", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("CÁCH DÙNG", color = Pink, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("1. Chọn game.  2. Cấp quyền overlay.  3. Bật Trolyao Macro Service trong Trợ năng.  4. Mở game, kéo nút nổi và chạm để chạy macro.", color = Muted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ToolRow(icon: ImageVector, title: String, description: String, accent: Color, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clickable(onClick = onClick)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(Panel2, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(23.dp))
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(3.dp))
                Text(description, color = Muted, fontSize = 11.sp)
            }
            Text("›", color = accent, fontSize = 26.sp)
        }
    }
}

private fun installedApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    return try {
        @Suppress("DEPRECATION")
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != context.packageName }
            .map { InstalledApp(pm.getApplicationLabel(it).toString(), it.packageName) }
            .sortedBy { it.label.lowercase() }
    } catch (_: Exception) { emptyList() }
}

private fun isAccessibilityEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    return manager.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .any { it.resolveInfo.serviceInfo.packageName == context.packageName }
}
