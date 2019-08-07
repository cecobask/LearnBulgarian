package bask.learnbulgarian.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bask.learnbulgarian.R
import bask.learnbulgarian.models.WordDefinition
import kotlinx.android.synthetic.main.wotd_item.view.*

class WordOfTheDayAdapter(private val definitions: ArrayList<WordDefinition>) :
    RecyclerView.Adapter<WordOfTheDayAdapter.DefinitionHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefinitionHolder =
        DefinitionHolder(LayoutInflater.from(parent.context).inflate(R.layout.wotd_item, parent, false))

    override fun getItemCount(): Int = definitions.size

    override fun onBindViewHolder(holder: DefinitionHolder, position: Int) = holder.bindItems(definitions[position], position)

    class DefinitionHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val view: View = v

        fun bindItems(definition: WordDefinition, position: Int) {
            val typeStr = "${position+1}. ${definition.type}"
            view.wotdTypeTV.text = typeStr
            view.wotdDefinitionTV.text = definition.meaning
            if (definition.exampleEN == null) {
                view.wotdENExampleTV.visibility = View.GONE
                view.wotdBGExampleTV.visibility = View.GONE
            } else {
                view.wotdENExampleTV.text = definition.exampleEN
                view.wotdBGExampleTV.text = definition.exampleBG
            }
        }
    }
}