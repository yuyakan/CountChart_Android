package com.kanbe1365.countergraph.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kanbe1365.countergraph.R
import com.kanbe1365.countergraph.data.ChartType
import com.kanbe1365.countergraph.ui.theme.LocalBrandColor

/**
 * ファイル詳細画面。4つのチャート種別をボトムタブで切り替える。
 * iOS の FileView（TabView）に相当。
 */
@Composable
fun FileScreen(
    fileId: String,
    onBack: () -> Unit,
) {
    val brand = LocalBrandColor.current
    val viewModel: FileViewModel = viewModel(
        key = "file_$fileId",
        factory = viewModelFactory {
            initializer { FileViewModel(fileId) }
        },
    )
    var selected by rememberSaveable { mutableStateOf(ChartType.BAR) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // 下タブの背景は画面と同じ白（ダーク時は黒）に合わせる。
            NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                TabItem(ChartType.BAR, Icons.Filled.BarChart, stringResource(R.string.bar_chart), selected, brand) { selected = it }
                TabItem(ChartType.PIE, Icons.Filled.PieChart, stringResource(R.string.pie_chart), selected, brand) { selected = it }
                TabItem(ChartType.RANKING, Icons.AutoMirrored.Filled.List, stringResource(R.string.ranking), selected, brand) { selected = it }
                TabItem(ChartType.GROUP, Icons.Filled.GridView, stringResource(R.string.groups), selected, brand) { selected = it }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            when (selected) {
                ChartType.BAR -> BarTab(viewModel, onBack, Modifier.padding(padding))
                ChartType.PIE -> PieTab(viewModel, onBack, Modifier.padding(padding))
                ChartType.RANKING -> RankingTab(viewModel, onBack, Modifier.padding(padding))
                ChartType.GROUP -> GroupTab(viewModel, onBack, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun RowScope.TabItem(
    type: ChartType,
    icon: ImageVector,
    label: String,
    selected: ChartType,
    brand: androidx.compose.ui.graphics.Color,
    onSelect: (ChartType) -> Unit,
) {
    NavigationBarItem(
        selected = selected == type,
        onClick = { onSelect(type) },
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, fontWeight = FontWeight.Medium) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = brand,
            selectedTextColor = brand,
            indicatorColor = brand.copy(alpha = 0.12f),
        ),
    )
}
