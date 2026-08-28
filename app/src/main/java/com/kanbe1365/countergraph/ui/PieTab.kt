package com.kanbe1365.countergraph.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.LocalActivity
import com.kanbe1365.countergraph.R
import com.kanbe1365.countergraph.ad.AdCounter
import com.kanbe1365.countergraph.ad.InterstitialAdManager
import com.kanbe1365.countergraph.data.ChartEntry
import com.kanbe1365.countergraph.data.ChartSortOrder
import com.kanbe1365.countergraph.ui.theme.LocalBrandColor

@Composable
fun PieTab(
    viewModel: FileViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brand = LocalBrandColor.current
    val activity = LocalActivity.current
    val revision by viewModel.revision.collectAsStateWithLifecycleCompat()
    val title by viewModel.title.collectAsStateWithLifecycleCompat()
    val countUnit by viewModel.countUnit.collectAsStateWithLifecycleCompat()

    var editing by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf(ChartSortOrder.ENTRY) }
    var showAdd by remember { mutableStateOf(false) }
    var showMaxAlert by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<Int?>(null) }
    var showRename by remember { mutableStateOf(false) }

    val entries = remember(revision, sortOrder) { viewModel.pieEntries(sortOrder) }
    val isEmpty = entries.isEmpty()
    val displayed = if (isEmpty) samplePieEntries() else entries
    val total = displayed.sumOf { it.value }
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(modifier = modifier.fillMaxSize()) {
        TabHeader(brand = brand, onBack = onBack) {
            SortMenuButton(current = sortOrder, onSelect = { sortOrder = it }, tint = brand)
            EditToggle(editing = editing, brand = brand) {
                // 編集モードから「完了」を押したときにカウントし、条件を満たせば広告を表示する。
                // iOS の PieChartView の「完了」ボタンに相当。
                val wasEditing = editing
                editing = !editing
                if (wasEditing) {
                    AdCounter.increment(amount = 2) // 編集完了は2回分
                    activity?.let { InterstitialAdManager.presentIfReady(it) }
                }
            }
        }

        TabTitle(title = title, brand = brand, editing = editing, onEditTitle = { showRename = true })

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (editing) 8.dp else 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            PieChart(
                entries = displayed,
                dimmed = isEmpty,
                total = total,
                totalColor = textColor,
                totalLabelColor = textColor,
                editing = editing,
                modifier = Modifier
                    .fillMaxWidth(if (editing) 0.44f else 0.72f)
                    .aspectRatio(1f),
            )
        }

        if (editing) {
            EditableItemList(
                entries = entries,
                textColor = textColor,
                brand = brand,
                onEdit = { editingId = it },
                onMinus = { viewModel.minus(it, countUnit) },
                onPlus = { viewModel.plus(it, countUnit) },
                onDelete = { viewModel.removeData(it) },
                onAdd = { showAdd = true },
                modifier = Modifier.weight(1f),
            )
            CountUnitPicker(
                unit = countUnit,
                onChange = { viewModel.setCountUnit(it) },
                tint = brand,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
        } else {
            LegendList(
                entries = displayed,
                dimmed = isEmpty,
                textColor = textColor,
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (showAdd) {
        AddItemDialog(
            tint = brand,
            onDismiss = { showAdd = false },
            onAdd = { name, value -> if (!viewModel.addData(name, value)) showMaxAlert = true },
        )
    }
    editingId?.let { id ->
        ItemEditDialog(
            initialName = viewModel.name(id),
            initialValue = viewModel.value(id),
            tint = brand,
            onDismiss = { editingId = null },
            onSave = { name, value ->
                viewModel.updateName(id, name)
                viewModel.updateValue(id, value)
            },
        )
    }
    if (showMaxAlert) MaxDataAlert { showMaxAlert = false }
    if (showRename) {
        RenameDialog(initial = title, onDismiss = { showRename = false }, onConfirm = { viewModel.setTitle(it) })
    }
}

/** 表示モードの凡例（色チップ＋名前＋値＋パーセント）。 */
@Composable
private fun LegendList(
    entries: List<ChartEntry>,
    dimmed: Boolean,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 28.dp, vertical = 8.dp),
    ) {
        items(entries, key = { it.id }) { entry ->
            val alpha = if (dimmed) 0.4f else 1f
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ColorSwatch(entry.color)
                Spacer(Modifier.size(12.dp))
                Text(entry.name, color = textColor.copy(alpha = alpha), maxLines = 1, modifier = Modifier.weight(1f))
                Text(entry.value.toString(), color = textColor.copy(alpha = alpha), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(8.dp))
                Text(
                    entry.percent,
                    color = textColor.copy(alpha = 0.6f * alpha),
                    fontSize = 13.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(56.dp),
                )
            }
            HorizontalDivider(color = textColor.copy(alpha = 0.12f))
        }
    }
}

@Composable
fun samplePieEntries(): List<ChartEntry> {
    val g2 = Color.Gray.copy(alpha = 0.2f)
    val g3 = Color.Gray.copy(alpha = 0.3f)
    return listOf(
        ChartEntry(0, stringResource(R.string.name_ann), 80, g2, "6.4%"),
        ChartEntry(1, stringResource(R.string.name_tom), 230, g3, "18.4%"),
        ChartEntry(2, stringResource(R.string.name_bob), 500, g2, "40.0%"),
        ChartEntry(3, stringResource(R.string.name_casey), 320, g3, "25.6%"),
        ChartEntry(4, stringResource(R.string.name_brian), 120, g2, "9.6%"),
    )
}
