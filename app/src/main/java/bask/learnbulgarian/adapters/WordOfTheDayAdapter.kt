package bask.learnbulgarian.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bask.learnbulgarian.R
import bask.learnbulgarian.models.WordOfTheDay
import kotlinx.android.synthetic.main.wotd_item.view.*

class WordOfTheDayAdapter(private val favouriteWords: ArrayList<WordOfTheDay>) :
    RecyclerView.Adapter<WordOfTheDayAdapter.WordHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordHolder =
        WordHolder(LayoutInflater.from(parent.context).inflate(R.layout.wotd_item, parent, false))

    override fun getItemCount(): Int = favouriteWords.size

    override fun onBindViewHolder(holder: WordHolder, position: Int) =
        holder.bindItems(favouriteWords[position])

    class WordHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val view: View = v

        fun bindItems(wordOfTheDay: WordOfTheDay) {
            view.wordOfTheDayTV.text = wordOfTheDay.word
            view.wordOfTheDayDate.text = wordOfTheDay.wordDate
        }
    }
}