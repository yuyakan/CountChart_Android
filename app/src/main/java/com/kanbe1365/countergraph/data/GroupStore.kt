package com.kanbe1365.countergraph.data

import androidx.compose.ui.graphics.Color
import com.kanbe1365.countergraph.ui.theme.GroupPalette
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** 項目をまとめるグループ。名前と色を持つ。iOS の CountGroup に相当。 */
data class CountGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Color,
)

/**
 * グループ定義を fileId ごとに永続化する。iOS の GroupStore に相当。
 * "groups_file{id}" に JSON 配列で保存する。
 */
class GroupStore(private val fileId: String) {

    private val storageKey get() = "groups_file$fileId"

    private val _groups: MutableList<CountGroup> = mutableListOf()
    val groups: List<CountGroup> get() = _groups.toList()

    init {
        reload()
    }

    fun reload() {
        _groups.clear()
        val raw = Prefs.get().getString(storageKey, null) ?: return
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                _groups.add(
                    CountGroup(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        color = Color(obj.getLong("color").toULong()),
                    )
                )
            }
        } catch (_: Exception) {
            _groups.clear()
        }
    }

    fun group(id: String?): CountGroup? {
        if (id == null) return null
        return _groups.firstOrNull { it.id == id }
    }

    /** 新しいグループを追加する。色はパレットから未使用のものを優先する。 */
    fun add(name: String): CountGroup {
        val used = _groups.map { it.color }.toSet()
        val color = GroupPalette.firstOrNull { it !in used }
            ?: GroupPalette[_groups.size % GroupPalette.size]
        val group = CountGroup(name = name, color = color)
        _groups.add(group)
        save()
        return group
    }

    fun remove(id: String) {
        _groups.removeAll { it.id == id }
        save()
    }

    private fun save() {
        val array = JSONArray()
        _groups.forEach { group ->
            array.put(
                JSONObject().apply {
                    put("id", group.id)
                    put("name", group.name)
                    put("color", group.color.value.toLong())
                }
            )
        }
        Prefs.get().edit().putString(storageKey, array.toString()).apply()
    }
}
