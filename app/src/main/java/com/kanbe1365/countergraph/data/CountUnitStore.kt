package com.kanbe1365.countergraph.data

/**
 * 1カウントあたりの増減単位を fileId ごとに永続化する。iOS の CountUnit に相当。
 * プリセット 1/10/100/1000 ＋任意入力に対応。
 */
object CountUnitStore {
    val presets = listOf(1, 10, 100, 1000)

    fun value(fileId: String): Int {
        val saved = Prefs.get().getInt("CountUnit_file$fileId", 0)
        return if (saved > 0) saved else 1
    }

    fun save(fileId: String, value: Int) {
        Prefs.get().edit().putInt("CountUnit_file$fileId", value).apply()
    }

    fun isCustom(value: Int): Boolean = value !in presets
}
