package com.kanbe1365.countergraph.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanbe1365.countergraph.R
import com.kanbe1365.countergraph.data.ChartEntry
import com.kanbe1365.countergraph.ui.theme.LocalBrandColor

@Composable
fun GroupTab(
    viewModel: FileViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brand = LocalBrandColor.current
    val revision by viewModel.revision.collectAsStateWithLifecycleCompat()
    val title by viewModel.title.collectAsStateWithLifecycleCompat()
    val groups by viewModel.groups.collectAsStateWithLifecycleCompat()

    var editing by remember { mutableStateOf(false) }
    var showAddGroup by remember { mutableStateOf(false) }

    val bars = remember(revision, groups) { viewModel.groupBars() }
    val items = remember(revision, groups) { viewModel.groupItems() }
    val hasGroups = groups.isNotEmpty()
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(modifier = modifier.fillMaxSize()) {
        TabHeader(brand = brand, onBack = onBack) {
            EditToggle(editing = editing, brand = brand) { editing = !editing }
        }
        TabTitle(title = title, brand = brand, editing = editing, onEditTitle = {})

        if (editing) {
            EditGroupContent(
                viewModel = viewModel,
                groups = groups,
                items = items,
                brand = brand,
                textColor = textColor,
                onAddGroup = { showAddGroup = true },
                modifier = Modifier.weight(1f),
            )
        } else if (hasGroups) {
            // グループ合計をグループ色の棒で表示する（棒グラフと同じ描画を流用）。
            val entries = bars.mapIndexed { index, bar ->
                ChartEntry(id = index, name = bar.name, value = bar.value, color = bar.color)
            }
            BarChart(
                entries = entries,
                horizontal = false,
                valueColor = textColor,
                labelColor = textColor,
                dimmed = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeightFraction(0.5f)
                    .padding(horizontal = 28.dp, vertical = 16.dp),
            )
            Spacer(Modifier.weight(1f))
        } else {
            EmptyGroupPlaceholder(brand = brand, textColor = textColor, modifier = Modifier.weight(1f))
        }
    }

    if (showAddGroup) {
        RenameDialogTitled(
            titleRes = R.string.new_group,
            hint = stringResource(R.string.group_name),
            confirmRes = R.string.add,
            onDismiss = { showAddGroup = false },
            onConfirm = { name -> if (name.isNotEmpty()) viewModel.addGroup(name) },
        )
    }
}

@Composable
private fun EditGroupContent(
    viewModel: FileViewModel,
    groups: List<com.kanbe1365.countergraph.data.CountGroup>,
    items: List<com.kanbe1365.countergraph.data.GroupItem>,
    brand: Color,
    textColor: Color,
    onAddGroup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp),
    ) {
        item {
            SectionHeader(stringResource(R.string.groups), textColor)
        }
        items(groups, key = { it.id }) { group ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(group.color))
                Spacer(Modifier.size(12.dp))
                Text(group.name, color = textColor, modifier = Modifier.weight(1f))
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp).clickableRow { viewModel.removeGroup(group.id) },
                )
            }
            HorizontalDivider(color = textColor.copy(alpha = 0.1f))
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableRow { onAddGroup() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.AddCircle, contentDescription = null, tint = brand, modifier = Modifier.size(22.dp))
                Spacer(Modifier.size(12.dp))
                Text(stringResource(R.string.new_group), color = brand, fontWeight = FontWeight.SemiBold)
            }
        }

        item {
            Spacer(Modifier.size(8.dp))
            SectionHeader(stringResource(R.string.assign_items), textColor)
        }
        items(items, key = { it.id }) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(item.name, color = textColor, maxLines = 1, modifier = Modifier.weight(1f))
                Spacer(Modifier.size(8.dp))
                GroupPickerMenu(
                    current = viewModel.group(item.groupId),
                    groups = groups,
                    brand = brand,
                    textColor = textColor,
                    onSelect = { viewModel.setItemGroup(item.id, it) },
                )
            }
        }
    }
}

@Composable
private fun GroupPickerMenu(
    current: com.kanbe1365.countergraph.data.CountGroup?,
    groups: List<com.kanbe1365.countergraph.data.CountGroup>,
    brand: Color,
    textColor: Color,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier.clickableRow { expanded = true }.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (current != null) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(current.color))
                Text(current.name, color = textColor)
            } else {
                Text(stringResource(R.string.no_group), color = textColor.copy(alpha = 0.5f))
            }
            Icon(Icons.Filled.UnfoldMore, contentDescription = null, tint = textColor.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.no_group)) },
                onClick = { onSelect(null); expanded = false },
            )
            groups.forEach { group ->
                DropdownMenuItem(
                    text = { Text(group.name) },
                    leadingIcon = { Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(group.color)) },
                    onClick = { onSelect(group.id); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, textColor: Color) {
    Text(
        text = text.uppercase(),
        color = textColor.copy(alpha = 0.5f),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 6.dp),
    )
}

@Composable
private fun EmptyGroupPlaceholder(brand: Color, textColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 48.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.GridView, contentDescription = null, tint = brand.copy(alpha = 0.4f), modifier = Modifier.size(44.dp))
        Spacer(Modifier.size(12.dp))
        Text(
            stringResource(R.string.no_groups_yet),
            color = textColor.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/** グループ名入力用のダイアログ（タイトル・ヒント・確定ラベルを差し替え可能な RenameDialog 派生）。 */
@Composable
fun RenameDialogTitled(
    titleRes: Int,
    hint: String,
    confirmRes: Int,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text(hint) },
                singleLine = true,
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = { onConfirm(draft.trim()); onDismiss() }) {
                Text(stringResource(confirmRes))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
