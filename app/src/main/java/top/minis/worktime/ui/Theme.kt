package top.minis.worktime.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import top.minis.worktime.WorkStore
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

/**
 * 主题：跟随系统 / 强制浅色 / 强制深色，可叠加系统动态取色。
 * 动态取色走 Monet，其余情况用 Miuix 内置配色，风格与 HyperOS 一致。
 */
@Composable
fun WorkTimeTheme(
    store: WorkStore,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val themeMode = store.themeMode
    val dynamic = store.dynamicColor

    val controller = remember(themeMode, dynamic, systemDark) {
        val mode = when (themeMode) {
            1 -> ColorSchemeMode.Light
            2 -> ColorSchemeMode.Dark
            else -> if (dynamic) ColorSchemeMode.MonetSystem else ColorSchemeMode.System
        }
        ThemeController(
            colorSchemeMode = mode,
            keyColor = Color(0xFF3482FF),
            isDark = systemDark,
        )
    }

    MiuixTheme(
        controller = controller,
        content = content,
    )
}
