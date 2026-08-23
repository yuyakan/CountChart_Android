package com.kanbe1365.countergraph.ui

import androidx.lifecycle.ViewModel
import com.kanbe1365.countergraph.data.ChartEntry
import com.kanbe1365.countergraph.data.ChartSortOrder
import com.kanbe1365.countergraph.data.CountGroup
import com.kanbe1365.countergraph.data.CountUnitStore
import com.kanbe1365.countergraph.data.DataList
import com.kanbe1365.countergraph.data.GroupBar
import com.kanbe1365.countergraph.data.GroupItem
import com.kanbe1365.countergraph.data.GroupStore
import com.kanbe1365.countergraph.data.PieColorStore
import com.kanbe1365.countergraph.data.SettingStore
import com.kanbe1365.countergraph.ui.theme.ChartPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ファイル詳細画面（棒/円/ランキング/グループ）で共有する状態。
 * iOS では Setting/BarChartViewModel/PieChartViewModel/CountUnit/GroupStore に分かれていたものを、
 * 1ファイル=1画面なのでまとめて保持する。
 */
class FileViewModel(val fileId: String) : ViewModel() {

    private var dataList = DataList(fileId)
    private var groupStore = GroupStore(fileId)

    private val _title = MutableStateFlow(SettingStore.title(fileId))
    val title: StateFlow<String> = _title.asStateFlow()

    private val _countUnit = MutableStateFlow(CountUnitStore.value(fileId))
    val countUnit: StateFlow<Int> = _countUnit.asStateFlow()

    // 変更のたびにインクリメントして再構成のトリガーにする。
    private val _revision = MutableStateFlow(0)
    val revision: StateFlow<Int> = _revision.asStateFlow()

    private val _groups = MutableStateFlow(groupStore.groups)
    val groups: StateFlow<List<CountGroup>> = _groups.asStateFlow()

    private fun bump() { _revision.value += 1 }

    /** 他タブ・他画面での変更を反映するため最新データを読み直す。 */
    fun reload() {
        dataList = DataList(fileId)
        groupStore.reload()
        _title.value = SettingStore.title(fileId)
        _countUnit.value = CountUnitStore.value(fileId)
        _groups.value = groupStore.groups
        bump()
    }

    // MARK: - タイトル

    fun setTitle(value: String) {
        _title.value = value
        SettingStore.saveTitle(fileId, value)
    }

    // MARK: - カウント幅

    fun setCountUnit(value: Int) {
        if (value <= 0) return
        _countUnit.value = value
        CountUnitStore.save(fileId, value)
    }

    // MARK: - 項目

    fun count(): Int = dataList.count()

    fun canAdd(): Boolean = dataList.count() < DataList.MAX_DATA_COUNT

    /** 棒グラフ・ランキング用のエントリ（既定パレット色）。 */
    fun barEntries(order: ChartSortOrder): List<ChartEntry> {
        val base = (0 until dataList.count()).map { index ->
            ChartEntry(
                id = index,
                name = dataList.name(index),
                value = dataList.value(index),
                color = ChartPalette[index % ChartPalette.size],
            )
        }
        return sort(base, order)
    }

    /** 円グラフ用のエントリ（保存色＋パーセント付き）。 */
    fun pieEntries(order: ChartSortOrder): List<ChartEntry> {
        val colors = PieColorStore.colors(fileId, dataList.count())
        val percents = dataList.ratios().map { String.format("%.1f", it * 100) + "%" }
        val base = (0 until dataList.count()).map { index ->
            ChartEntry(
                id = index,
                name = dataList.name(index),
                value = dataList.value(index),
                color = colors.getOrElse(index) { ChartPalette[index % ChartPalette.size] },
                percent = percents.getOrElse(index) { "" },
            )
        }
        return sort(base, order)
    }

    private fun sort(list: List<ChartEntry>, order: ChartSortOrder): List<ChartEntry> = when (order) {
        ChartSortOrder.ENTRY -> list
        ChartSortOrder.DESCENDING -> list.sortedByDescending { it.value }
        ChartSortOrder.ASCENDING -> list.sortedBy { it.value }
    }

    fun name(index: Int): String = dataList.name(index)
    fun value(index: Int): Int = dataList.value(index)

    fun plus(index: Int, value: Int) { dataList.plus(index, value); bump() }
    fun minus(index: Int, value: Int) { dataList.minus(index, value); bump() }
    fun removeData(index: Int) { dataList.removeData(index); bump() }

    fun updateName(index: Int, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        dataList.updateName(index, trimmed)
        bump()
    }

    fun updateValue(index: Int, value: Int) {
        dataList.updateValue(index, value)
        bump()
    }

    /** 項目を追加する。上限超過なら false を返す（呼び出し側でアラート表示）。 */
    fun addData(name: String, value: Int): Boolean {
        if (dataList.count() >= DataList.MAX_DATA_COUNT) return false
        dataList.add(value, name)
        bump()
        return true
    }

    // MARK: - グループ

    fun groupItems(): List<GroupItem> =
        (0 until dataList.count()).map { index ->
            GroupItem(
                id = index,
                name = dataList.name(index),
                value = dataList.value(index),
                groupId = dataList.groupId(index),
            )
        }

    /** グループごとの合計棒。groups の順で並べ、値0でも表示する。未所属は含めない。 */
    fun groupBars(): List<GroupBar> {
        val items = groupItems()
        return groupStore.groups.map { group ->
            val total = items.filter { it.groupId == group.id }.sumOf { it.value }
            GroupBar(id = group.id, name = group.name, value = total, color = group.color)
        }
    }

    fun group(id: String?): CountGroup? = groupStore.group(id)

    fun addGroup(name: String) {
        groupStore.add(name)
        _groups.value = groupStore.groups
        bump()
    }

    fun removeGroup(id: String) {
        // グループ削除時、その所属項目を未所属に戻す。
        for (index in 0 until dataList.count()) {
            if (dataList.groupId(index) == id) dataList.updateGroup(index, null)
        }
        groupStore.remove(id)
        _groups.value = groupStore.groups
        bump()
    }

    fun setItemGroup(index: Int, groupId: String?) {
        dataList.updateGroup(index, groupId)
        bump()
    }
}
