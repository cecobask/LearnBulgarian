package bask.lingvino.models

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WordOfTheDay (
    val wordDate: String = "",
    val wordBG: String = "",
    val wordEN: String = "",
    val wordRU: String = "",
    val wordES: String = "",
    val wordTransliterationBG: String = "",
    val wordTransliterationEN: String = "",
    val wordTransliterationRU: String = "",
    val wordTransliterationES: String = "",
    val wordType: String = "",
    val wordDefinitionBG: String = "",
    val wordDefinitionEN: String = "",
    val wordDefinitionRU: String = "",
    val wordDefinitionES: String = "",
    val exampleSentenceEN: String = "",
    val exampleSentenceBG: String = "",
    val exampleSentenceRU: String = "",
    val exampleSentenceES: String = "",
    val pronunciationURL_BG: String = "",
    val pronunciationURL_EN: String = "",
    val pronunciationURL_RU: String = "",
    val pronunciationURL_ES: String = ""): Comparable<WordOfTheDay> {

    override fun compareTo(other: WordOfTheDay): Int {
        // Create a convert function, String -> LocalDate
        val dateTimeStrToLocalDateTime: (String) -> LocalDate = {
            LocalDate.parse(it, DateTimeFormatter.ofPattern("d-M-yyyy"))
        }

        // Compare object based on LocalDate value.
        return wordDate.let(dateTimeStrToLocalDateTime)
            .compareTo(other.wordDate.let(dateTimeStrToLocalDateTime))
    }

}