package com.kanbe1365.countergraph.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kanbe1365.countergraph.R
import kotlinx.coroutines.delay

/**
 * 起動時のアプリ内スプラッシュ。iOS の SplashView に相当。
 * splash 画像を中央にフェードイン表示し、一定時間後に onFinish を呼ぶ。
 * 画像は白背景のロゴのため、ダーク時も白で統一する。
 */
@Composable
fun SplashScreen(onFinish: () -> Unit) {
    var appeared by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "splashAlpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.92f,
        animationSpec = tween(durationMillis = 500),
        label = "splashScale",
    )

    LaunchedEffect(Unit) {
        appeared = true
        delay(1500)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.splash),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(220.dp)
                .alpha(alpha)
                .scale(scale),
        )
    }
}
