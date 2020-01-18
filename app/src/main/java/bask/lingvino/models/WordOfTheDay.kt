package bask.lingvino.models

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WordOfTheDay (
    val wordDate: String = "",
    val word: String = "",
    val wordEng: String = "",
    val wordTransliteration: String = "",
    val wordType: String = "",
    val wordDefinition: String = "",
    val exampleSentenceEN: String = "",
    val exampleSentenceBG: String = "",
    val pronunciationURL: String = ""): Comparable<WordOfTheDay> {

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