package bask.lingvino.utils

import android.widget.Filter
import androidx.fragment.app.FragmentActivity
import bask.lingvino.adapters.WordOfTheDayAdapter
import bask.lingvino.fragments.WordOfTheDayFavouritesFragment
import bask.lingvino.models.WordOfTheDay


class WordFilter(private val adapter: WordOfTheDayAdapter,
                 private val originalWords: ArrayList<WordOfTheDay>,
                 private val activity: FragmentActivity
) : Filter() {

    override fun performFiltering(prefix: CharSequence?): FilterResults {
        // Initialise an object that holds the query results.
        val results = FilterResults()
        val sharedPref = activity.getSharedPreferences("learnBulgarian", 0)
        val targetLang = sharedPref?.getString("TARGET_LANG_NAME", "Bulgarian")

        if (prefix.isNullOrEmpty()) {
            // Returns the original list as the prefix is empty.
            results.values = originalWords
            results.count = originalWords.size
        } else {
            // List of objects that match the criteria.
            val filteredWords = originalWords.filter {
                // Compare word values, ignoring case.
                when (targetLang) {
                    "Bulgarian" -> it.wordBG.contains(prefix.toString(), true)
                    "English" -> it.wordEN.contains(prefix.toString(), true)
                    "Spanish" -> it.wordES.contains(prefix.toString(), true)
                    "Russian" -> it.wordRU.contains(prefix.toString(), true)
                    else -> it.wordBG.contains(prefix.toString(), true)
                }
            }
            results.values = filteredWords
            results.count = filteredWords.size
        }
        return results
    }

    @Suppress("unchecked_cast")
    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapter.favouriteWords = results?.values as ArrayList<WordOfTheDay>
        adapter.notifyDataSetChanged()

        val fragment = activity.supportFragmentManager.findFragmentByTag(
            "WordOfTheDayFavouritesFragment"
        ) as WordOfTheDayFavouritesFragment
        fragment.noResultsVisible(results.count == 0) // If no results, show  a feedback message to the user.
    }
}