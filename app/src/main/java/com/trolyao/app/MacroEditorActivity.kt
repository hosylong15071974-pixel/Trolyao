package com.trolyao.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Dark = Color(0xFF0A0B12); private val CardDark = Color(0xFF151827); private val Neon = Color(0xFF00E5FF); private val Magenta = Color(0xFFFF36C7)
class MacroEditorActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { EditorScreen() } }
 @Composable private fun EditorScreen() {
  val prefs = getSharedPreferences("trolyao", 0)
  var x by remember { mutableStateOf(prefs.getInt("x1", 500).toString()) }; var y by remember { mutableStateOf(prefs.getInt("y1", 800).toString()) }
  var size by remember { mutableFloatStateOf(prefs.getFloat("buttonSize", 1f)) }; var opacity by remember { mutableFloatStateOf(prefs.getFloat("buttonOpacity", .94f)) }
  var type by remember { mutableStateOf("Click") }; var saved by remember { mutableStateOf(false) }
  MaterialTheme(colorScheme = darkColorScheme(primary = Neon, background = Dark, surface = CardDark)) {
   Surface(Modifier.fillMaxSize(), color = Dark) { LazyColumn(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
    item { Text("TAB • MACRO EDITOR", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black); Text("Thiết lập thao tác cho Floating Menu", color = Neon) }
    item { PanelBox { Text("Loại thao tác", color = Color.White, fontWeight = FontWeight.Bold); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Click", "Long press", "Swipe", "Delay", "MultiClick").forEach { label -> FilterChip(selected = type == label, onClick = { type = label }, label = { Text(label) }) } } } }
    item { PanelBox { Text("Tọa độ chạm", color = Color.White, fontWeight = FontWeight.Bold); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedTextField(value = x, onValueChange = { x = it.filter(Char::isDigit) }, label = { Text("X") }, modifier = Modifier.weight(1f)); OutlinedTextField(value = y, onValueChange = { y = it.filter(Char::isDigit) }, label = { Text("Y") }, modifier = Modifier.weight(1f)) }; Text("Nhập tọa độ màn hình theo pixel. Mặc định 500, 800.", color = Color.LightGray, fontSize = 12.sp) } }
    item { PanelBox { Text("Multiply Button Runner", color = Color.White, fontWeight = FontWeight.Bold); Text("Khoảng chờ mục tiêu: 2 ms", color = Neon); Text("Nhấn giữ nút Multiply để lặp liên tục; thả tay để dừng. Tốc độ thực tế phụ thuộc Android và thời gian xử lý lệnh chạm.", color = Color.LightGray, fontSize = 12.sp) } }
    item { PanelBox { Text("Kích thước nút nổi", color = Color.White, fontWeight = FontWeight.Bold); Slider(value = size, onValueChange = { size = it }, valueRange = .7f..1.4f); Text("${(size * 100).toInt()}%", color = Neon); Text("Độ mờ nút", color = Color.White); Slider(value = opacity, onValueChange = { opacity = it }, valueRange = .2f..1f); Text("${(opacity * 100).toInt()}%", color = Neon) } }
    item { Button(onClick = { prefs.edit().putInt("x1", x.toIntOrNull() ?: 500).putInt("y1", y.toIntOrNull() ?: 800).putInt("multiplyDelay", 2).putFloat("buttonSize", size).putFloat("buttonOpacity", opacity).putString("stepType", type).apply(); saved = true }, modifier = Modifier.fillMaxWidth()) { Text("Lưu cấu hình") }; if (saved) Text("Đã lưu cấu hình trên thiết bị.", color = Color(0xFF5DFFAE)) }
    item { OutlinedButton(onClick = { finish() }, modifier = Modifier.fillMaxWidth()) { Text("Quay lại Floating Menu") } }
   } }
  }
 }
 @Composable private fun PanelBox(content: @Composable ColumnScope.() -> Unit) { Card(colors = CardDefaults.cardColors(containerColor = CardDark), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Neon, Magenta))), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content) } }
}
