package bask.learnbulgarian.fragments

import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import bask.learnbulgarian.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class WordOfTheDayFragment: Fragment() {

    private lateinit var database: FirebaseDatabase

    companion object {
        fun newInstance(): WordOfTheDayFragment {
            return WordOfTheDayFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.wordoftheday, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fix for error caused by: android.os.NetworkOnMainThreadException.
        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        database = FirebaseDatabase.getInstance()

        val wotdDateTV = view.findViewById<TextView>(R.id.wotdDateTV)
        val wotdTV = view.findViewById<TextView>(R.id.wotdTV)
        val wotdPronounceFAB = view.findViewById<FloatingActionButton>(R.id.wotdPronounceFAB)
        val wotdTransliterationTV = view.findViewById<TextView>(R.id.wotdTransliterationTV)
        val wotdTypeTV = view.findViewById<TextView>(R.id.wotdTypeTV)
        val wotdDefinitionTV = view.findViewById<TextView>(R.id.wotdDefinitionTV)
        val wotdBGExampleTV = view.findViewById<TextView>(R.id.wotdBGExampleTV)
        val wotdENExampleTV = view.findViewById<TextView>(R.id.wotdENExampleTV)
        val wotdLoveFAB = view.findViewById<FloatingActionButton>(R.id.wotdLoveFAB)
        val wotdShareFAB = view.findViewById<FloatingActionButton>(R.id.wotdShareFAB)

        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-M-yyyy"))

        database.reference.child("wordOfTheDay").child(currentDate).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.tag("wordOfTheDay").d(p0.toException())
            }

            override fun onDataChange(p0: DataSnapshot) {
                // Current word of the day.
                val currentWOTD = p0.value as HashMap<*,*>

                // Set values for all widgets.
                wotdDateTV.text = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                wotdTV.text = currentWOTD["word"].toString()
                wotdTransliterationTV.text = currentWOTD["wordTransliteration"].toString()
                wotdTypeTV.text = currentWOTD["wordType"].toString()
                wotdDefinitionTV.text = currentWOTD["wordDefinition"].toString()
                if (currentWOTD.containsKey("exampleSentenceEN")) {
                    wotdENExampleTV.text = currentWOTD["exampleSentenceEN"].toString()
                    wotdBGExampleTV.text = currentWOTD["exampleSentenceBG"].toString()
                }
            }
        })

    }

}