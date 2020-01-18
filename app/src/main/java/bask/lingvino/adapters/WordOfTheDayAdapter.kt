package bask.lingvino.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import bask.lingvino.R
import bask.lingvino.fragments.WordOfTheDayFragment
import bask.lingvino.models.WordOfTheDay
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.wotd_item.view.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WordOfTheDayAdapter(
    var favouriteWords: ArrayList<WordOfTheDay>,
    recyclerView: RecyclerView, fragmentManager: FragmentManager?
) :
    RecyclerView.Adapter<WordOfTheDayAdapter.WordHolder>() {

    private val rv = recyclerView
    private val fm = fragmentManager
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
        val snackBar = Snackbar.make(rv, "Successful deletion.", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                wordsToRemove.forEach { word ->
                    favWordsRef.child(word.wordDate).setValue(word)
                    favouriteWords.add(word)
                    notifyDataSetChanged()
                }
            }
        snackBar.apply {
            if (favouriteWords.isNotEmpty()) show()
        }
    }

    inner class WordHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val view: View = v

        init {
            v.setOnClickListener {
                val date = favouriteWords[adapterPosition].wordDate
                fm!!.beginTransaction()
                    .replace(R.id.fragmentContainer, WordOfTheDayFragment.newInstance(date))
                    .addToBackStack(null)
                    .commit()
            }
        }

        fun bindItems(
            wordOfTheDay: WordOfTheDay,
            isActivated: Boolean = false
        ) {
            val date = LocalDate.parse(wordOfTheDay.wordDate, DateTimeFormatter.ofPattern("d-M-yyyy"))
            val formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            view.wordOfTheDayTV.text = wordOfTheDay.word
            view.wordOfTheDayDate.text = formattedDate
            itemView.isActivated = isActivated
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
            }
    }
}