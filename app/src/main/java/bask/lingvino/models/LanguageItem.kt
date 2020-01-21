package bask.lingvino.models

data class LanguageItem(val languageName: String, val flag: Int) {

    override fun toString(): String {
        return languageName
    }
}