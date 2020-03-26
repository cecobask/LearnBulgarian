package bask.lingvino.models

data class LeaderboardUser(
    var userID: String = "",
    var username: String = "",
    var currentMonthScore: Int = 0,
    var currentYearScore: Int = 0
)