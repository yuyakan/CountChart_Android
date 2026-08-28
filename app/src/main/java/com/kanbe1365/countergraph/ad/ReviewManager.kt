package com.kanbe1365.countergraph.ad

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory
import com.kanbe1365.countergraph.data.Prefs

/**
 * 標準のレビュー要求ダイアログ（Play In-App Review）の表示制御。
 * iOS の ReviewManager に相当する。
 *
 * 広告表示とは独立したタイミングで、機会カウントのみで判定してレビュー要求を送る。
 * しきい値（機会カウント）は iOS と同じ: 3回目、または10回目以降の10回ごと。
 *
 * ※ In-App Review は Google 側の頻度制限があり、必ずダイアログが出るとは限らない
 *   （iOS の SKStoreReviewController と同様の挙動）。
 */
object ReviewManager {
    private const val TAG = "ReviewManager"
    private const val OPPORTUNITY_COUNT_KEY = "reviewOpportunityCount"

    /**
     * 通常タイミングを記録し、必要なら標準レビュー要求を送信する。
     * メニュー画面が前面に来るたびに呼ぶ想定。
     */
    fun recordOpportunityAndRequestReviewIfNeeded(activity: Activity) {
        val opportunityCount = Prefs.get().getInt(OPPORTUNITY_COUNT_KEY, 0) + 1
        Log.d(
            TAG,
            "opportunityCount=$opportunityCount shouldRequest=${shouldRequestReview(opportunityCount)}",
        )

        // カウントはしきい値に関わらず常に更新する（iOS 版と同じ）。
        Prefs.get().edit().putInt(OPPORTUNITY_COUNT_KEY, opportunityCount).apply()

        if (!shouldRequestReview(opportunityCount)) return
        requestSystemReview(activity)
    }

    private fun shouldRequestReview(opportunityCount: Int): Boolean =
        opportunityCount == 3 || (opportunityCount >= 10 && opportunityCount % 10 == 0)

    private fun requestSystemReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity.applicationContext)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (!request.isSuccessful) {
                Log.d(TAG, "requestReviewFlow に失敗しました: ${request.exception?.message}")
                return@addOnCompleteListener
            }
            manager.launchReviewFlow(activity, request.result).addOnCompleteListener {
                Log.d(TAG, "launchReviewFlow 完了")
            }
        }
    }
}
