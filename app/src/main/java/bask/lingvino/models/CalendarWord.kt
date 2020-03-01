package bask.lingvino.models

import org.threeten.bp.LocalDate

data class CalendarWord(val date: LocalDate,
                        val targetWord: String,
                        val sourceWord: String
)