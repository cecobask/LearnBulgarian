package bask.learnbulgarian.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import bask.learnbulgarian.R
import bask.learnbulgarian.models.WordOfTheDay
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.wotd_item.view.*

class WordOfTheDayAdapter(private val favouriteWords: ArrayList<WordOfTheDay>) :
    RecyclerView.Adapter<WordOfTheDayAdapter.WordHolder>() {

    var tracker: SelectionTracker<Long>? = null

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordHolder =
        WordHolder(LayoutInflater.from(parent.context).inflate(R.layout.wotd_item, parent, false))

    override fun getItemCount(): Int = favouriteWords.size

    override fun onBindViewHolder(holder: WordHolder, position: Int) {
        // Bind viewHolder items to the RecyclerView.
        tracker?.let {
            holder.bindItems(favouriteWords[position], it.isSelected(position.toLong()))
        }
    }

    fun removeItems(positions: ArrayList<Int>, favWordsRef: DatabaseReference) {
        // Store objects of the positions to be removed.
        val wordsToRemove = arrayListOf<WordOfTheDay>()
        positions.forEach { wordsToRemove.add(favouriteWords[it]) }
        wordsToRemove.forEach {
            // Remove from Firebase DB and RecyclerView.
            favWordsRef.child(it.wordDate).removeValue()
            favouriteWords.remove(it)
            notifyDataSetChanged()
        }
    }

    fun removeAt(position: Int, favWordsRef: DatabaseReference) {
        // Remove word from Firebase db and from local ArrayList.
        favWordsRef.child(favouriteWords[position].wordDate).removeValue()
        favouriteWords.removeAt(position)
        notifyItemRemoved(position)
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