package com.kanbe1365.countergraph.ad

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.kanbe1365.countergraph.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 広告の同意フローを管理する。iOS の ConsentManager に相当する。
 *
 * 1) UMP（User Messaging Platform）で同意情報を更新し、必要なら同意フォームを表示する。
 * 2) 同意状態が整い次第、Google Mobile Ads SDK を初期化する。
 *
 * UMP の同意結果に基づき、SDK が自動でパーソナライズ/非パーソナライズ広告を切り替える。
 * そのため npa などのパラメータをアプリ側で明示設定する必要はない。
 *
 * ※ iOS の ATT に相当する仕組みは Android にはないため、
 *   UMP → SDK 初期化 の順で進む（ATT ステップは省く）。
 */
object ConsentManager {
    private const val TAG = "ConsentManager"

    /** SDK の二重初期化を防ぐフラグ。 */
    private val didStartAds = AtomicBoolean(false)

    /**
     * 同意フローを起動する。アプリ起動時に一度だけ呼ぶ想定。
     * UMP の同意情報更新 →（必要なら）同意フォーム表示 → AdMob 初期化 の順で進む。
     */
    fun gatherConsent(activity: Activity) {
        val params = ConsentRequestParameters.Builder()
            // デバッグ時に地域や同意状態を強制したい場合は ConsentDebugSettings をここで設定する。
            .apply {
                if (BuildConfig.DEBUG) {
                    // 開発中は毎回同意フォームの検証ができるよう、必要に応じてここで
                    // ConsentDebugSettings（EEA 相当の geography 指定など）を設定する。
                }
            }
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // 必要な場合のみ同意フォームを読み込んで表示する。不要な地域では即完了する。
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.d(TAG, "UMP: 同意フォームの表示に失敗しました: ${formError.message}")
                    }
                    continueAfterConsent(activity, consentInformation)
                }
            },
            { error ->
                // 同意情報の取得に失敗しても、前回までに canRequestAds が立っていれば広告を出せる。
                Log.d(TAG, "UMP: 同意情報の更新に失敗しました: ${error.message}")
                continueAfterConsent(activity, consentInformation)
            },
        )
    }

    /** UMP のフローが終わったあとの共通処理。広告が要求できる状態なら SDK を初期化する。 */
    private fun continueAfterConsent(activity: Activity, consentInformation: ConsentInformation) {
        if (!consentInformation.canRequestAds()) {
            Log.d(TAG, "UMP: 広告のリクエストが許可されていません。")
            return
        }
        startAdsIfNeeded(activity)
    }

    /** Google Mobile Ads SDK を初期化する（未初期化のときのみ）。初期化後に広告を先読みする。 */
    private fun startAdsIfNeeded(activity: Activity) {
        if (!didStartAds.compareAndSet(false, true)) return
        MobileAds.initialize(activity.applicationContext) {
            // SDK 初期化完了後に最初の広告を先読みしておく。
            InterstitialAdManager.loadInterstitial(activity.applicationContext)
        }
    }
}
