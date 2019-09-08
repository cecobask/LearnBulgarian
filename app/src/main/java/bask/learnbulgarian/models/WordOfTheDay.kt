package bask.learnbulgarian.models

data class WordOfTheDay (
    val wordDate: String = "",
    val word: String = "",
    val wordTransliteration: String = "",
    val wordType: String = "",
    val wordDefinition: String = "",
    val exampleSentenceEN: String = "",
    val exampleSentenceBG: String = "",
    val pronunciationURL: String = "")