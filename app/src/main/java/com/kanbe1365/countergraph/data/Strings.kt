package com.kanbe1365.countergraph.data

import android.content.Context

/**
 * Compose の外（データ層）から文字列リソースを引くための薄いヘルパ。
 * サンプル名やデフォルトタイトルなど、保存する文言のローカライズに使う。
 */
object Strings {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /** 文字列リソース名（例: "name_ann"）から現在ロケールの文字列を返す。 */
    fun get(resName: String): String {
        val id = appContext.resources.getIdentifier(resName, "string", appContext.packageName)
        return if (id != 0) appContext.getString(id) else resName
    }
}
