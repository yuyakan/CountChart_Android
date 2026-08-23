package com.kanbe1365.countergraph.data

import org.json.JSONObject

/**
 * 1項目分のデータ。iOS の PersonalData に相当。
 * groupId は未所属のとき null（旧データとの後方互換）。
 */
data class PersonalData(
    val value: Int,
    val name: String,
    val groupId: String? = null,
)

/**
 * 1ファイル分の項目リスト。iOS の DataList に相当し、
 * "data{index}_file{fileId}" キーに項目ごとの JSON を保存する。
 */
class DataList(val fileId: String) {

    private val list: MutableList<PersonalData> = mutableListOf()

    companion object {
        /** 1ファイルあたりに保持できる項目の最大件数。 */
        const val MAX_DATA_COUNT = 10

        private fun key(index: Int, fileId: String) = "data${index}_file$fileId"
    }

    init {
        val prefs = Prefs.get()
        if (prefs.getString(key(0, fileId), null) != null) {
            loadFromPrefs()
        } else {
            // 初回起動時のみ、fileId "0" にサンプルを入れる。
            if (fileId == "0" && !prefs.getBoolean("isSecondLaunched", false)) {
                prefs.edit().putBoolean("isSecondLaunched", true).apply()
                list.addAll(
                    listOf(
                        PersonalData(80, Strings.get("name_ann")),
                        PersonalData(230, Strings.get("name_tom")),
                        PersonalData(500, Strings.get("name_bob")),
                        PersonalData(320, Strings.get("name_casey")),
                        PersonalData(120, Strings.get("name_brian")),
                    )
                )
            }
            save()
        }
    }

    private fun loadFromPrefs() {
        val prefs = Prefs.get()
        for (index in 0 until MAX_DATA_COUNT) {
            val json = prefs.getString(key(index, fileId), null) ?: break
            decode(json)?.let { list.add(it) }
        }
    }

    private fun decode(json: String): PersonalData? = try {
        val obj = JSONObject(json)
        PersonalData(
            value = obj.getInt("value"),
            name = obj.getString("name"),
            groupId = if (obj.isNull("groupId")) null else obj.optString("groupId", null),
        )
    } catch (_: Exception) {
        null
    }

    private fun encode(data: PersonalData): String = JSONObject().apply {
        put("value", data.value)
        put("name", data.name)
        put("groupId", data.groupId ?: JSONObject.NULL)
    }.toString()

    private fun save() {
        val editor = Prefs.get().edit()
        list.forEachIndexed { index, data ->
            editor.putString(key(index, fileId), encode(data))
        }
        editor.apply()
    }

    // MARK: - 読み取り

    fun max(): Int = list.maxOfOrNull { it.value } ?: 0

    fun count(): Int = list.size

    fun value(index: Int): Int = list[index].value

    fun name(index: Int): String = list[index].name

    fun names(): List<String> = list.map { it.name }

    fun groupId(index: Int): String? = list[index].groupId

    /** 各項目が全体に占める割合。 */
    fun ratios(): List<Double> {
        val sum = list.sumOf { it.value }.toDouble()
        if (sum == 0.0) return list.map { 0.0 }
        return list.map { it.value / sum }
    }

    fun items(): List<PersonalData> = list.toList()

    // MARK: - 変更（都度保存）

    fun plus(index: Int, value: Int) {
        val old = list[index]
        list[index] = old.copy(value = old.value + value)
        save()
    }

    fun minus(index: Int, value: Int) {
        val old = list[index]
        list[index] = old.copy(value = old.value - value)
        save()
    }

    fun add(value: Int, name: String) {
        list.add(PersonalData(value, name))
        save()
    }

    fun updateName(index: Int, name: String) {
        if (index !in list.indices) return
        list[index] = list[index].copy(name = name)
        save()
    }

    fun updateValue(index: Int, value: Int) {
        if (index !in list.indices) return
        list[index] = list[index].copy(value = value)
        save()
    }

    fun updateGroup(index: Int, groupId: String?) {
        if (index !in list.indices) return
        list[index] = list[index].copy(groupId = groupId)
        save()
    }

    fun removeData(index: Int) {
        list.removeAt(index)
        // 余った末尾のキーを削除してから保存し直す。
        val editor = Prefs.get().edit()
        for (i in list.size until MAX_DATA_COUNT) {
            editor.remove(key(i, fileId))
        }
        editor.apply()
        save()
    }
}
