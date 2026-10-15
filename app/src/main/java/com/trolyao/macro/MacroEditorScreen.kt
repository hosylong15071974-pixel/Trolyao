package com.trolyao.macro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

private val Bg = Color(0xFF0B0D12)
private val CardBg = Color(0xFF151923)
private val Accent = Color(0xFF31D7E8)
private val Dim = Color(0xFF9AA4B5)

@Composable
fun MacroEditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val list = remember { mutableStateListOf<MacroCommand>().apply { addAll(MacroStore.commands(context)) } }
    var type by remember { mutableStateOf("CLICK") }
    var x by remember { mutableStateOf("500") }
    var y by remember { mutableStateOf("800") }
    var x2 by remember { mutableStateOf("700") }
    var y2 by remember { mutableStateOf("800") }
    var duration by remember { mutableStateOf("100") }
    var repeat by remember { mutableStateOf("1") }
    var status by remember { mutableStateOf("") }

    Surface(Modifier.fillMaxSize(), color = Bg) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) }
                Column(Modifier.weight(1f)) {
                    Text("MACRO EDITOR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Lệnh được chạy theo thứ tự từ trên xuống", color = Dim, fontSize = 11.sp)
                }
                IconButton(onClick = {
                    MacroStore.saveCommands(context, list.toList())
                    status = "Đã lưu ${list.size} lệnh"
                }) { Icon(Icons.Filled.Save, null, tint = Accent) }
            }
            Spacer(Modifier.height(10.dp))
            Text("LOẠI LỆNH", color = Dim, fontSize = 11.sp)
            val types = listOf("CLICK", "MULTICLICK", "SWIPE", "DELAY", "LONG_CLICK")
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                types.forEach { item ->
                    FilterChip(selected = type == item, onClick = { type = item }, label = {
                        Text(when(item) { "MULTICLICK" -> "Multi"; "LONG_CLICK" -> "Long"; else -> item.lowercase().replaceFirstChar { it.uppercase() } }, fontSize = 10.sp)
                    })
                }
            }
            if (type != "DELAY") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("X", x, { x = it }, Modifier.weight(1f))
                    NumberField("Y", y, { y = it }, Modifier.weight(1f))
                }
            }
            if (type == "SWIPE") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("X kết thúc", x2, { x2 = it }, Modifier.weight(1f))
                    NumberField("Y kết thúc", y2, { y2 = it }, Modifier.weight(1f))
                }
            }
            if (type == "DELAY" || type == "SWIPE" || type == "LONG_CLICK") {
                NumberField(if (type == "DELAY") "Thời gian chờ (ms)" else "Thời lượng (ms)", duration, { duration = it }, Modifier.fillMaxWidth())
            }
            if (type == "MULTICLICK") {
                NumberField("Số lần (1–20)", repeat, { repeat = it }, Modifier.fillMaxWidth())
            }
            Button(
                onClick = {
                    val cmd = MacroCommand(
                        type = type,
                        x = x.toFloatOrNull() ?: 500f,
                        y = y.toFloatOrNull() ?: 800f,
                        x2 = x2.toFloatOrNull() ?: 700f,
                        y2 = y2.toFloatOrNull() ?: 800f,
                        durationMs = (duration.toLongOrNull() ?: 100L).coerceIn(1L, 60000L),
                        repeat = (repeat.toIntOrNull() ?: 1).coerceIn(1, 20)
                    )
                    list.add(cmd)
                    status = "Đã thêm lệnh ${list.size}"
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black)
            ) { Text("＋ Thêm lệnh") }
            if (status.isNotBlank()) Text(status, color = Accent, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(8.dp))
            Text("DANH SÁCH LỆNH (${list.size})", color = Dim, fontSize = 11.sp)
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                itemsIndexed(list) { index, cmd ->
                    Card(colors = CardDefaults.cardColors(containerColor = CardBg), shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${index + 1}. ${cmd.type}", fontWeight = FontWeight.SemiBold, color = Accent)
                                Text(
                                    if (cmd.type == "DELAY") "Chờ ${cmd.durationMs} ms"
                                    else if (cmd.type == "SWIPE") "(${cmd.x}, ${cmd.y}) → (${cmd.x2}, ${cmd.y2}) • ${cmd.durationMs} ms"
                                    else "X=${cmd.x}, Y=${cmd.y}" + if (cmd.type == "MULTICLICK") " • ${cmd.repeat} lần" else "",
                                    color = Dim, fontSize = 11.sp
                                )
                            }
                            IconButton(onClick = { list.removeAt(index) }) { Icon(Icons.Filled.Delete, null, tint = Color(0xFFFF4FA3)) }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { list.clear(); status = "Đã xóa danh sách" }, modifier = Modifier.weight(1f)) { Text("Xóa tất cả") }
                Button(onClick = {
                    MacroStore.saveCommands(context, list.toList())
                    status = "Đã lưu ${list.size} lệnh"
                }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color.Black)) {
                    Text("Lưu macro")
                }
            }
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = { onValueChange(it.filter { c -> c.isDigit() || c == '-' }) },
        label = { Text(label) }, modifier = modifier.padding(vertical = 4.dp),
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Accent, focusedLabelColor = Accent,
            unfocusedBorderColor = Color(0xFF343B49), unfocusedLabelColor = Dim
        )
    )
}
