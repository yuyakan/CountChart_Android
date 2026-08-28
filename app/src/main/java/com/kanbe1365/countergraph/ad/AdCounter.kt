package com.kanbe1365.countergraph.ad

import com.kanbe1365.countergraph.data.Prefs

/**
 * 広告表示のための操作回数カウンター（アプリ全体で共通）。
 * iOS の AdCounter に相当。
 *
 * 詳細画面の編集「完了」・メニューの「追加」・「複製」を同じカウントで数え、
 * しきい値以上たまったら広告を表示し、表示後は0にリセットする。
 * メニューの「追加」「複製」は3回分、編集「完了」は2回分として数える。
 *
 * 状態は SharedPreferences（iOS の UserDefaults 相当）に保存し、
 * アプリ全体で単一の値を共有する（object として実装）。
 */
object AdCounter {
    private const val STORAGE_KEY = "adActionCount"

    /** 広告を表示する操作回数のしきい値。 */
    const val THRESHOLD = 5

    /** 現在のカウント。 */
    val count: Int
        get() = Prefs.get().getInt(STORAGE_KEY, 0)

    /** 対象操作を加算する。amount で重み（回数分）を指定する（既定1）。 */
    fun increment(amount: Int = 1) {
        Prefs.get().edit().putInt(STORAGE_KEY, count + amount).apply()
    }

    /**
     * しきい値に達していれば true を返し、カウントを0にリセットする。
     * 広告表示の可否判定に使う（呼んだ時点でリセットするので副作用に注意）。
     */
    fun consumeIfReady(): Boolean {
        if (count < THRESHOLD) return false
        Prefs.get().edit().putInt(STORAGE_KEY, 0).apply()
        return true
    }
}
