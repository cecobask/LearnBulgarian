package bask.lingvino.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bask.lingvino.R
import bask.lingvino.adapters.LeaderboardAdapter
import bask.lingvino.models.LeaderboardUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.kizitonwose.calendarview.utils.yearMonth
import org.threeten.bp.LocalDate
import timber.log.Timber

class QuizLeaderboard : Fragment() {

    private lateinit var usersRef: DatabaseReference
    private lateinit var fbUser: FirebaseUser
    private lateinit var leaderboardRV: RecyclerView
    private val date = LocalDate.now()

    companion object {
        fun newInstance(): QuizLeaderboard {
            return QuizLeaderboard()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.quiz_leaderboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        leaderboardRV = view.findViewById<RecyclerView>(R.id.leaderboardRV).apply {
            val lm = LinearLayoutManager(context)
            layoutManager = lm
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(this.context, lm.orientation))
        }

        usersRef = FirebaseDatabase.getInstance().reference.child("users")
        fbUser = FirebaseAuth.getInstance().currentUser!!

        loadUsersData()
    }

    private fun loadUsersData() {
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.tag("loadUsersData()").e(p0.toException())
            }

            override fun onDataChange(p0: DataSnapshot) {
                val leaderboardUsers = mutableListOf<LeaderboardUser>()
                p0.children.forEach {
                    LeaderboardUser().apply {
                        userID = it.key!!
                        username = "${it.child("username").value}"
                        currentMonthScore = with(currentMonthScore) {
                            it.child("quizStats/${date.yearMonth}").run {
                                if (this.exists()) (this.value as Long).toInt()
                                else this@with
                            }
                        }
                        currentYearScore = with(currentYearScore) {
                            it.child("quizStats").run {
                                var currentYearScore = this@with // Default is 0.
                                if (this.exists()) { // If the user has played a quiz game before.
                                    this.children.forEach { dateSnapshot ->
                                        // Sum the monthly scores of the current year.
                                        if (dateSnapshot.key!!.startsWith("${date.year}"))
                                            currentYearScore += ((dateSnapshot).value as Long).toInt()
                                    }
                                }
                                currentYearScore // Returns 0 or sum of monthly scores.
                            }
                        }
                        leaderboardUsers.add(this) // Add the user to a list.
                    }
                }
                leaderboardRV.adapter = LeaderboardAdapter(
                    leaderboardUsers as ArrayList<LeaderboardUser>
                )
                Timber.tag("seleccc").d(leaderboardUsers.toString())
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Change toolbar title.
        (activity as? AppCompatActivity)?.supportActionBar?.title =
            resources.getString(R.string.quizLeaderboardTitle)
    }
}