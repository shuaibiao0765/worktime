package top.minis.worktime

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.LocalDate

/**
 * 工时数据与设置。
 *
 * 用 Compose 的 state 承载，界面读写即可触发重组；写入时同步落盘到
 * SharedPreferences，所以杀进程后正在计时的会话也能恢复。
 */
class WorkStore(context: Context) {

    private val prefs = context.getSharedPreferences("worktime", Context.MODE_PRIVATE)

    // ---------------- 记录 ----------------

    var entries by mutableStateOf(loadEntries())
        private set

    /** 计时开始时间戳，0 表示未在计时。 */
    var runningStart by mutableLongStateOf(prefs.getLong(KEY_RUNNING_START, 0L))
        private set

    var runningProject by mutableStateOf(prefs.getString(KEY_RUNNING_PROJECT, "").orEmpty())
        private set

    val isRunning: Boolean get() = runningStart != 0L

    fun start(project: String) {
        if (runningStart != 0L) return
        val name = project.ifBlank { defaultProject }
        runningStart = System.currentTimeMillis()
        runningProject = name
        prefs.edit()
            .putLong(KEY_RUNNING_START, runningStart)
            .putString(KEY_RUNNING_PROJECT, name)
            .apply()
    }

    /** 结束计时并落一条记录，返回刚生成的记录（未在计时则返回 null）。 */
    fun stop(now: Long = System.currentTimeMillis()): WorkEntry? {
        val start = runningStart
        if (start == 0L) return null
        val entry = WorkEntry(
            id = nextId(),
            project = runningProject.ifBlank { defaultProject },
            start = start,
            end = now,
        )
        runningStart = 0L
        runningProject = ""
        prefs.edit()
            .putLong(KEY_RUNNING_START, 0L)
            .remove(KEY_RUNNING_PROJECT)
            .apply()
        save(listOf(entry) + entries)
        return entry
    }

    fun addManual(project: String, minutes: Int) {
        val end = System.currentTimeMillis()
        val entry = WorkEntry(
            id = nextId(),
            project = project.ifBlank { defaultProject },
            start = end - minutes * 60_000L,
            end = end,
        )
        save(entries + entry)
    }

    /** 保证 id 唯一，避免同毫秒内两条记录撞 LazyColumn 的 key。 */
    private fun nextId(): Long {
        val maxExisting = entries.maxOfOrNull { it.id } ?: 0L
        return maxOf(System.currentTimeMillis(), maxExisting + 1L)
    }

    fun delete(id: Long) {
        save(entries.filterNot { it.id == id })
    }

    fun clearAll() {
        save(emptyList())
    }

    private fun save(list: List<WorkEntry>) {
        entries = list.sortedByDescending { it.start }
        prefs.edit().putString(KEY_ENTRIES, encode(entries)).apply()
    }

    // ---------------- 设置 ----------------

    private var _defaultProject by mutableStateOf(
        prefs.getString(KEY_DEFAULT_PROJECT, "工作").orEmpty(),
    )
    var defaultProject: String
        get() = _defaultProject
        set(value) {
            val v = value.ifBlank { "工作" }
            _defaultProject = v
            prefs.edit().putString(KEY_DEFAULT_PROJECT, v).apply()
        }

    private var _weeklyGoalHours by mutableIntStateOf(prefs.getInt(KEY_WEEKLY_GOAL, 40))
    var weeklyGoalHours: Int
        get() = _weeklyGoalHours
        set(value) {
            val v = value.coerceIn(1, 100)
            _weeklyGoalHours = v
            prefs.edit().putInt(KEY_WEEKLY_GOAL, v).apply()
        }

    /** 0 = 跟随系统，1 = 浅色，2 = 深色 */
    private var _themeMode by mutableIntStateOf(prefs.getInt(KEY_THEME, 0))
    var themeMode: Int
        get() = _themeMode
        set(value) {
            _themeMode = value
            prefs.edit().putInt(KEY_THEME, value).apply()
        }

    private var _dynamicColor by mutableStateOf(prefs.getBoolean(KEY_DYNAMIC, true))
    var dynamicColor: Boolean
        get() = _dynamicColor
        set(value) {
            _dynamicColor = value
            prefs.edit().putBoolean(KEY_DYNAMIC, value).apply()
        }

    // ---------------- 统计 ----------------

    fun entriesBetween(from: LocalDate, to: LocalDate): List<WorkEntry> =
        entries.filter { it.date >= from && it.date <= to }

    fun totalMillis(list: List<WorkEntry>): Long = list.sumOf { it.duration }

    // ---------------- 持久化细节 ----------------

    private fun loadEntries(): List<WorkEntry> {
        val raw = prefs.getString(KEY_ENTRIES, null) ?: return emptyList()
        return raw.split('\n')
            .mapNotNull { line ->
                val parts = line.split('|')
                if (parts.size < 4) return@mapNotNull null
                val id = parts[0].toLongOrNull() ?: return@mapNotNull null
                val start = parts[1].toLongOrNull() ?: return@mapNotNull null
                val end = parts[2].toLongOrNull() ?: return@mapNotNull null
                val project = runCatching { URLDecoder.decode(parts[3], "UTF-8") }.getOrDefault("")
                WorkEntry(id, project, start, end)
            }
            .sortedByDescending { it.start }
    }

    private fun encode(list: List<WorkEntry>): String =
        list.joinToString("\n") { e ->
            "${e.id}|${e.start}|${e.end}|${URLEncoder.encode(e.project, "UTF-8")}"
        }

    private companion object {
        const val KEY_ENTRIES = "entries"
        const val KEY_RUNNING_START = "running_start"
        const val KEY_RUNNING_PROJECT = "running_project"
        const val KEY_DEFAULT_PROJECT = "default_project"
        const val KEY_WEEKLY_GOAL = "weekly_goal_hours"
        const val KEY_THEME = "theme_mode"
        const val KEY_DYNAMIC = "dynamic_color"
    }
}
