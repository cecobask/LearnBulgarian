package bask.lingvino.utils

import android.widget.Filter
import bask.lingvino.adapters.TranslationsAdapter
import bask.lingvino.models.Translation


class TranslationFilter(private val adapter: TranslationsAdapter,
                        private val originalTranslations: ArrayList<Translation>
) : Filter() {

    override fun performFiltering(prefix: CharSequence?): FilterResults {
        // Initialise an object that holds the query results.
        val results = FilterResults()

        if (prefix.isNullOrEmpty()) {
            // Returns the original list as the prefix is empty.
            results.values = originalTranslations
            results.count = originalTranslations.size
        } else {
            // List of objects that match the criteria.
            val filteredTranslations = originalTranslations.filter {
                // Compare query with attributes 'input' and 'translation', ignoring case.
                it.input.contains(prefix.toString(), true) ||
                        it.translation.contains(prefix.toString(), true)
            }
            results.values = filteredTranslations
            results.count = filteredTranslations.size
        }
        return results
    }

    @Suppress("unchecked_cast")
    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapter.translations = results?.values as ArrayList<Translation>

        if (results.count >= 0) adapter.notifyDataSetChanged()
        else {
            adapter.translations = originalTranslations
            adapter.notifyDataSetChanged()
        }
    }
}