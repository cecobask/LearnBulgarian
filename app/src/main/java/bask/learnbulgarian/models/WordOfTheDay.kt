package bask.learnbulgarian.models

import com.google.firebase.database.PropertyName

data class WordOfTheDay (
    @PropertyName("word") val word: String = "",
    @PropertyName("wordTransliteration") val wordTransliteration: String = "",
    @PropertyName("wordType") val wordType: String = "",
    @PropertyName("wordDefinition") val wordDefinition: String = "",
    @PropertyName("exampleSentenceEN") val exampleSentenceEN: String = "",
    @PropertyName("exampleSentenceBG") val exampleSentenceBG: String = "")