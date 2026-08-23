package com.kanbe1365.countergraph

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kanbe1365.countergraph.data.Prefs
import com.kanbe1365.countergraph.data.Strings
import com.kanbe1365.countergraph.ui.FileScreen
import com.kanbe1365.countergraph.ui.MenuScreen
import com.kanbe1365.countergraph.ui.MenuViewModel
import com.kanbe1365.countergraph.ui.SplashScreen
import com.kanbe1365.countergraph.ui.theme.CounterGraphTheme
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // データ層が SharedPreferences / 文字列リソースを使えるよう初期化する。
        Prefs.init(this)
        Strings.init(this)
        enableEdgeToEdge()
        setContent {
            CounterGraphTheme {
                AppRoot()
            }
        }
    }
}

/** アプリ全体のルート。スプラッシュ→メニュー→ファイル詳細を state で切り替える。 */
@Composable
private fun AppRoot() {
    var showSplash by remember { mutableStateOf(true) }
    // 開いているファイルID（null ならメニュー画面）。
    var openFileId by remember { mutableStateOf<String?>(null) }
    val menuViewModel: MenuViewModel = viewModel()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            val fileId = openFileId
            if (fileId == null) {
                MenuScreen(
                    onOpenFile = { openFileId = it },
                    viewModel = menuViewModel,
                )
            } else {
                FileScreen(
                    fileId = fileId,
                    onBack = {
                        openFileId = null
                        menuViewModel.reload()
                    },
                )
                // 端末の戻るでもメニューへ戻す。
                BackHandler {
                    openFileId = null
                    menuViewModel.reload()
                }
            }
        }

        // スプラッシュはフェードアウトしてメニューへ遷移する。
        AnimatedVisibility(
            visible = showSplash,
            exit = fadeOut(),
        ) {
            SplashScreen(onFinish = { showSplash = false })
        }
    }
}
