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
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import java.io.InputStream

class WordOfTheDayFragment: Fragment() {

    private lateinit var translate: Translate
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

        // Access Google credentials and feed them into the Cloud Translation API
        val inputStream: InputStream = resources.openRawResource(R.raw.credentials)
        val credentials: GoogleCredentials = GoogleCredentials.fromStream(inputStream)
        val translateOptions: TranslateOptions = TranslateOptions.newBuilder().setCredentials(credentials).build()
        translate = translateOptions.service


        database = FirebaseDatabase.getInstance()

        val wotdDateTV = view.findViewById<TextView>(R.id.wotdDateTV)
        val wotdWordTV = view.findViewById<TextView>(R.id.wotdWordTV)
        val wotdPronounceFAB = view.findViewById<FloatingActionButton>(R.id.wotdPronounceFAB)
        val wotdTransliterationTV = view.findViewById<TextView>(R.id.wotdTransliterationTV)
        val wotdTypeTV = view.findViewById<TextView>(R.id.wotdTypeTV)
        val wotdDefinitionTV = view.findViewById<TextView>(R.id.wotdDefinitionTV)
        val wotdBGExampleTV = view.findViewById<TextView>(R.id.wotdBGExampleTV)
        val wotdENExampleTV = view.findViewById<TextView>(R.id.wotdENExampleTV)
        val wotdLoveFAB = view.findViewById<FloatingActionButton>(R.id.wotdLoveFAB)
        val wotdShareFAB = view.findViewById<FloatingActionButton>(R.id.wotdShareFAB)

        val wotd = database.reference.child("wordOfTheDay").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.tag("wordOfTheDay").d(p0.toException())
            }

            override fun onDataChange(p0: DataSnapshot) {
                // Set word of the day.
                wotdWordTV.text = p0.value.toString()
                // Get the value of the English example sentence and translate it to Bulgarian.
                val exampleTextBg = translateText(wotdENExampleTV.text.toString(), "en", "bg")
                // Display the Bulgarian translation.
                wotdBGExampleTV.text = exampleTextBg
            }

        })

    }

    private fun translateText(text: String, sourceLang: String, targetLang: String): String {
        return translate.translate(
            text,
            Translate.TranslateOption.sourceLanguage(sourceLang),
            Translate.TranslateOption.targetLanguage(targetLang)
        ).translatedText
    }

}