package com.kanbe1365.countergraph.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.LocalActivity
import com.kanbe1365.countergraph.R
import com.kanbe1365.countergraph.ad.AdCounter
import com.kanbe1365.countergraph.ad.InterstitialAdManager
import com.kanbe1365.countergraph.data.BarOrientation
import com.kanbe1365.countergraph.data.ChartEntry
import com.kanbe1365.countergraph.data.ChartSortOrder
import com.kanbe1365.countergraph.ui.theme.LocalBrandColor

@Composable
fun BarTab(
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
    var orientation by remember { mutableStateOf(BarOrientation.VERTICAL) }
    var sortOrder by remember { mutableStateOf(ChartSortOrder.ENTRY) }
    var showAdd by remember { mutableStateOf(false) }
    var showMaxAlert by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<Int?>(null) }
    var showRename by remember { mutableStateOf(false) }

    // revision を参照して再計算する。
    val entries = remember(revision, sortOrder) { viewModel.barEntries(sortOrder) }
    val isEmpty = entries.isEmpty()
    val displayed = if (isEmpty) sampleBarEntries() else entries
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(modifier = modifier.fillMaxSize()) {
        TabHeader(brand = brand, onBack = onBack) {
            HeaderIconButton(
                icon = if (orientation == BarOrientation.VERTICAL) Icons.AutoMirrored.Filled.TextSnippet else Icons.Filled.BarChart,
                tint = brand,
            ) {
                orientation = if (orientation == BarOrientation.VERTICAL) BarOrientation.HORIZONTAL else BarOrientation.VERTICAL
            }
            SortMenuButton(current = sortOrder, onSelect = { sortOrder = it }, tint = brand)
            EditToggle(editing = editing, brand = brand) {
                // 編集モードから「完了」を押したときにカウントし、条件を満たせば広告を表示する。
                // iOS の BarChartView の「完了」ボタンに相当。
                val wasEditing = editing
                editing = !editing
                if (wasEditing) {
                    AdCounter.increment(amount = 2) // 編集完了は2回分
                    activity?.let { InterstitialAdManager.presentIfReady(it) }
                }
            }
        }

        TabTitle(title = title, brand = brand, editing = editing, onEditTitle = { showRename = true })

        val chartHeightFraction = when {
            editing -> 0.26f
            orientation == BarOrientation.HORIZONTAL -> 0.5f
            else -> 0.44f
        }

        // 表示モードはグラフの上にも余白を入れ、上下の Spacer で挟んで縦方向の中央に寄せる。
        if (!editing) Spacer(Modifier.weight(1f))

        BarChart(
            entries = displayed,
            horizontal = orientation == BarOrientation.HORIZONTAL,
            valueColor = if (isEmpty) textColor.copy(alpha = 0.4f) else textColor,
            labelColor = textColor,
            dimmed = isEmpty,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeightFraction(chartHeightFraction)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        )

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
            Spacer(Modifier.weight(1f))
        }
    }

    if (showAdd) {
        AddItemDialog(
            tint = brand,
            onDismiss = { showAdd = false },
            onAdd = { name, value ->
                if (!viewModel.addData(name, value)) showMaxAlert = true
            },
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
        RenameDialog(
            initial = title,
            onDismiss = { showRename = false },
            onConfirm = { viewModel.setTitle(it) },
        )
    }
}

/** 編集モードの項目リスト（名前・値タップで編集、±、スワイプ相当は削除ボタン）＋最下部に追加行。 */
@Composable
fun EditableItemList(
    entries: List<ChartEntry>,
    textColor: Color,
    brand: Color,
    onEdit: (Int) -> Unit,
    onMinus: (Int) -> Unit,
    onPlus: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
    ) {
        items(entries, key = { it.id }) { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ColorSwatch(entry.color)
                Spacer(Modifier.size(12.dp))
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickableRow { onEdit(entry.id) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(entry.name, color = textColor, modifier = Modifier.weight(1f), maxLines = 2)
                    Spacer(Modifier.size(8.dp))
                    Text(entry.value.toString(), color = textColor, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(6.dp))
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = brand.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.size(8.dp))
                Icon(
                    Icons.Filled.RemoveCircle, contentDescription = stringResource(R.string.delete), tint = brand,
                    modifier = Modifier.size(28.dp).clickableRow { onMinus(entry.id) },
                )
                Spacer(Modifier.size(8.dp))
                Icon(
                    Icons.Filled.AddCircle, contentDescription = stringResource(R.string.add), tint = brand,
                    modifier = Modifier.size(28.dp).clickableRow { onPlus(entry.id) },
                )
                Spacer(Modifier.size(8.dp))
                Icon(
                    Icons.Filled.Delete, contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp).clickableRow { onDelete(entry.id) },
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableRow { onAdd() }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.AddCircle, contentDescription = null, tint = brand, modifier = Modifier.size(22.dp))
                Spacer(Modifier.size(12.dp))
                Text(stringResource(R.string.add), color = brand, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun MaxDataAlert(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.max_alert_text)) },
        text = { Text(stringResource(R.string.max_alert_message)) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) } },
    )
}

@Composable
fun RenameDialog(initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var draft by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title)) },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft.trim()); onDismiss() }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

/** データ未登録時のサンプル（淡色プレースホルダ）。 */
@Composable
fun sampleBarEntries(): List<ChartEntry> {
    val gray = Color.Gray.copy(alpha = 0.3f)
    return listOf(
        ChartEntry(0, stringResource(R.string.name_ann), 80, gray),
        ChartEntry(1, stringResource(R.string.name_tom), 230, gray),
        ChartEntry(2, stringResource(R.string.name_bob), 500, gray),
        ChartEntry(3, stringResource(R.string.name_casey), 320, gray),
        ChartEntry(4, stringResource(R.string.name_brian), 120, gray),
    )
}
