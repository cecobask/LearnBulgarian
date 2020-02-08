package bask.lingvino.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
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
) : RecyclerView.Adapter<TranslationsAdapter.TranslationHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationHolder =
        TranslationHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.translation_item,
                parent,
                false
            )
        )

    override fun getItemCount(): Int = translations.size

    override fun onBindViewHolder(holder: TranslationHolder, position: Int) {
        val translation = translations[holder.adapterPosition]
        // Bind viewHolder items to the RecyclerView.
        tracker?.let {
            holder.bindItems(
                translation,
                it.isSelected(getItemID(holder.adapterPosition))
            )
        }
    }

    // Rotates the arrow by 180 degrees.
    private fun rotateView(imageView: ImageView, from: Float, to: Float) {
        val rotate = RotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f)
            .apply {
                duration = 350
                interpolator = LinearInterpolator()
                isFillEnabled = true
                fillAfter = true
            }
        imageView.startAnimation(rotate)
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

    inner class TranslationHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val view: View = v

        init {
            // Expand / collapse view.
            v.setOnClickListener {
                val translation = translations[adapterPosition]
                val expanded: Boolean = translation.expanded
                translation.expanded = !expanded
                notifyItemChanged(adapterPosition)
            }
        }

        fun bindItems(
            translation: Translation,
            isActivated: Boolean = false
        ) {
            // Populate values to widgets.
            view.phraseTV.text = translation.input
            view.sourceTV.text = translation.input
            view.targetTV.text = translation.translation
            if (translation.expanded) {
                view.translationLL.visibility = View.VISIBLE
                rotateView(view.findViewById(R.id.arrow), 0f, 180f)
            } else {
                view.translationLL.visibility = View.GONE
                rotateView(view.findViewById(R.id.arrow), 180f, 0f)
            }

            when (translation.sourceLang) {
                "Bulgarian" -> view.srcLangIV.setImageResource(R.drawable.ic_bulgarian)
                "English" -> view.srcLangIV.setImageResource(R.drawable.ic_english)
                "Spanish" -> view.srcLangIV.setImageResource(R.drawable.ic_spanish)
                "Russian" -> view.srcLangIV.setImageResource(R.drawable.ic_russian)
            }

            when (translation.targetLang) {
                "Bulgarian" -> view.tgtLangIV.setImageResource(R.drawable.ic_bulgarian)
                "English" -> view.tgtLangIV.setImageResource(R.drawable.ic_english)
                "Spanish" -> view.tgtLangIV.setImageResource(R.drawable.ic_spanish)
                "Russian" -> view.tgtLangIV.setImageResource(R.drawable.ic_russian)
            }

            itemView.isActivated = isActivated
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> =
            object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): String? = translations[adapterPosition].id
            }
    }
}