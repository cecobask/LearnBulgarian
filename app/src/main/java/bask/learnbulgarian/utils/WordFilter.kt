package bask.learnbulgarian.utils

import android.widget.Filter
import bask.learnbulgarian.adapters.WordOfTheDayAdapter
import bask.learnbulgarian.models.WordOfTheDay


class WordFilter(private val adapter: WordOfTheDayAdapter,
                 private val originalWords: ArrayList<WordOfTheDay>) : Filter() {

    override fun performFiltering(prefix: CharSequence?): FilterResults {
        // Initialise an object that holds the query results.
        val results = FilterResults()

        if (prefix.isNullOrEmpty()) {
            // Returns the original list as the prefix is empty.
            results.values = originalWords
            results.count = originalWords.size
        } else {
            // List of objects that match the criteria.
            val filteredWords = originalWords.filter {
                // Compare word values, ignoring case.
                it.word.contains(prefix.toString(), true)
            }
            results.values = filteredWords
            results.count = filteredWords.size
        }
        return results
    }

    @Suppress("unchecked_cast")
    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapter.favouriteWords = results?.values as ArrayList<WordOfTheDay>

        if (results.count >= 0) adapter.notifyDataSetChanged()
        else {
            adapter.favouriteWords = originalWords
            adapter.notifyDataSetChanged()
        }
    }
}