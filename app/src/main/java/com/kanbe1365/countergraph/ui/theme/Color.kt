package com.kanbe1365.countergraph.ui.theme

import androidx.compose.ui.graphics.Color

// アプリ共通のブランドカラー（ティール/シアン系）。iOS 版と同じ値。
// ライト: 標準ティール、ダーク: 黒地で映えるよう明るめ。
val BrandLight = Color(red = 0.0f, green = 0.62f, blue = 0.65f)
val BrandDark = Color(red = 0.0f, green = 0.70f, blue = 0.73f)

// チャート凡例の既定パレット（iOS の PieChartViewModel.defaultColors と同順）。
val ChartPalette = listOf(
    Color(0xFFFF9500), // orange
    Color(0xFF34C759), // green
    Color(0xFF007AFF), // blue
    Color(0xFFFF3B30), // red
    Color(0xFFFFCC00), // yellow
    Color(0xFFFF2D55), // pink
    Color(0xFFAF52DE), // purple
    Color(0xFF00C7BE), // mint
    Color(0xFF5856D6), // indigo
    Color(0xFF32ADE6), // cyan
)

// グループ色のパレット（iOS の GroupStore.paletteColors と同順）。
val GroupPalette = listOf(
    Color(0xFFFF3B30), // red
    Color(0xFF007AFF), // blue
    Color(0xFF34C759), // green
    Color(0xFFFF9500), // orange
    Color(0xFFAF52DE), // purple
    Color(0xFFFF2D55), // pink
    Color(0xFF30B0C7), // teal
    Color(0xFF5856D6), // indigo
)
