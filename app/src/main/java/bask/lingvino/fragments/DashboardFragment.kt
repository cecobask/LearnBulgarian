package bask.lingvino.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import bask.lingvino.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardFragment : Fragment(), View.OnClickListener {

    private lateinit var wotdCard: CardView
    private lateinit var translatorCard: CardView
    private lateinit var quizCard: CardView
    private lateinit var languageOptionsCard: CardView
    private lateinit var progressBar: ProgressBar
    private lateinit var welcomeTV: TextView
    private lateinit var wotdInfo: TextView
    private lateinit var translatorInfo: TextView
    private lateinit var quizInfo: TextView
    private lateinit var languageOptionsInfo: TextView
    private lateinit var navView: NavigationView

    companion object {
        fun newInstance(): DashboardFragment {
            return DashboardFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialise widgets.
        wotdCard = view.findViewById(R.id.wotdCard)
        translatorCard = view.findViewById(R.id.translatorCard)
        quizCard = view.findViewById(R.id.quizCard)
        languageOptionsCard = view.findViewById(R.id.languageOptionsCard)
        navView = activity!!.findViewById(R.id.navView)
        progressBar = view.findViewById(R.id.progressBar)
        welcomeTV = view.findViewById(R.id.welcomeTV)
        wotdInfo = view.findViewById(R.id.wotdInfo)
        translatorInfo = view.findViewById(R.id.translatorInfo)
        quizInfo = view.findViewById(R.id.quizInfo)
        languageOptionsInfo = view.findViewById(R.id.languageOptionsInfo)

        val sharedPref = activity?.getSharedPreferences("learnBulgarian", 0)
        val spokenLanguage = sharedPref?.getString("SPOKEN_LANG_NAME", "Bulgarian")

        displayWelcomeMessage(FirebaseAuth.getInstance().currentUser!!.uid, spokenLanguage)
        displayCardsInfo(spokenLanguage)

        // Set click listeners to the CardView items.
        wotdCard.setOnClickListener(this)
        translatorCard.setOnClickListener(this)
        quizCard.setOnClickListener(this)
        languageOptionsCard.setOnClickListener(this)
    }

    private fun displayCardsInfo(spokenLanguage: String?) {
        when(spokenLanguage) {
            "Bulgarian" -> {
                wotdInfo.text = resources.getString(R.string.wotd_info_bg)
                translatorInfo.text = resources.getString(R.string.translator_info_bg)
                quizInfo.text = resources.getString(R.string.quiz_info_bg)
                languageOptionsInfo.text = resources.getString(R.string.lang_options_info_bg)
            }
            "Spanish" -> {
                wotdInfo.text = resources.getString(R.string.wotd_info_es)
                translatorInfo.text = resources.getString(R.string.translator_info_es)
                quizInfo.text = resources.getString(R.string.quiz_info_es)
                languageOptionsInfo.text = resources.getString(R.string.lang_options_info_es)
            }
            "Russian" -> {
                wotdInfo.text = resources.getString(R.string.wotd_info_ru)
                translatorInfo.text = resources.getString(R.string.translator_info_ru)
                quizInfo.text = resources.getString(R.string.quiz_info_ru)
                languageOptionsInfo.text = resources.getString(R.string.lang_options_info_ru)
            }
            else -> {
                wotdInfo.text = resources.getString(R.string.wotd_info_en)
                translatorInfo.text = resources.getString(R.string.translator_info_en)
                quizInfo.text = resources.getString(R.string.quiz_info_en)
                languageOptionsInfo.text = resources.getString(R.string.lang_options_info_en)
            }
        }
    }

    private fun displayWelcomeMessage(userID: String, spokenLanguage: String?) {
        progressBar.visibility = View.VISIBLE
        FirebaseDatabase.getInstance().reference
            .child("users/$userID").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    progressBar.visibility = View.GONE
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val username = p0.child("username").value as String
                    welcomeTV.text = when(spokenLanguage) {
                        "Bulgarian" -> resources.getString(R.string.welcome_message_bg, username)
                        "Spanish" -> resources.getString(R.string.welcome_message_es, username)
                        "Russian" -> resources.getString(R.string.welcome_message_ru, username)
                        else -> resources.getString(R.string.welcome_message_en, username)
                    }
                    progressBar.visibility = View.GONE
                }
            })
    }

    override fun onResume() {
        super.onResume()
        // Change toolbar title.
        (activity as? AppCompatActivity)?.supportActionBar?.title =
            resources.getString(R.string.dashboard_title)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.wotdCard -> navView.menu.performIdentifierAction(R.id.navItemWOTD, 0)
            R.id.translatorCard -> navView.menu.performIdentifierAction(R.id.navItemTranslate, 0)
            R.id.quizCard -> navView.menu.performIdentifierAction(R.id.navItemQuiz, 0)
            R.id.languageOptionsCard -> navView.menu.performIdentifierAction(R.id.navItemLang, 0)
        }
    }
}