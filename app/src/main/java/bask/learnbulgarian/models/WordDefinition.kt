package bask.learnbulgarian.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class WordDefinition (val word: String, val meaning: Any) {
    class Deserializer: ResponseDeserializable<Array<WordDefinition>> {
        override fun deserialize(content: String): Array<WordDefinition>? = Gson().fromJson(content, Array<WordDefinition>::class.java)
    }

}