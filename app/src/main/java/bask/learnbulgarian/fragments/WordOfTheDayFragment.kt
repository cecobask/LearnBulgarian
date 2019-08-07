package bask.learnbulgarian.fragments

import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bask.learnbulgarian.R
import bask.learnbulgarian.adapters.WordOfTheDayAdapter
import bask.learnbulgarian.models.WordDefinition
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONArray
import timber.log.Timber
import java.io.InputStream
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class WordOfTheDayFragment: Fragment() {

    private lateinit var translate: Translate
    private lateinit var database: FirebaseDatabase
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: WordOfTheDayAdapter

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
        val wotdTV = view.findViewById<TextView>(R.id.wotdTV)
        val wotdPronounceFAB = view.findViewById<FloatingActionButton>(R.id.wotdPronounceFAB)
        val wotdTransliterationTV = view.findViewById<TextView>(R.id.wotdTransliterationTV)
//        val wotdLoveFAB = view.findViewById<FloatingActionButton>(R.id.wotdLoveFAB)
//        val wotdShareFAB = view.findViewById<FloatingActionButton>(R.id.wotdShareFAB)
        val wotdRV = view.findViewById<RecyclerView>(R.id.wotdRV)

        // Configure RecyclerView.
        linearLayoutManager = LinearLayoutManager(context)
        wotdRV.layoutManager = linearLayoutManager
        wotdRV.addItemDecoration(DividerItemDecoration(wotdRV.context, linearLayoutManager.orientation))

        database.reference.child("wordOfTheDay").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.tag("wordOfTheDay").d(p0.toException())
            }

            override fun onDataChange(p0: DataSnapshot) {
                // Display current date.
                wotdDateTV.text = getCurrentDate()

                // Set word of the day.
                val wotdStr = p0.value.toString()
                val wotdTranslation = translateText(wotdStr)
                wotdTV.text = wotdStr

                // Transliterate word of the day.
                val transliterationStr = "| ${transliterateBulgarian(wotdStr)} |"
                wotdTransliterationTV.text = transliterationStr

                // API call to Google Dictionary to fetch word definitions.
                val jsonResponse = JSONArray(getWordDefinitions(wotdTranslation))
                val wordDefinitions: MutableList<WordDefinition> = ArrayList()
                // Loop through the results.
                for (i in 0 until jsonResponse.length()) {
                    val word = jsonResponse.getJSONObject(i)
                    val wordDefinition = word.getJSONObject("meaning")
                    // Loop through all the word definitions and extract needed values
                    for (key in wordDefinition.keys()) {
                        val currentElement = wordDefinition.getJSONArray(key).getJSONObject(0)
                        val definition = currentElement.getString("definition")
                        var exampleEN: String?
                        var exampleBG: String?
                        try {
                            exampleEN = currentElement.getString("example")
                            exampleBG = translateText(exampleEN, "en", "bg")
                        } catch (e: Exception) {
                            Timber.tag("null_example").d(e)
                            exampleEN = null
                            exampleBG = null
                        }

                        // Create an object and add it to ArrayList.
                        wordDefinitions.add(
                            WordDefinition(wotdTranslation, definition, transliterationStr, key, exampleEN, exampleBG)
                        )
                    }

                }

                // Configure the RecyclerView to populate data from wordDefinitions ArrayList.
                adapter = WordOfTheDayAdapter(wordDefinitions as ArrayList<WordDefinition>)
                wotdRV.adapter = adapter
            }

        })

    }

    private fun translateText(text: String, sourceLang: String = "bg", targetLang: String = "en"): String {
        return translate.translate(
            text,
            Translate.TranslateOption.sourceLanguage(sourceLang),
            Translate.TranslateOption.targetLanguage(targetLang)
        ).translatedText
    }

    private fun transliterateBulgarian(bg: String): String {

        var en = bg.replace("а".toRegex(), "a")
        en = en.replace("б".toRegex(), "b")
        en = en.replace("в".toRegex(), "v")
        en = en.replace("г".toRegex(), "g")
        en = en.replace("д".toRegex(), "d")
        en = en.replace("е".toRegex(), "e")
        en = en.replace("ж".toRegex(), "zh")
        en = en.replace("з".toRegex(), "z")
        en = en.replace("и".toRegex(), "i")
        en = en.replace("й".toRegex(), "y")
        en = en.replace("к".toRegex(), "k")
        en = en.replace("л".toRegex(), "l")
        en = en.replace("м".toRegex(), "m")
        en = en.replace("н".toRegex(), "n")
        en = en.replace("о".toRegex(), "o")
        en = en.replace("п".toRegex(), "p")
        en = en.replace("р".toRegex(), "r")
        en = en.replace("с".toRegex(), "s")
        en = en.replace("т".toRegex(), "t")
        en = en.replace("у".toRegex(), "u")
        en = en.replace("ф".toRegex(), "f")
        en = en.replace("х".toRegex(), "h")
        en = en.replace("ц".toRegex(), "ts")
        en = en.replace("ч".toRegex(), "ch")
        en = en.replace("ш".toRegex(), "sh")
        en = en.replace("щ".toRegex(), "sht")
        en = en.replace("ъ".toRegex(), "а'")
        en = en.replace("ь".toRegex(), "y")
        en = en.replace("ю".toRegex(), "yu")
        en = en.replace("я".toRegex(), "ya")

        en = en.replace("А".toRegex(), "A")
        en = en.replace("Б".toRegex(), "B")
        en = en.replace("В".toRegex(), "V")
        en = en.replace("Г".toRegex(), "G")
        en = en.replace("Д".toRegex(), "D")
        en = en.replace("Е".toRegex(), "E")
        en = en.replace("Ж".toRegex(), "ZH")
        en = en.replace("З".toRegex(), "Z")
        en = en.replace("И".toRegex(), "I")
        en = en.replace("Л".toRegex(), "Y")
        en = en.replace("К".toRegex(), "K")
        en = en.replace("Л".toRegex(), "L")
        en = en.replace("М".toRegex(), "M")
        en = en.replace("Н".toRegex(), "N")
        en = en.replace("О".toRegex(), "O")
        en = en.replace("П".toRegex(), "P")
        en = en.replace("Р".toRegex(), "R")
        en = en.replace("С".toRegex(), "S")
        en = en.replace("Т".toRegex(), "T")
        en = en.replace("У".toRegex(), "U")
        en = en.replace("Ф".toRegex(), "F")
        en = en.replace("Х".toRegex(), "H")
        en = en.replace("Ц".toRegex(), "TS")
        en = en.replace("Ч".toRegex(), "CH")
        en = en.replace("Ш".toRegex(), "SH")
        en = en.replace("Щ".toRegex(), "SHT")
        en = en.replace("Ъ".toRegex(), "A'")
        en = en.replace("Ь".toRegex(), "Y")
        en = en.replace("Ю".toRegex(), "YU")
        en = en.replace("Я".toRegex(), "YA")

        return en
    }

    private fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        return LocalDate.now().format(formatter)
    }

    private fun getWordDefinitions(word: String): String? {
        var definitions: String? = null
        val (_, _, result) = Fuel.get("https://googledictionaryapi.eu-gb.mybluemix.net/?define=$word")
            .responseString()

        when (result) {
            is Result.Failure -> Timber.tag("fuel").d(result.getException())
            is Result.Success -> definitions = result.get()
        }

        return definitions
    }

}