package com.kanbe1365.countergraph.ad

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.kanbe1365.countergraph.BuildConfig

/**
 * インタースティシャル広告の読み込み・表示を管理する。
 * iOS の Interstitial クラスに相当し、以下の挙動を揃えている。
 *
 * - 広告は使い捨て。表示したら破棄して次を先読みする。
 * - 読み込み失敗時は指数バックオフ（2,4,8,…秒, 上限5回）で再試行する。
 * - 一度表示したら cooldown 秒は次の広告を出さない。
 * - presentIfReady はカウントしきい値・クールダウン・ロード状態を見て表示する。
 *   未ロードのときはカウントを消費せず、読み込みだけ促して次の機会に持ち越す。
 *
 * 広告ID（本番/テスト）は build.gradle の buildConfigField で切り替える
 * （iOS の Interstitial.useTestAd に相当）。
 */
object InterstitialAdManager {
    private const val TAG = "InterstitialAd"

    /** 実際に使う広告ユニットID（DEBUG=テスト, RELEASE=本番）。 */
    private val adUnitId: String get() = BuildConfig.ADMOB_INTERSTITIAL_UNIT_ID

    /** 読み込み済みの広告。表示可能なら null 以外。 */
    private var interstitialAd: InterstitialAd? = null

    /** 二重読み込みを防ぐフラグ。 */
    private var isLoading = false

    /** 読み込み失敗時のリトライ回数（成功で0に戻す）。指数バックオフの算出に使う。 */
    private var retryCount = 0

    /** リトライ上限。これを超えたら次の loadInterstitial() 呼び出しまで再試行しない。 */
    private const val MAX_RETRY_COUNT = 5

    /** 前回広告を表示した時刻（elapsedRealtime, ミリ秒）。null は未表示。 */
    private var lastPresentedAt: Long? = null

    /** 広告表示のクールダウン（ミリ秒）。一度表示したらこの時間は次の広告を出さない。 */
    private const val COOLDOWN_MS = 90_000L

    private val mainHandler = Handler(Looper.getMainLooper())

    /** 広告が表示可能な状態か。 */
    private val isReady: Boolean get() = interstitialAd != null

    /** クールダウン中か（前回表示から cooldown 未満）。 */
    private val isInCooldown: Boolean
        get() {
            val last = lastPresentedAt ?: return false
            return SystemClock.elapsedRealtime() - last < COOLDOWN_MS
        }

    /** 広告を先読みする。既にロード済み／読み込み中なら何もしない。 */
    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isLoading) return
        isLoading = true

        val appContext = context.applicationContext
        InterstitialAd.load(
            appContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    retryCount = 0
                    ad.fullScreenContentCallback = fullScreenCallback(appContext)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.d(TAG, "読み込み失敗: ${error.message}")
                    interstitialAd = null
                    isLoading = false
                    // 読み込み失敗時は指数バックオフでリトライする。
                    scheduleRetry(appContext)
                }
            },
        )
    }

    /** 指数バックオフ（2,4,8,…秒, 上限あり）で読み込みを再試行する。 */
    private fun scheduleRetry(context: Context) {
        if (retryCount >= MAX_RETRY_COUNT) return
        retryCount += 1
        val delayMs = (Math.pow(2.0, retryCount.toDouble()) * 1000).toLong() // 2,4,8,16,32 秒
        mainHandler.postDelayed({ loadInterstitial(context) }, delayMs)
    }

    /**
     * カウンターがしきい値に達していて、かつ広告が準備できていれば表示する。
     * 広告が未ロードのときはカウントを消費せず、次の機会に持ち越して読み込みを仕込む。
     */
    fun presentIfReady(activity: Activity) {
        // 前回表示から一定時間（cooldown）は次を出さない。
        // カウントは消費せず持ち越し、クールダウン明けの機会に表示できるようにする。
        if (isInCooldown) return

        // 表示条件（カウントしきい値）を満たすか確認。満たさなければ何もしない。
        if (AdCounter.count < AdCounter.THRESHOLD) return

        // 通常の広告表示。未ロードならカウントは消費せず読み込みだけ促す。
        val ad = interstitialAd
        if (ad == null) {
            loadInterstitial(activity)
            return
        }
        AdCounter.consumeIfReady()
        presentInterstitial(activity, ad)
    }

    /** インタースティシャル広告の表示。 */
    private fun presentInterstitial(activity: Activity, ad: InterstitialAd) {
        ad.show(activity)
        // 表示時刻を記録し、以後 cooldown は次の広告を出さない。
        lastPresentedAt = SystemClock.elapsedRealtime()
        // InterstitialAd は使い捨て。表示したら参照を破棄し、閉じたあと次を読み込む。
        interstitialAd = null
    }

    /** 表示・クローズ・失敗の各コールバック。閉じたら次を先読みする。 */
    private fun fullScreenCallback(context: Context) = object : FullScreenContentCallback() {
        override fun onAdShowedFullScreenContent() {
            Log.d(TAG, "インタースティシャル広告を表示しました")
        }

        override fun onAdDismissedFullScreenContent() {
            Log.d(TAG, "インタースティシャル広告を閉じました")
            interstitialAd = null
            // 次の広告を先読みする。
            loadInterstitial(context)
        }

        override fun onAdFailedToShowFullScreenContent(error: AdError) {
            Log.d(TAG, "インタースティシャル広告の表示に失敗しました: ${error.message}")
            interstitialAd = null
            loadInterstitial(context)
        }
    }
}
