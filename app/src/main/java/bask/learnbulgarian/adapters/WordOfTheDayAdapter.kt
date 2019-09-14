package bask.learnbulgarian.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import bask.learnbulgarian.R
import bask.learnbulgarian.models.WordOfTheDay
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.wotd_item.view.*

class WordOfTheDayAdapter(private val favouriteWords: ArrayList<WordOfTheDay>, recyclerView: RecyclerView) :
    RecyclerView.Adapter<WordOfTheDayAdapter.WordHolder>() {

    private val rv = recyclerView
//    private var deletedWords: ArrayList<WordOfTheDay>? = null
    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = favouriteWords[position].wordDate
        .replace("-", "")
        .toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordHolder =
        WordHolder(LayoutInflater.from(parent.context).inflate(R.layout.wotd_item, parent, false))

    override fun getItemCount(): Int = favouriteWords.size

    override fun onBindViewHolder(holder: WordHolder, position: Int) {
        // Bind viewHolder items to the RecyclerView.
        tracker?.let {
            holder.bindItems(favouriteWords[holder.adapterPosition], it.isSelected(getItemId(holder.adapterPosition)))
        }
    }

    fun getSelectedItemsById(selectedIds: List<String>): List<WordOfTheDay> {
        // Returns a List of words that match the id (wordDate).
        return favouriteWords.filter { word ->
            selectedIds.contains(word.wordDate.replace("-","")) }
    }

    fun removeItems(wordsToRemove: List<WordOfTheDay>, favWordsRef: DatabaseReference) {
        // Remove words from Firebase DB and RecyclerView.
        wordsToRemove.forEach {
            favWordsRef.child(it.wordDate).removeValue()
            favouriteWords.remove(it)
            notifyDataSetChanged()
        }

        // Offer the user to undo the deletion.
        Snackbar.make(rv, "Successful deletion.", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                wordsToRemove.forEach { word ->
                    favWordsRef.child(word.wordDate).setValue(word)
                    favouriteWords.add(word)
                    notifyDataSetChanged()
                }
            }
            .show()
    }

    inner class WordHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val view: View = v

        fun bindItems(
            wordOfTheDay: WordOfTheDay,
            isActivated: Boolean = false
        ) {
            view.wordOfTheDayTV.text = wordOfTheDay.word
            view.wordOfTheDayDate.text = wordOfTheDay.wordDate
            itemView.isActivated = isActivated
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
            }
    }
}