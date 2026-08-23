package com.kanbe1365.countergraph.data

import androidx.compose.ui.graphics.Color
import com.kanbe1365.countergraph.ui.theme.ChartPalette
import org.json.JSONArray

/**
 * 円グラフの扇形色を fileId ごとに保持する。iOS の pieColors_file{id} に相当。
 * 保存が無ければ既定パレットを使い、件数不足ぶんはパレットで補う。
 */
object PieColorStore {

    fun colors(fileId: String, count: Int): List<Color> {
        val raw = Prefs.get().getString("pieColors_file$fileId", null)
        val base: MutableList<Color> = if (raw != null) {
            try {
                val array = JSONArray(raw)
                (0 until array.length()).map { Color(array.getLong(it).toULong()) }.toMutableList()
            } catch (_: Exception) {
                defaultColors(count)
            }
        } else {
            defaultColors(count)
        }
        // 不足ぶんを既定色で補う。
        var index = base.size
        while (base.size < count) {
            base.add(ChartPalette[index % ChartPalette.size])
            index++
        }
        return base
    }

    private fun defaultColors(count: Int): MutableList<Color> =
        (0 until maxOf(count, ChartPalette.size)).map { ChartPalette[it % ChartPalette.size] }.toMutableList()
}
