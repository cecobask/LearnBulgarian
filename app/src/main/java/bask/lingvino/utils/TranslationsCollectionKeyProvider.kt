package bask.lingvino.utils

import androidx.recyclerview.selection.ItemKeyProvider
import bask.lingvino.adapters.TranslationsAdapter

/**
 * ItemKeyProvider class that uses Long as key.
 */
class TranslationsCollectionKeyProvider(private val adapter: TranslationsAdapter) :
    ItemKeyProvider<String>(SCOPE_MAPPED) {

    override fun getKey(position: Int): String = adapter.getItem(position).id
    override fun getPosition(key: String): Int = adapter.getPosition(key)
}