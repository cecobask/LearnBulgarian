package bask.learnbulgarian.models

data class WordDefinition(
    val word: String,
    val meaning: String,
    val transliteration: String? = null,
    val type: String? = null,
    val exampleEN: String? = null,
    val exampleBG: String? = null
)