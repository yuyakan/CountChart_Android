package com.kanbe1365.countergraph.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

/** タップ可能にする薄いラッパ（リップルはデフォルト）。 */
fun Modifier.clickableRow(onClick: () -> Unit): Modifier = this.clickable(onClick = onClick)

/** 画面高さに対する割合で高さを指定する。iOS の height * ratio に相当。 */
@Composable
fun Modifier.fillMaxHeightFraction(fraction: Float): Modifier {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    return this.then(Modifier.height((screenHeight * fraction).dp))
}

/** StateFlow を lifecycle-aware に購読する短縮版。 */
@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycleCompat(): State<T> = collectAsStateWithLifecycle()
