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

        displayWelcomeMessage(FirebaseAuth.getInstance().currentUser!!.uid)

        // Set click listeners to the CardView items.
        wotdCard.setOnClickListener(this)
        translatorCard.setOnClickListener(this)
        quizCard.setOnClickListener(this)
        languageOptionsCard.setOnClickListener(this)
    }

    private fun displayWelcomeMessage(userID: String) {
        progressBar.visibility = View.VISIBLE
        FirebaseDatabase.getInstance().reference
            .child("users/$userID").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    progressBar.visibility = View.GONE
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val username = p0.child("username").value as String
                    welcomeTV.text = resources.getString(R.string.welcome_message, username)
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