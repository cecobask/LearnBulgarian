package bask.learnbulgarian.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import bask.learnbulgarian.R
import bask.learnbulgarian.models.WordOfTheDay
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class WordOfTheDayFragment: Fragment() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var mediaPlayer: MediaPlayer // For playing word pronunciation.
    private lateinit var firebaseAuth: FirebaseAuth

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

        // Change toolbar title.
        (activity as? AppCompatActivity)?.supportActionBar?.title = resources.getString(R.string.wotd)

        val wotdDateTV = view.findViewById<TextView>(R.id.wotdDateTV)
        val wotdTV = view.findViewById<TextView>(R.id.wotdTV)
        val wotdPronounceFAB = view.findViewById<FloatingActionButton>(R.id.wotdPronounceFAB)
        val wotdTransliterationTV = view.findViewById<TextView>(R.id.wotdTransliterationTV)
        val wotdTypeTV = view.findViewById<TextView>(R.id.wotdTypeTV)
        val wotdDefinitionTV = view.findViewById<TextView>(R.id.wotdDefinitionTV)
        val wotdBGExampleTV = view.findViewById<TextView>(R.id.wotdBGExampleTV)
        val wotdENExampleTV = view.findViewById<TextView>(R.id.wotdENExampleTV)
        val wotdLoveFAB = view.findViewById<FloatingActionButton>(R.id.wotdLoveFAB)
        val wotdListFAB = view.findViewById<FloatingActionButton>(R.id.wotdListFAB)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("d-M-yyyy"))

        // Show progress bar until Firebase retrieves data from the database.
        progressBar.visibility = View.VISIBLE

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        // Query Firebase Database for current word of the day.
        databaseRef = FirebaseDatabase.getInstance().reference
        databaseRef.child("wordOfTheDay").child(currentDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.tag("wordOfTheDay").d(p0.toException())
            }

            override fun onDataChange(p0: DataSnapshot) {
                // Current word of the day.
                val currentWOTD = p0.getValue(WordOfTheDay::class.java)

                // Set values for all widgets.
                wotdDateTV.text = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                wotdTV.text = currentWOTD?.word
                wotdTransliterationTV.text = currentWOTD?.wordTransliteration
                wotdTypeTV.text = currentWOTD?.wordType
                wotdDefinitionTV.text = currentWOTD?.wordDefinition
                wotdPronounceFAB.visibility = View.VISIBLE
                if (currentWOTD?.exampleSentenceEN != "") {
                    wotdENExampleTV.text = currentWOTD?.exampleSentenceEN
                    wotdBGExampleTV.text = currentWOTD?.exampleSentenceBG
                }

                // Create MediaPlayer instance that uses URL from Google Cloud Storage.
                // to play word pronunciation.
                mediaPlayer = MediaPlayer.create(context, Uri.parse(currentWOTD?.pronunciationURL))
                mediaPlayer
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                )

                // Play word pronunciation on click of pronounce FAB.
                wotdPronounceFAB.setOnClickListener { mediaPlayer.start() }

                // Reference to current user's collection of favourite words.
                val favWordRef = databaseRef.child("users")
                    .child(currentUser!!.uid)
                    .child("favWords")

                // Query user's favourite words collection
                // to check if today's word of the day exists there.
                favWordRef.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Timber.tag("favWord").d(p0.toException())
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        // Variable used to determine what action wotdLoveFAB will do.
                        var loveFABAction: String?

                        // Hide progress bar.
                        progressBar.visibility = View.GONE

                        // Show buttons.
                        wotdLoveFAB.visibility = View.VISIBLE
                        wotdListFAB.visibility = View.VISIBLE

                        if (p0.hasChild(currentDate)) {
                            // Word exists in user's collection.
                            // Set action to remove the word from the db if the button is clicked.
                            // Set colour to red.
                            loveFABAction = "hate"
                            wotdLoveFAB.backgroundTintList = ColorStateList.valueOf(Color.RED)
                        } else {
                            // Word doesn't exists in user's collection.
                            // Set action to add the word to the db if the button is clicked.
                            // Set colour to default.
                            loveFABAction = "love"
                            wotdLoveFAB.backgroundTintList =
                                ColorStateList.valueOf(
                                    ResourcesCompat
                                        .getColor(resources, R.color.colorPrimary, null)
                                )
                        }

                        // Determine what action to take on click of the button (add/remove from db).
                        wotdLoveFAB.setOnClickListener {
                            if (loveFABAction == "love") {
                                loveFABAction = "hate"
                                // Add the word to the db.
                                // Set button colour to red.
                                favWordRef.child(currentDate).setValue(currentWOTD)
                                wotdLoveFAB.backgroundTintList = ColorStateList.valueOf(Color.RED)
                            } else {
                                loveFABAction = "love"
                                // Remove the word from the db.
                                // Reset button colour.
                                favWordRef.child(currentDate).removeValue()
                                wotdLoveFAB.backgroundTintList =
                                    ColorStateList.valueOf(
                                        ResourcesCompat
                                            .getColor(resources, R.color.colorPrimary, null)
                                    )
                            }
                        }

                        // Open user's favourite words collection in a new Fragment.
                        wotdListFAB.setOnClickListener {
                            fragmentManager!!
                                .beginTransaction()
                                .replace(
                                    R.id.fragmentContainer,
                                    WordOfTheDayFavouritesFragment.newInstance()
                                )
                                .addToBackStack(null)
                                .commit()
                        }

                        wotdListFAB.isEnabled = p0.exists()
                    }

                })
            }
        })

    }

}