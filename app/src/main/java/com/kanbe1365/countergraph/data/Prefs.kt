package com.kanbe1365.countergraph.data

import android.content.Context
import android.content.SharedPreferences

/**
 * アプリ全体で共有する SharedPreferences。iOS の UserDefaults.standard に相当する。
 * キー体系も iOS 版に合わせている（例: "data0_file{id}", "Title_file{id}", "fileIds"）。
 */
object Prefs {
    private const val NAME = "countergraph"

    @Volatile
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            prefs = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        }
    }

    fun get(): SharedPreferences = prefs
}
