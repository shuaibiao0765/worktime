package top.minis.worktime.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import top.minis.worktime.TimeFormat
import top.minis.worktime.WorkEntry
import top.minis.worktime.WorkStore
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val RANGES = listOf("今天", "本周", "全部")

@Composable
fun RecordsScreen(
    store: WorkStore,
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    snackbar: SnackbarHostState,
) {
    var range by rememberSaveable { mutableStateOf(0) }
    var pendingDelete by remember { mutableStateOf<WorkEntry?>(null) }

    val today = LocalDate.now()
    val filtered = when (range) {
        0 -> store.entries.filter { it.date == today }
        1 -> store.entries.filter { it.date >= today.minusDays(6) }
        else -> store.entries
    }
    val total = filtered.sumOf { it.duration }
    val days = filtered.groupBy { it.date }.entries.sortedByDescending { it.key }
    val average = if (days.isEmpty()) 0L else total / days.size

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            TabRow(
                tabs = RANGES,
                selectedTabIndex = range,
                onTabSelected = { range = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                insideMargin = PaddingValues(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "合计",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        Text(
                            text = TimeFormat.duration(total),
                            style = MiuixTheme.textStyles.title3,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${filtered.size} 条",
                            style = MiuixTheme.textStyles.body2,
                        )
                        Text(
                            text = "日均 ${TimeFormat.duration(average)}",
                            style = MiuixTheme.textStyles.footnote2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
        }

        if (days.isEmpty()) {
            item { EmptyHint("这个时间段还没有记录") }
        } else {
            days.forEach { (date, dayEntries) ->
                item(key = "day-$date") {
                    SmallTitle(TimeFormat.dayLabel(date))
                }
                items(
                    items = dayEntries.sortedByDescending { it.start },
                    key = { it.id },
                ) { entry ->
                    EntryCard(
                        entry = entry,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        onClick = { pendingDelete = entry },
                    )
                }
            }
        }
    }

    val target = pendingDelete
    OverlayDialog(
        show = target != null,
        title = "删除这条记录？",
        summary = target?.let { "${it.project} · ${TimeFormat.duration(it.duration)}" },
        onDismissRequest = { pendingDelete = null },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextButton(
                text = "取消",
                onClick = { pendingDelete = null },
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = {
                    if (target != null) {
                        store.delete(target.id)
                        pendingDelete = null
                        scope.launch { snackbar.showSnackbar("已删除") }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.error,
                    contentColor = MiuixTheme.colorScheme.onError,
                ),
            ) {
                Text("删除")
            }
        }
    }
}
