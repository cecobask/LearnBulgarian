package bask.lingvino.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import bask.lingvino.R
import bask.lingvino.fragments.WordOfTheDayFragment
import bask.lingvino.models.WordOfTheDay
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.wordoftheday_item.view.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WordOfTheDayAdapter(
    var favouriteWords: ArrayList<WordOfTheDay>,
    recyclerView: RecyclerView,
    fragmentManager: FragmentManager?,
    val activity: FragmentActivity?
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
        WordHolder(LayoutInflater.from(parent.context).inflate(R.layout.wordoftheday_item, parent, false))

    override fun getItemCount(): Int = favouriteWords.size

    override fun onBindViewHolder(holder: WordHolder, position: Int) {
        val sharedPref = activity?.getSharedPreferences("learnBulgarian", 0)
        val targetLang = sharedPref?.getString("TARGET_LANG_NAME", "Bulgarian")
        // Bind viewHolder items to the RecyclerView.
        tracker?.let {
            holder.bindItems(favouriteWords[holder.adapterPosition], it.isSelected(getItemId(holder.adapterPosition)), targetLang)
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
                val wotdFragment = WordOfTheDayFragment.newInstance(date)
                fm!!.beginTransaction()
                    .replace(R.id.fragmentContainer, wotdFragment, wotdFragment.javaClass.simpleName)
                    .addToBackStack(null)
                    .commit()
            }
        }

        fun bindItems(
            wordOfTheDay: WordOfTheDay,
            isActivated: Boolean = false,
            targetLang: String?
        ) {
            val date = LocalDate.parse(wordOfTheDay.wordDate, DateTimeFormatter.ofPattern("d-M-yyyy"))
            val formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            when (targetLang) {
                "Bulgarian" -> view.wordOfTheDayTV.text = wordOfTheDay.wordBG
                "English" -> view.wordOfTheDayTV.text = wordOfTheDay.wordEN
                "Spanish" -> view.wordOfTheDayTV.text = wordOfTheDay.wordES
                "Russian" -> view.wordOfTheDayTV.text = wordOfTheDay.wordRU
            }
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