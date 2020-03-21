package bask.lingvino.models

import org.threeten.bp.LocalDate

data class CalendarWord(val date: LocalDate,
                        val targetWord: String,
                        val sourceWord: String
) : Comparable<CalendarWord> {
    override fun compareTo(other: CalendarWord): Int = date.compareTo(other.date)
}