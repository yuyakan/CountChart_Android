package com.kanbe1365.countergraph.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.kanbe1365.countergraph.R
import com.kanbe1365.countergraph.data.ChartEntry
import kotlin.math.PI
import kotlin.math.max

/**
 * 縦棒 / 横棒グラフ。iOS の BarChartView の Chart 部分に相当。
 * 角丸・グラデーションの棒、棒の上（横棒は右）に値、縦棒は棒の直下に斜め45度で名前を表示する。
 */
@Composable
fun BarChart(
    entries: List<ChartEntry>,
    horizontal: Boolean,
    valueColor: Color,
    labelColor: Color,
    dimmed: Boolean,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val maxValue = max(entries.maxOfOrNull { it.value } ?: 0, 1)
    val labelSize = labelFontSizeFor(entries.size).sp
    val valueSize = 12.sp

    Canvas(modifier = modifier) {
        if (entries.isEmpty()) return@Canvas
        val alpha = if (dimmed) 0.5f else 1f
        if (horizontal) {
            drawHorizontalBars(entries, maxValue, alpha, textMeasurer, valueColor, labelColor, valueSize, labelSize)
        } else {
            drawVerticalBars(entries, maxValue, alpha, textMeasurer, valueColor, labelColor, valueSize, labelSize)
        }
    }
}

private fun DrawScope.drawVerticalBars(
    entries: List<ChartEntry>,
    maxValue: Int,
    alpha: Float,
    textMeasurer: TextMeasurer,
    valueColor: Color,
    labelColor: Color,
    valueSize: TextUnit,
    labelSize: TextUnit,
) {
    val count = entries.size
    val slot = size.width / count
    val barWidth = slot * 0.6f
    val topInset = 22.dp.toPx()               // 値ラベルぶんの余白
    // 斜めラベルぶんの余白は実測に基づいて確保する。固定割合だとラベルが短いとき
    // 余白が過剰になり、棒群が枠内で上に偏って見えるため。
    val labelMaxWidth = size.width * 0.28f
    val bottomInset = estimatedDiagonalLabelHeight(
        names = entries.map { it.name },
        textMeasurer = textMeasurer,
        labelSize = labelSize,
        maxWidth = labelMaxWidth,
    ).coerceIn(24.dp.toPx(), size.height * 0.32f)
    val plotHeight = size.height - topInset - bottomInset

    entries.forEachIndexed { index, entry ->
        val centerX = slot * (index + 0.5f)
        val h = plotHeight * (max(entry.value, 0).toFloat() / maxValue)
        val left = centerX - barWidth / 2f
        val top = topInset + (plotHeight - h)

        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(entry.color.copy(alpha = alpha), entry.color.copy(alpha = 0.55f * alpha)),
                startY = top, endY = top + h,
            ),
            topLeft = Offset(left, top),
            size = Size(barWidth, h),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        )

        // 値（棒の上）
        val valueLayout = textMeasurer.measure(
            entry.value.toString(),
            TextStyle(color = valueColor, fontSize = valueSize, fontWeight = FontWeight.SemiBold),
        )
        drawText(
            valueLayout,
            topLeft = Offset(centerX - valueLayout.size.width / 2f, top - valueLayout.size.height - 2.dp.toPx()),
        )

        // 名前（棒の直下に斜め45度）
        val nameLayout = textMeasurer.measure(
            entry.name,
            TextStyle(color = labelColor, fontSize = labelSize),
            constraints = Constraints(maxWidth = (size.width * 0.28f).toInt().coerceAtLeast(1)),
            maxLines = 2,
        )
        val anchorX = centerX
        val anchorY = topInset + plotHeight + 4.dp.toPx()
        rotate(degrees = 45f, pivot = Offset(anchorX, anchorY)) {
            drawText(nameLayout, topLeft = Offset(anchorX, anchorY))
        }
    }
}

