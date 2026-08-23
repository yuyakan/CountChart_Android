package com.kanbe1365.countergraph.data

import androidx.compose.ui.graphics.Color

/** ファイル詳細画面のタブ種別。 */
enum class ChartType { BAR, PIE, RANKING, GROUP }

/** 棒グラフの向き（縦棒 / 横棒）。 */
enum class BarOrientation { VERTICAL, HORIZONTAL }

/** グラフ・凡例の並び順。iOS の ChartSortOrder に相当。 */
enum class ChartSortOrder { ENTRY, DESCENDING, ASCENDING }

/** 棒1本 / 凡例1行ぶんのデータ。id は dataList 内の元 index。 */
data class ChartEntry(
    val id: Int,
    val name: String,
    val value: Int,
    val color: Color,
    val percent: String = "",
)

/** グループ集計の棒1本分。 */
data class GroupBar(
    val id: String,
    val name: String,
    val value: Int,
    val color: Color,
)

/** グループ割当UI用の項目1件。 */
data class GroupItem(
    val id: Int,
    val name: String,
    val value: Int,
    val groupId: String?,
)
