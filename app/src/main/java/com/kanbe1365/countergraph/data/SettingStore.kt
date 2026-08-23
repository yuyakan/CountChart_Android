package com.kanbe1365.countergraph.data

/**
 * ファイル単位の設定（現状はタイトルのみ）。iOS の Setting に相当。
 * 色はブランドカラー＋システム配色に統一しているため保持しない。
 */
object SettingStore {

    fun title(fileId: String): String {
        val prefs = Prefs.get()
        val saved = prefs.getString("Title_file$fileId", null)
        return when {
            saved != null -> saved
            fileId == "0" -> Strings.get("result")
            else -> ""
        }
    }

    fun saveTitle(fileId: String, title: String) {
        Prefs.get().edit().putString("Title_file$fileId", title).apply()
    }
}
