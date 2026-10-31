package top.minis.worktime.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay
import top.minis.worktime.WorkStore
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Tasks
import top.yukonga.miuix.kmp.icon.extended.Timer

private enum class Tab(val label: String, val title: String, val largeTitle: String) {
    Clock("计时", "今日", "工时"),
    Records("记录", "记录", "工时记录"),
    Settings("设置", "设置", "设置"),
}

@Composable
fun WorkTimeApp(store: WorkStore) {
    WorkTimeTheme(store) {
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val scrollBehavior = MiuixScrollBehavior()

        var tab by rememberSaveable { mutableStateOf(Tab.Clock.ordinal) }
        var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

        // 计时中每秒刷新一次；未计时时只刷新一次。
        LaunchedEffect(store.isRunning) {
            while (store.isRunning) {
                now = System.currentTimeMillis()
                delay(1_000L)
            }
            now = System.currentTimeMillis()
        }

        val view = LocalView.current
        LaunchedEffect(store.isRunning) {
            view.keepScreenOn = store.isRunning
        }

        val current = Tab.entries[tab]

        Scaffold(
            topBar = {
                TopAppBar(
                    title = current.title,
                    largeTitle = current.largeTitle,
                    scrollBehavior = scrollBehavior,
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == Tab.Clock.ordinal,
                        onClick = { tab = Tab.Clock.ordinal },
                        icon = MiuixIcons.Timer,
                        label = Tab.Clock.label,
                    )
                    NavigationBarItem(
                        selected = tab == Tab.Records.ordinal,
                        onClick = { tab = Tab.Records.ordinal },
                        icon = MiuixIcons.Tasks,
                        label = Tab.Records.label,
                    )
                    NavigationBarItem(
                        selected = tab == Tab.Settings.ordinal,
                        onClick = { tab = Tab.Settings.ordinal },
                        icon = MiuixIcons.Settings,
                        label = Tab.Settings.label,
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            val contentModifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)

            when (current) {
                Tab.Clock -> ClockScreen(store, now, contentModifier, scope, snackbar)
                Tab.Records -> RecordsScreen(store, contentModifier, scope, snackbar)
                Tab.Settings -> SettingsScreen(store, contentModifier, scope, snackbar)
            }
        }
    }
}
