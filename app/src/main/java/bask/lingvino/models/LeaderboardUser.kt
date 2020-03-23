package bask.lingvino.models

data class LeaderboardUser(
    var userID: String = "",
    var email: String = "",
    var currentMonthScore: Int = 0,
    var yearlyScore: Int = 0
)