private fun DrawScope.drawHorizontalBars(
    entries: List<ChartEntry>,
    maxValue: Int,
    alpha: Float,
    textMeasurer: TextMeasurer,
    valueColor: Color,
    labelColor: Color,
    valueSize: TextUnit,
    labelSize: TextUnit,
) {
    val count = entries.size
    val slot = size.height / count
    val barHeight = slot * 0.55f
    val labelArea = size.width * 0.24f
    val valueArea = 44.dp.toPx()
    val plotWidth = size.width - labelArea - valueArea

    entries.forEachIndexed { index, entry ->
        val centerY = slot * (index + 0.5f)
        val w = plotWidth * (max(entry.value, 0).toFloat() / maxValue)
        val top = centerY - barHeight / 2f
        val left = labelArea

        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(entry.color.copy(alpha = alpha), entry.color.copy(alpha = 0.55f * alpha)),
                startX = left, endX = left + w,
            ),
            topLeft = Offset(left, top),
            size = Size(w, barHeight),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        )

        // 名前（左・右寄せ）
        val nameLayout = textMeasurer.measure(
            entry.name,
            TextStyle(color = labelColor, fontSize = labelSize, textAlign = TextAlign.End),
            constraints = Constraints(maxWidth = (labelArea - 8.dp.toPx()).toInt().coerceAtLeast(1)),
            maxLines = 1,
        )
        drawText(
            nameLayout,
            topLeft = Offset(labelArea - 6.dp.toPx() - nameLayout.size.width, centerY - nameLayout.size.height / 2f),
        )

        // 値（右）
        val valueLayout = textMeasurer.measure(
            entry.value.toString(),
            TextStyle(color = valueColor, fontSize = valueSize, fontWeight = FontWeight.SemiBold),
        )
        drawText(
            valueLayout,
            topLeft = Offset(left + w + 6.dp.toPx(), centerY - valueLayout.size.height / 2f),
        )
    }
}

/**
 * ドーナツチャート＋中央に合計値。iOS の PieChartView の Chart 部分に相当。
 */
@Composable
fun PieChart(
    entries: List<ChartEntry>,
    dimmed: Boolean,
    total: Int,
    totalColor: Color,
    totalLabelColor: Color,
    modifier: Modifier = Modifier,
    editing: Boolean = false,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sum = entries.sumOf { max(it.value, 0) }.toFloat()
            if (sum <= 0f) return@Canvas
            val alpha = if (dimmed) 0.5f else 1f
            val diameter = minOf(size.width, size.height)
            val outer = diameter / 2f
            val inner = outer * 0.62f
            val ringWidth = outer - inner
            val center = Offset(size.width / 2f, size.height / 2f)
            val midRadius = inner + ringWidth / 2f
            val insetPx = 1.5.dp.toPx()
            // 隙間ぶんの角度（中間半径の弧長 insetPx を角度換算）。
            val gapDeg = (insetPx / midRadius) * (180f / PI.toFloat())

            var startAngle = -90f
            entries.forEach { entry ->
                val sweep = 360f * (max(entry.value, 0).toFloat() / sum)
                if (sweep > 0f) {
                    drawArc(
                        color = entry.color.copy(alpha = alpha),
                        startAngle = startAngle + gapDeg / 2f,
                        sweepAngle = (sweep - gapDeg).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = Offset(center.x - midRadius, center.y - midRadius),
                        size = Size(midRadius * 2f, midRadius * 2f),
                        style = Stroke(width = ringWidth),
                    )
                }
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.total),
                color = totalLabelColor.copy(alpha = 0.6f),
                fontSize = 13.sp,
            )
            Text(
                text = total.toString(),
                color = totalColor,
                fontSize = if (editing) 28.sp else 40.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * 斜め45度ラベルに必要な縦方向の高さを見積もる。iOS の estimatedDiagonalLabelHeight に相当。
 * 最長テキストを実測し、maxWidth で頭打ち（＝2行折り返し）した幅・高さから求める。
 */
private fun DrawScope.estimatedDiagonalLabelHeight(
    names: List<String>,
    textMeasurer: TextMeasurer,
    labelSize: TextUnit,
    maxWidth: Float,
): Float {
    if (names.isEmpty()) return 0f
    val style = TextStyle(fontSize = labelSize)
    val rawMaxWidth = names.maxOf { textMeasurer.measure(it, style).size.width.toFloat() }
    val lineHeight = textMeasurer.measure("あ", style).size.height.toFloat()
    val effectiveWidth = minOf(rawMaxWidth, maxWidth)
    val lines = if (rawMaxWidth > maxWidth) 2f else 1f
    val textHeight = lineHeight * lines
    // 45度回転後の外接矩形の高さ = (幅 + 高さ) / √2 ＋ 余白
    return (effectiveWidth + textHeight) / 1.41421356f + 8.dp.toPx()
}

/** 名前ラベルのフォントサイズを項目数でスケールする（少ないほど大きく）。iOS の labelFontSize に相当。 */
private fun labelFontSizeFor(count: Int): Float {
    val maxSize = 15f
    val minSize = 10f
    val few = 3
    val many = 10
    return when {
        count <= few -> maxSize
        count >= many -> minSize
        else -> {
            val t = (count - few).toFloat() / (many - few)
            maxSize - (maxSize - minSize) * t
        }
    }
}
