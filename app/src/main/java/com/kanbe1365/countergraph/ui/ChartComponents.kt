package com.kanbe1365.countergraph.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanbe1365.countergraph.R
import com.kanbe1365.countergraph.data.ChartSortOrder
import com.kanbe1365.countergraph.data.CountUnitStore

/** ソート順のラベル。 */
@Composable
fun ChartSortOrder.label(): String = stringResource(
    when (this) {
        ChartSortOrder.ENTRY -> R.string.sort_entry
        ChartSortOrder.DESCENDING -> R.string.sort_descending
        ChartSortOrder.ASCENDING -> R.string.sort_ascending
    }
)

/** ソート切替のドロップダウンメニュー付きアイコンボタン。 */
@Composable
fun SortMenuButton(
    current: ChartSortOrder,
    onSelect: (ChartSortOrder) -> Unit,
    tint: Color,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        HeaderIconButton(icon = Icons.Filled.SwapVert, tint = tint) { expanded = true }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ChartSortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = { Text(order.label()) },
                    leadingIcon = {
                        Icon(
                            when (order) {
                                ChartSortOrder.ENTRY -> Icons.Filled.FormatListNumbered
                                ChartSortOrder.DESCENDING -> Icons.Filled.ArrowDownward
                                ChartSortOrder.ASCENDING -> Icons.Filled.ArrowUpward
                            },
                            contentDescription = null,
                        )
                    },
                    onClick = { onSelect(order); expanded = false },
                )
            }
        }
    }
}

/** ヘッダー用の 44dp 正方形アイコンボタン。 */
@Composable
fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(24.dp))
    }
}

/**
 * 1カウント単位を選択するチップ列。プリセット＋カスタム入力（✎）。
 * iOS の CountUnitPicker に相当。
 */
@Composable
fun CountUnitPicker(
    unit: Int,
    onChange: (Int) -> Unit,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    var showCustom by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.count_width),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
        )
        Spacer(Modifier.size(10.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CountUnitStore.presets.forEach { preset ->
                Chip(
                    label = "$preset",
                    selected = unit == preset,
                    tint = tint,
                ) { onChange(preset) }
            }
            val custom = CountUnitStore.isCustom(unit)
            Chip(
                label = if (custom) "$unit" else null,
                icon = Icons.Filled.Edit,
                selected = custom,
                tint = tint,
            ) {
                draft = unit.toString()
                showCustom = true
            }
        }
    }

    if (showCustom) {
        AlertDialog(
            onDismissRequest = { showCustom = false },
            title = { Text(stringResource(R.string.count_width)) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it.filter(Char::isDigit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    draft.toIntOrNull()?.let { if (it > 0) onChange(it) }
                    showCustom = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showCustom = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun Chip(
    label: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    selected: Boolean,
    tint: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) tint else tint.copy(alpha = 0.12f))
            .clickable { onClick() }
            .defaultMinSize(minWidth = 34.dp)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
    ) {
        val content = if (selected) Color.White else tint
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
        }
        if (label != null) {
            Text(label, color = content, fontWeight = FontWeight.SemiBold)
        }
    }
}
