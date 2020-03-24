package bask.lingvino.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bask.lingvino.R
import bask.lingvino.models.LeaderboardUser
import kotlinx.android.synthetic.main.leaderboard_item.view.*

class LeaderboardAdapter(var leaderboardUsers: ArrayList<LeaderboardUser>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardHolder =
        LeaderboardHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.leaderboard_item, parent, false)
        )

    override fun getItemCount(): Int = leaderboardUsers.size

    override fun onBindViewHolder(holder: LeaderboardHolder, position: Int) {
        holder.bindItems(leaderboardUsers[holder.adapterPosition])
    }

    inner class LeaderboardHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val view: View = v

        fun bindItems(leaderboardUser: LeaderboardUser) {
            val email = leaderboardUser.email
            val currentMonthScore = leaderboardUser.currentMonthScore
            val currentYearScore = leaderboardUser.currentYearScore

            @SuppressLint("SetTextI18n")
            view.position.text = "${adapterPosition + 1}"
            view.email.text = email
            view.currentMonthScore.text = "$currentMonthScore"
            view.currentYearScore.text = "$currentYearScore"
        }
    }
}