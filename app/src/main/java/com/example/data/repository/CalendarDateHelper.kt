package com.example.data.repository

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class CalendarViewMode(val label: String, val shortLabel: String) {
    ONE_DAY("1 Hari", "1H"),
    ONE_WEEK("1 Minggu", "1Mgg"),
    ONE_MONTH("1 Bulan", "1Bln"),
    THREE_MONTHS("3 Bulan", "3Bln"),
    SIX_MONTHS("6 Bulan", "6Bln"),
    ONE_YEAR("1 Tahun", "1Thn")
}

data class CalendarDayCell(
    val dateStr: String,
    val dayOfMonth: Int,
    val monthZeroBased: Int,
    val year: Int,
    val dayOfWeekShort: String,
    val isCurrentMonth: Boolean = true
)

data class MonthCalendarData(
    val year: Int,
    val monthZeroBased: Int,
    val monthTitle: String,
    val leadingEmptySlots: Int,
    val days: List<CalendarDayCell>
)

object CalendarDateHelper {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val monthNamesId = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    private val monthShortId = listOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )

    private val weekDaysMondayFirst = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    fun mondayFirstHeaderLabels(): List<String> = weekDaysMondayFirst

    fun parseDateOrToday(dateStr: String): Calendar {
        val cal = Calendar.getInstance()
        try {
            val parsed = dateFormatter.parse(dateStr)
            if (parsed != null) {
                cal.time = parsed
            }
        } catch (_: Exception) {
        }
        return cal
    }

    fun formatDate(cal: Calendar): String = dateFormatter.format(cal.time)

    fun getMonthName(monthZeroBased: Int, year: Int): String {
        val idx = monthZeroBased.coerceIn(0, 11)
        return "${monthNamesId[idx]} $year"
    }

    fun getMonthShort(monthZeroBased: Int): String {
        val idx = monthZeroBased.coerceIn(0, 11)
        return monthShortId[idx]
    }

    fun getDayOfWeekShortId(cal: Calendar): String {
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Sen"
            Calendar.TUESDAY -> "Sel"
            Calendar.WEDNESDAY -> "Rab"
            Calendar.THURSDAY -> "Kam"
            Calendar.FRIDAY -> "Jum"
            Calendar.SATURDAY -> "Sab"
            else -> "Min"
        }
    }

    /**
     * Builds the 7-day Monday..Sunday week containing [anchorDateStr].
     */
    fun generateWeekDays(anchorDateStr: String): List<CalendarDayCell> {
        val cal = parseDateOrToday(anchorDateStr)
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val offsetFromMonday = when (dow) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            else -> 6
        }
        cal.add(Calendar.DAY_OF_MONTH, -offsetFromMonday)

        return (0 until 7).map {
            val cell = CalendarDayCell(
                dateStr = formatDate(cal),
                dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                monthZeroBased = cal.get(Calendar.MONTH),
                year = cal.get(Calendar.YEAR),
                dayOfWeekShort = getDayOfWeekShortId(cal),
                isCurrentMonth = true
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
            cell
        }
    }

    /**
     * Generates a single calendar month with Monday-first leading empty offset.
     */
    fun generateMonthData(year: Int, monthZeroBased: Int): MonthCalendarData {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroBased)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDow = cal.get(Calendar.DAY_OF_WEEK)
        val leadingEmpty = when (firstDow) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            else -> 6
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val days = (1..daysInMonth).map { day ->
            cal.set(Calendar.DAY_OF_MONTH, day)
            CalendarDayCell(
                dateStr = formatDate(cal),
                dayOfMonth = day,
                monthZeroBased = monthZeroBased,
                year = year,
                dayOfWeekShort = getDayOfWeekShortId(cal),
                isCurrentMonth = true
            )
        }
        return MonthCalendarData(
            year = year,
            monthZeroBased = monthZeroBased,
            monthTitle = getMonthName(monthZeroBased, year),
            leadingEmptySlots = leadingEmpty,
            days = days
        )
    }

    /**
     * Generates [monthCount] consecutive months starting from the period containing [anchorDateStr].
     * For 12 months (ONE_YEAR), starts in January of the anchor's year.
     */
    fun generateMultiMonthData(anchorDateStr: String, monthCount: Int): List<MonthCalendarData> {
        val cal = parseDateOrToday(anchorDateStr)
        val anchorYear = cal.get(Calendar.YEAR)
        val anchorMonth = cal.get(Calendar.MONTH)

        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, anchorYear)
            set(Calendar.DAY_OF_MONTH, 1)
            if (monthCount >= 12) {
                set(Calendar.MONTH, Calendar.JANUARY)
            } else {
                set(Calendar.MONTH, anchorMonth)
            }
        }

        return (0 until monthCount).map {
            val data = generateMonthData(
                year = startCal.get(Calendar.YEAR),
                monthZeroBased = startCal.get(Calendar.MONTH)
            )
            startCal.add(Calendar.MONTH, 1)
            data
        }
    }

    fun shiftAnchorDate(anchorDateStr: String, mode: CalendarViewMode, step: Int): String {
        val cal = parseDateOrToday(anchorDateStr)
        when (mode) {
            CalendarViewMode.ONE_DAY -> cal.add(Calendar.DAY_OF_MONTH, step)
            CalendarViewMode.ONE_WEEK -> cal.add(Calendar.WEEK_OF_YEAR, step)
            CalendarViewMode.ONE_MONTH -> cal.add(Calendar.MONTH, step)
            CalendarViewMode.THREE_MONTHS -> cal.add(Calendar.MONTH, step * 3)
            CalendarViewMode.SIX_MONTHS -> cal.add(Calendar.MONTH, step * 6)
            CalendarViewMode.ONE_YEAR -> cal.add(Calendar.YEAR, step)
        }
        return formatDate(cal)
    }

    fun getPeriodHeaderLabel(anchorDateStr: String, mode: CalendarViewMode): String {
        val cal = parseDateOrToday(anchorDateStr)
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return when (mode) {
            CalendarViewMode.ONE_DAY -> "${getDayOfWeekShortId(cal)}, $d ${getMonthName(m, y)}"
            CalendarViewMode.ONE_WEEK -> {
                val week = generateWeekDays(anchorDateStr)
                val first = week.first()
                val last = week.last()
                "${first.dayOfMonth} ${getMonthShort(first.monthZeroBased)} – ${last.dayOfMonth} ${getMonthShort(last.monthZeroBased)} ${last.year}"
            }
            CalendarViewMode.ONE_MONTH -> getMonthName(m, y)
            CalendarViewMode.THREE_MONTHS -> {
                val endCal = (cal.clone() as Calendar).apply { add(Calendar.MONTH, 2) }
                "${getMonthShort(m)} $y – ${getMonthShort(endCal.get(Calendar.MONTH))} ${endCal.get(Calendar.YEAR)}"
            }
            CalendarViewMode.SIX_MONTHS -> {
                val endCal = (cal.clone() as Calendar).apply { add(Calendar.MONTH, 5) }
                "${getMonthShort(m)} $y – ${getMonthShort(endCal.get(Calendar.MONTH))} ${endCal.get(Calendar.YEAR)}"
            }
            CalendarViewMode.ONE_YEAR -> "Kalender Tahun $y"
        }
    }
}
