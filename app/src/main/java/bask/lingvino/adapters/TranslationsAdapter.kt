package bask.lingvino.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import bask.lingvino.R
import bask.lingvino.models.Translation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.translation_item.view.*

class TranslationsAdapter(var translations: ArrayList<Translation>,
                          recyclerView: RecyclerView,
                          val activity: FragmentActivity?
) : RecyclerView.Adapter<TranslationsAdapter.CollectionHolder>() {

    private val rv = recyclerView
    var tracker: SelectionTracker<String>? = null

    fun getItem(position: Int): Translation = translations[position]

    private fun getItemID(position: Int): String = translations[position].id

    fun getPosition(id: String): Int = translations.indexOfFirst { it.id == id }

    fun getSelectedItemsById(selectedIds: List<String>): List<Translation> {
        // Returns a list of Translations that match the ids.
        return translations.filter { translation ->
            selectedIds.contains(translation.id)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionHolder =
        CollectionHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.translation_item,
                parent,
                false
            )
        )

    override fun getItemCount(): Int = translations.size

    override fun onBindViewHolder(holder: CollectionHolder, position: Int) {
        val sharedPref = activity?.getSharedPreferences("learnBulgarian", 0)
        val targetLang = sharedPref?.getString("TARGET_LANG_NAME", "Bulgarian")
        val translation = translations[holder.adapterPosition]

        // Bind viewHolder items to the RecyclerView.
        tracker?.let {
            holder.bindItems(
                translation,
                it.isSelected(getItemID(holder.adapterPosition)),
                targetLang
            )
        }

        // Swap expand value with the opposite.
        holder.itemView.setOnClickListener {
            val expanded: Boolean = translation.expanded
            translation.expanded = !expanded
            notifyItemChanged(position)
        }
    }

    fun removeItems(translationsToRemove: List<Translation>,
                    collectionRef: DatabaseReference
    ) {
        // Remove translations from Firebase DB and RecyclerView.
        translationsToRemove.forEach { translation ->
            collectionRef.child(translation.id).removeValue()
            translations.remove(translation)
            notifyDataSetChanged()
        }

        // Offer the user to undo the deletion.
        Snackbar.make(rv, "Successful deletion.", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                translationsToRemove.forEach { translation ->
                    collectionRef.child(translation.id).setValue(translation)
                    translations.add(translation)
                    notifyDataSetChanged()
                }
            }
            .apply {
                if (translations.isNotEmpty()) show()
            }
    }

    inner class CollectionHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val view: View = v

        fun bindItems(
            translation: Translation,
            isActivated: Boolean = false,
            targetLang: String?
        ) {
            // Populate values to widgets.
            view.phraseTV.text = translation.input
            view.sourceTV.text = translation.input
            view.targetTV.text = translation.translation
            view.translationLL.visibility = if (translation.expanded) View.VISIBLE else View.GONE


//            when (targetLang) {
//                "Bulgarian" -> view.targetText.text = collection.translations
//                "English" -> view.wordOfTheDayTV.text = wordOfTheDay.wordEN
//                "Spanish" -> view.wordOfTheDayTV.text = wordOfTheDay.wordES
//                "Russian" -> view.wordOfTheDayTV.text = wordOfTheDay.wordRU
//            }

            itemView.isActivated = isActivated
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> =
            object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): String? = translations[adapterPosition].id
            }
    }
}