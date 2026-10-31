package top.minis.worktime

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 一条工时记录。end 一定大于 start。 */
data class WorkEntry(
    val id: Long,
    val project: String,
    val start: Long,
    val end: Long,
) {
    val duration: Long get() = (end - start).coerceAtLeast(0L)

    val date: LocalDate
        get() = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate()
}

object TimeFormat {

    private val hhmm = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val dayPattern = DateTimeFormatter.ofPattern("M 月 d 日 EEE", Locale.getDefault())

    /** 09:12 */
    fun timeOfDay(epochMillis: Long): String =
        hhmm.format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))

    /** 计时器样式：03:25 或 1:03:25 */
    fun stopwatch(millis: Long): String {
        val total = (millis / 1000).coerceAtLeast(0)
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) {
            String.format(Locale.US, "%d:%02d:%02d", h, m, s)
        } else {
            String.format(Locale.US, "%02d:%02d", m, s)
        }
    }

    /** 人类可读时长：2 小时 15 分 */
    fun duration(millis: Long): String {
        val minutes = millis / 60_000L
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h > 0 && m > 0 -> "$h 小时 $m 分"
            h > 0 -> "$h 小时"
            else -> "$m 分"
        }
    }

    /** 用于大号数字：7.5 */
    fun hoursText(millis: Long): String =
        String.format(Locale.US, "%.1f", millis / 3_600_000.0)

    fun dayLabel(date: LocalDate): String {
        val today = LocalDate.now()
        return when (date) {
            today -> "今天"
            today.minusDays(1) -> "昨天"
            else -> date.format(dayPattern)
        }
    }
}
