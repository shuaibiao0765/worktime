package top.minis.worktime.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import top.minis.worktime.TimeFormat
import top.minis.worktime.WorkStore
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ClockScreen(
    store: WorkStore,
    now: Long,
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    snackbar: SnackbarHostState,
) {
    val running = store.isRunning
    var project by rememberSaveable { mutableStateOf(store.defaultProject) }
    var showManual by remember { mutableStateOf(false) }

    val todayEntries = store.entries.filter { it.date == LocalDate.now() }
    val todayTotal = todayEntries.sumOf { it.duration }
    val dailyGoal = (store.weeklyGoalHours * 3_600_000L / 7).coerceAtLeast(1L)

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            insideMargin = PaddingValues(20.dp),
        ) {
            Text(
                text = if (running) "正在计时" else "准备开始",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Text(
                text = TimeFormat.stopwatch(
                    if (running) (now - store.runningStart).coerceAtLeast(0L) else 0L,
                ),
                fontSize = 52.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )

            if (running) {
                Text(
                    text = store.runningProject,
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            } else {
                TextField(
                    value = TextFieldValue(project),
                    onValueChange = { project = it.text },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    label = "项目",
                    singleLine = true,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = {
                        if (running) {
                            val entry = store.stop()
                            scope.launch {
                                snackbar.showSnackbar(
                                    if (entry != null) {
                                        "已记录 ${TimeFormat.duration(entry.duration)}"
                                    } else {
                                        "已结束"
                                    },
                                )
                            }
                        } else {
                            store.start(project)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (running) {
                        ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.error,
                            contentColor = MiuixTheme.colorScheme.onError,
                        )
                    } else {
                        ButtonDefaults.buttonColorsPrimary()
                    },
                ) {
                    Icon(
                        imageVector = if (running) MiuixIcons.Ok else MiuixIcons.Play,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (running) "结束" else "开始")
                }
                TextButton(
                    text = "补录",
                    onClick = { showManual = true },
                )
            }
        }

        SmallTitle("今日进度")
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            insideMargin = PaddingValues(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "今日总时长",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    Text(
                        text = TimeFormat.duration(todayTotal),
                        style = MiuixTheme.textStyles.title3,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${todayEntries.size} 条记录",
                        style = MiuixTheme.textStyles.body2,
                    )
                    Text(
                        text = "目标 ${TimeFormat.duration(dailyGoal)}",
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            LinearProgressIndicator(
                progress = (todayTotal.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
        }

        SmallTitle("今日记录")
        if (todayEntries.isEmpty()) {
            EmptyHint("还没有记录，点「开始」计时，或用「补录」手工添加")
        } else {
            todayEntries.take(6).forEach { entry ->
                EntryCard(
                    entry = entry,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }
    }

    ManualEntryDialog(
        store = store,
        visible = showManual,
        onDismiss = { showManual = false },
        scope = scope,
        snackbar = snackbar,
    )
}
