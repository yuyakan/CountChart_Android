package com.kanbe1365.countergraph.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.window.DialogProperties
import com.kanbe1365.countergraph.R
import com.kanbe1365.countergraph.data.NameTemplateStore

/**
 * 既存項目の名前・値を編集するダイアログ。iOS の ItemEditSheet に相当。
 */
@Composable
fun ItemEditDialog(
    initialName: String,
    initialValue: Int,
    tint: Color,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var value by remember { mutableStateOf(initialValue.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.item_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.size(12.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text(stringResource(R.string.value)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.trim().isNotEmpty(),
                onClick = {
                    onSave(name, value.toIntOrNull() ?: initialValue)
                    onDismiss()
                },
            ) { Text(stringResource(R.string.done)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

/**
 * 新規項目を追加するダイアログ。よく使う名前（テンプレート）をチップで呼び出せる。
 * iOS の AddItemSheet に相当。
 */
@Composable
fun AddItemDialog(
    tint: Color,
    onDismiss: () -> Unit,
    onAdd: (name: String, value: Int) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("100") }
    var saveAsTemplate by remember { mutableStateOf(true) }
    var templates by remember { mutableStateOf(NameTemplateStore.names()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true),
        title = { Text(stringResource(R.string.add)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.item_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.size(12.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text(stringResource(R.string.value)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.size(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.save_as_template),
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Switch(
                        checked = saveAsTemplate,
                        onCheckedChange = { saveAsTemplate = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = tint),
                    )
                }

                if (templates.isNotEmpty()) {
                    Spacer(Modifier.size(8.dp))
                    Text(
                        stringResource(R.string.templates),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.size(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        templates.forEach { templateName ->
                            Text(
                                text = templateName,
                                color = tint,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tint.copy(alpha = 0.12f))
                                    .clickable { name = templateName }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.trim().isNotEmpty(),
                onClick = {
                    if (saveAsTemplate) NameTemplateStore.add(name)
                    onAdd(name.trim(), value.toIntOrNull() ?: 0)
                    onDismiss()
                },
            ) { Text(stringResource(R.string.add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
