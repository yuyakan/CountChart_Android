package com.kanbe1365.countergraph.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanbe1365.countergraph.data.ChartEntry
import com.kanbe1365.countergraph.data.ChartSortOrder
import com.kanbe1365.countergraph.ui.theme.LocalBrandColor

@Composable
fun RankingTab(
    viewModel: FileViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brand = LocalBrandColor.current
    val revision by viewModel.revision.collectAsStateWithLifecycleCompat()
    val title by viewModel.title.collectAsStateWithLifecycleCompat()

    val entries = androidx.compose.runtime.remember(revision) { viewModel.barEntries(ChartSortOrder.DESCENDING) }
    val isEmpty = entries.isEmpty()
    val displayed = if (isEmpty) sampleRankingEntries() else entries
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(modifier = modifier.fillMaxSize()) {
        TabHeader(brand = brand, onBack = onBack)
        TabTitle(title = title, brand = brand, editing = false, onEditTitle = {})
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 28.dp, vertical = 8.dp),
        ) {
            itemsIndexed(displayed, key = { _, e -> e.id }) { index, entry ->
                RankRow(rank = index + 1, entry = entry, isFirst = index == 0, brand = brand, textColor = textColor, dimmed = isEmpty)
                if (index < displayed.size - 1) {
                    HorizontalDivider(
                        color = textColor.copy(alpha = 0.12f),
                        modifier = Modifier.padding(start = 44.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RankRow(
    rank: Int,
    entry: ChartEntry,
    isFirst: Boolean,
    brand: Color,
    textColor: Color,
    dimmed: Boolean,
) {
    val alpha = if (dimmed) 0.4f else 1f
    Box {
        if (isFirst) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 0.dp)
                    .size(width = 3.dp, height = 22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(brand.copy(alpha = alpha)),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "%02d".format(rank),
                fontSize = 20.sp,
                fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Normal,
                color = if (isFirst) brand.copy(alpha = alpha) else textColor.copy(alpha = 0.45f * alpha),
                modifier = Modifier.width(40.dp),
            )
            Spacer(Modifier.size(14.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(entry.color.copy(alpha = alpha)),
            )
            Spacer(Modifier.size(14.dp))
            Text(
                entry.name,
                color = textColor.copy(alpha = alpha),
                fontWeight = if (isFirst) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.size(8.dp))
            Text(
                entry.value.toString(),
                color = textColor.copy(alpha = alpha),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun sampleRankingEntries(): List<ChartEntry> {
    val gray = Color.Gray.copy(alpha = 0.3f)
    return listOf(
        ChartEntry(2, androidx.compose.ui.res.stringResource(com.kanbe1365.countergraph.R.string.name_bob), 500, gray),
        ChartEntry(3, androidx.compose.ui.res.stringResource(com.kanbe1365.countergraph.R.string.name_casey), 320, gray),
        ChartEntry(1, androidx.compose.ui.res.stringResource(com.kanbe1365.countergraph.R.string.name_tom), 230, gray),
        ChartEntry(4, androidx.compose.ui.res.stringResource(com.kanbe1365.countergraph.R.string.name_brian), 120, gray),
        ChartEntry(0, androidx.compose.ui.res.stringResource(com.kanbe1365.countergraph.R.string.name_ann), 80, gray),
    )
}
