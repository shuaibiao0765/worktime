package top.minis.worktime.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import top.minis.worktime.TimeFormat
import top.minis.worktime.WorkEntry
import top.minis.worktime.WorkStore
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SliderPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val THEME_OPTIONS = listOf("跟随系统" to 0, "浅色" to 1, "深色" to 2)

@Composable
fun SettingsScreen(
    store: WorkStore,
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    snackbar: SnackbarHostState,
) {
    val context = LocalContext.current
    var showTheme by remember { mutableStateOf(false) }
    var showProject by remember { mutableStateOf(false) }
    var showClear by remember { mutableStateOf(false) }
    var projectDraft by remember { mutableStateOf(store.defaultProject) }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
    ) {
        SmallTitle("计时与目标")
        Card(modifier = Modifier.padding(horizontal = 16.dp)) {
            ArrowPreference(
                title = "默认项目",
                summary = store.defaultProject,
                onClick = {
                    projectDraft = store.defaultProject
                    showProject = true
                },
            )
            HorizontalDivider()
            SliderPreference(
                value = store.weeklyGoalHours.toFloat(),
                onValueChange = { store.weeklyGoalHours = it.roundToInt() },
                title = "每周目标",
                valueText = "${store.weeklyGoalHours} 小时",
                valueRange = 1f..80f,
            )
        }

        SmallTitle("外观")
        Card(modifier = Modifier.padding(horizontal = 16.dp)) {
            SwitchPreference(
                checked = store.dynamicColor,
                onCheckedChange = { store.dynamicColor = it },
                title = "动态取色",
                summary = "跟随系统壁纸生成配色",
            )
            HorizontalDivider()
            ArrowPreference(
                title = "主题",
                summary = THEME_OPTIONS.first { it.second == store.themeMode }.first,
                onClick = { showTheme = true },
            )
        }

        SmallTitle("数据")
        Card(modifier = Modifier.padding(horizontal = 16.dp)) {
            ArrowPreference(
                title = "导出记录",
                summary = "以 CSV 文本分享，共 ${store.entries.size} 条",
                onClick = {
                    if (store.entries.isEmpty()) {
                        scope.launch { snackbar.showSnackbar("还没有可导出的记录") }
                    } else {
                        exportCsv(context, store.entries)
                    }
                },
            )
            HorizontalDivider()
            ArrowPreference(
                title = "清除所有记录",
                summary = "此操作不可撤销",
                onClick = { showClear = true },
            )
        }

        Text(
            text = "数据只保存在本机，不上传。",
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        )
    }

    // 主题选择
    OverlayDialog(
        show = showTheme,
        title = "主题",
        onDismissRequest = { showTheme = false },
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            THEME_OPTIONS.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            store.themeMode = value
                            showTheme = false
                        }
                        .padding(horizontal = 8.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = label, modifier = Modifier.weight(1f))
                    if (store.themeMode == value) {
                        Icon(
                            imageVector = MiuixIcons.Ok,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }

    // 默认项目
    OverlayDialog(
        show = showProject,
        title = "默认项目",
        summary = "新的计时默认使用这个项目名",
        onDismissRequest = { showProject = false },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = TextFieldValue(projectDraft),
                onValueChange = { projectDraft = it.text },
                modifier = Modifier.fillMaxWidth(),
                label = "项目名",
                singleLine = true,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    text = "取消",
                    onClick = { showProject = false },
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        store.defaultProject = projectDraft
                        showProject = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text("保存")
                }
            }
        }
    }

    // 清除确认
    OverlayDialog(
        show = showClear,
        title = "清除所有记录？",
        summary = "共 ${store.entries.size} 条，删除后无法恢复",
        onDismissRequest = { showClear = false },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextButton(
                text = "取消",
                onClick = { showClear = false },
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = {
                    store.clearAll()
                    showClear = false
                    scope.launch { snackbar.showSnackbar("记录已清除") }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.error,
                    contentColor = MiuixTheme.colorScheme.onError,
                ),
            ) {
                Text("清除")
            }
        }
    }
}

private fun exportCsv(context: Context, entries: List<WorkEntry>) {
    val header = "日期,开始,结束,项目,分钟"
    val rows = entries.sortedBy { it.start }.map { entry ->
        val project = entry.project.replace("\"", "\"\"")
        "${entry.date},${TimeFormat.timeOfDay(entry.start)},${TimeFormat.timeOfDay(entry.end)},\"$project\",${entry.duration / 60_000L}"
    }
    val csv = (listOf(header) + rows).joinToString("\n")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "工时记录")
        putExtra(Intent.EXTRA_TEXT, csv)
    }
    context.startActivity(Intent.createChooser(intent, "导出工时记录"))
}
