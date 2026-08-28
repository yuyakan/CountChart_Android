package com.kanbe1365.countergraph.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kanbe1365.countergraph.R
import com.kanbe1365.countergraph.ad.AdCounter
import com.kanbe1365.countergraph.ad.InterstitialAdManager
import com.kanbe1365.countergraph.ad.ReviewManager
import com.kanbe1365.countergraph.data.CountFile
import com.kanbe1365.countergraph.ui.theme.LocalBrandColor
import androidx.compose.ui.graphics.PathEffect

/**
 * メニュー画面。ファイルカードの一覧・追加・編集（並べ替え/削除/複製）を行う。
 * iOS の MenuView に相当。
 */
@Composable
fun MenuScreen(
    onOpenFile: (String) -> Unit,
    viewModel: MenuViewModel = viewModel(),
) {
    val files by viewModel.files.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(false) }
    val brand = LocalBrandColor.current
    val activity = LocalActivity.current

    // メニュー画面が前面に来るたび（起動時・詳細画面から戻った時）に、
    // レビュー要求の機会をカウントし、詳細画面遷移時にすぐ表示できるよう広告も先読みする。
    // iOS の MenuView.onAppear に相当。
    LaunchedEffect(Unit) {
        activity?.let { InterstitialAdManager.loadInterstitial(it) }
        delay(500)
        activity?.let { ReviewManager.recordOpportunityAndRequestReviewIfNeeded(it) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp,
            ),
        ) {
            // ヘッダー（編集トグルのみ・右寄せ）
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = stringResource(if (editing) R.string.done else R.string.edit),
                        color = brand,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { editing = !editing }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                }
            }

            itemsIndexed(files, key = { _, f -> f.id }) { index, file ->
                FileCard(
                    file = file,
                    editing = editing,
                    canMoveUp = index > 0,
                    canMoveDown = index < files.size - 1,
                    onClick = { if (!editing) onOpenFile(file.id) },
                    onDuplicate = {
                        viewModel.duplicate(file)
                        AdCounter.increment(amount = 3) // 複製は3回分
                    },
                    onDelete = { viewModel.remove(file) },
                    onMoveUp = { viewModel.move(index, index - 1) },
                    onMoveDown = { viewModel.move(index, index + 1) },
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }

            // 新規追加カード
            item {
                AddCard(
                    onClick = {
                        viewModel.add()
                        AdCounter.increment(amount = 3) // 追加は3回分
                    },
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun FileCard(
    file: CountFile,
    editing: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brand = LocalBrandColor.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = !editing) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brand.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.BarChart, contentDescription = null, tint = brand, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.size(14.dp))
        Text(
            text = file.title,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        if (editing) {
            IconAction(Icons.Filled.ArrowUpward, tint = if (canMoveUp) brand else brand.copy(alpha = 0.25f), enabled = canMoveUp, onClick = onMoveUp)
            IconAction(Icons.Filled.ArrowDownward, tint = if (canMoveDown) brand else brand.copy(alpha = 0.25f), enabled = canMoveDown, onClick = onMoveDown)
            IconAction(Icons.Filled.ContentCopy, tint = brand, contentDescription = stringResource(R.string.duplicate), onClick = onDuplicate)
            IconAction(Icons.Filled.Delete, tint = MaterialTheme.colorScheme.error, contentDescription = stringResource(R.string.delete), onClick = onDelete)
        } else {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
private fun IconAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    enabled: Boolean = true,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun AddCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val brand = LocalBrandColor.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .dashedBorder(brand.copy(alpha = 0.4f), 16.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brand),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.size(14.dp))
            Text(
                text = stringResource(R.string.add),
                color = brand,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** 破線の角丸ボーダーを描く Modifier（iOS の dash: [6,4] に相当）。 */
private fun Modifier.dashedBorder(color: androidx.compose.ui.graphics.Color, cornerRadius: androidx.compose.ui.unit.Dp): Modifier =
    drawBehind {
        val stroke = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f),
        )
        drawRoundRect(
            color = color,
            style = stroke,
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        )
    }
