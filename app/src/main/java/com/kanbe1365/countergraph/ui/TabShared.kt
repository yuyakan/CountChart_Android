package com.kanbe1365.countergraph.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanbe1365.countergraph.R

/**
 * 各タブ共通のヘッダー。左に「メニューへ戻る」、右に任意のアクション。
 */
@Composable
fun TabHeader(
    brand: Color,
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderIconButton(icon = Icons.AutoMirrored.Filled.List, tint = brand, onClick = onBack)
        Spacer(Modifier.weight(1f))
        actions()
    }
}

/** タイトル行。編集モードでは小さめ表示＋鉛筆アイコンを付け、タップで改名。 */
@Composable
fun TabTitle(
    title: String,
    brand: Color,
    editing: Boolean,
    onEditTitle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (editing) Modifier.clickable { onEditTitle() } else Modifier)
            .padding(top = if (editing) 6.dp else 20.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = brand,
            fontWeight = FontWeight.Bold,
            fontSize = if (editing) 24.sp else 34.sp,
        )
        if (editing) {
            Spacer(Modifier.size(6.dp))
            Icon(Icons.Filled.Edit, contentDescription = null, tint = brand.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }
    }
}

/** 「完了/編集」トグルのテキストボタン。 */
@Composable
fun EditToggle(editing: Boolean, brand: Color, onToggle: () -> Unit) {
    Text(
        text = stringResource(if (editing) R.string.done else R.string.edit),
        color = brand,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onToggle() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
    )
}

/** 色チップ（正方形）。 */
@Composable
fun ColorSwatch(color: Color, size: Int = 16) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color),
    )
}
