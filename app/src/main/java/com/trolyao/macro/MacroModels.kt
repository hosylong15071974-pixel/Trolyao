package com.trolyao.macro

import org.json.JSONArray
import org.json.JSONObject

data class MacroCommand(
    val type: String = "CLICK",
    val x: Float = 500f,
    val y: Float = 800f,
    val x2: Float = 700f,
    val y2: Float = 800f,
    val durationMs: Long = 100L,
    val repeat: Int = 1
) {
    fun toJson() = JSONObject().apply {
        put("type", type); put("x", x.toDouble()); put("y", y.toDouble())
        put("x2", x2.toDouble()); put("y2", y2.toDouble())
        put("durationMs", durationMs); put("repeat", repeat)
    }

    companion object {
        fun fromJson(o: JSONObject) = MacroCommand(
            type = o.optString("type", "CLICK"),
            x = o.optDouble("x", 500.0).toFloat(),
            y = o.optDouble("y", 800.0).toFloat(),
            x2 = o.optDouble("x2", 700.0).toFloat(),
            y2 = o.optDouble("y2", 800.0).toFloat(),
            durationMs = o.optLong("durationMs", 100L).coerceAtLeast(1L),
            repeat = o.optInt("repeat", 1).coerceIn(1, 20)
        )
    }
}

object MacroStore {
    private const val PREFS = "trolyao_store"
    private const val KEY_PACKAGE = "selected_package"
    private const val KEY_LABEL = "selected_label"
    private const val KEY_COMMANDS = "macro_commands"

    fun prefs(context: android.content.Context) =
        context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)

    fun selectedPackage(context: android.content.Context) = prefs(context).getString(KEY_PACKAGE, "") ?: ""
    fun selectedLabel(context: android.content.Context) = prefs(context).getString(KEY_LABEL, "") ?: ""

    fun setSelectedApp(context: android.content.Context, packageName: String, label: String) {
        prefs(context).edit().putString(KEY_PACKAGE, packageName).putString(KEY_LABEL, label).apply()
    }

    fun commands(context: android.content.Context): MutableList<MacroCommand> {
        val raw = prefs(context).getString(KEY_COMMANDS, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            MutableList(arr.length()) { i -> MacroCommand.fromJson(arr.getJSONObject(i)) }
        } catch (_: Exception) { mutableListOf() }
    }

    fun saveCommands(context: android.content.Context, commands: List<MacroCommand>) {
        val arr = JSONArray()
        commands.forEach { arr.put(it.toJson()) }
        prefs(context).edit().putString(KEY_COMMANDS, arr.toString()).apply()
    }
}
