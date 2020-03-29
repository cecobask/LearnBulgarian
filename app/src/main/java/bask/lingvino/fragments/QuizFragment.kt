package bask.lingvino.fragments

import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import bask.lingvino.R
import bask.lingvino.models.QuizQuestion
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.dyhdyh.support.countdowntimer.CountDownTimerSupport
import com.dyhdyh.support.countdowntimer.OnCountDownTimerListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import info.hoang8f.widget.FButton
import timber.log.Timber
import java.time.LocalDate

class QuizFragment : Fragment(), View.OnClickListener {

    private lateinit var topBadgeRL: RelativeLayout
    private lateinit var questionLL: LinearLayout
    private lateinit var answersLL: LinearLayout
    private lateinit var gemTV: TextView
    private lateinit var questions: MutableList<QuizQuestion>
    private lateinit var currentQuestion: QuizQuestion
    private lateinit var questionTV: TextView
    private lateinit var answerA: FButton
    private lateinit var answerB: FButton
    private lateinit var answerC: FButton
    private lateinit var answerD: FButton
    private lateinit var countDownTimer: CountDownTimerSupport
    private lateinit var timeTV: TextView
    private lateinit var quizTopicsRef: DatabaseReference
    private lateinit var dbRef: DatabaseReference
    private lateinit var userStats: DatabaseReference
    private lateinit var fbUser: FirebaseUser
    private lateinit var progressBar: ProgressBar
    private lateinit var quizTopic: String
    private lateinit var sharedPref: SharedPreferences
    private lateinit var spokenLang: String
    private var questionIndex: Int = 0
    private val yearMonth = LocalDate.now().toString().dropLast(3)

    companion object {
        fun newInstance(): QuizFragment {
            return QuizFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.quiz, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        // Initialise widgets.
        topBadgeRL = view.findViewById(R.id.topBadgeRL)
        questionLL = view.findViewById(R.id.questionLL)
        answersLL = view.findViewById(R.id.answersLL)
        gemTV = view.findViewById(R.id.gemTV)
        questionTV = view.findViewById(R.id.questionTV)
        answerA = view.findViewById(R.id.answerA)
        answerB = view.findViewById(R.id.answerB)
        answerC = view.findViewById(R.id.answerC)
        answerD = view.findViewById(R.id.answerD)
        timeTV = view.findViewById(R.id.timeTV)
        progressBar = view.findViewById(R.id.progressBar)

        sharedPref = activity!!.getSharedPreferences("learnBulgarian", 0)
        spokenLang = sharedPref.getString("SPOKEN_LANG_NAME", "English")!!

        dbRef = FirebaseDatabase.getInstance().reference
        fbUser = FirebaseAuth.getInstance().currentUser!!
        quizTopicsRef = dbRef.child("quizGame/topics")
        userStats = dbRef.child("users/${fbUser.uid}/quizStats")

//        val question1 = QuizQuestion("What is the English translation of the Bulgarian word 'патица'?", "duck", "cat", "duck", "chicken", "pig")
//        val question2 = QuizQuestion("What is the English translation of the Bulgarian word 'куче'?", "dog", "dog", "cat", "duck", "pigeon")
//        val question3 = QuizQuestion("What is the English translation of the Bulgarian word 'слон'?", "elephant", "wolf", "dolphin", "pigeon", "elephant")
//        quizTopicsRef.child("animals").push().setValue(question1)
//        quizTopicsRef.child("animals").push().setValue(question2)
//        quizTopicsRef.child("animals").push().setValue(question3)
//
//        val question4 = QuizQuestion("What is the English translation of the Bulgarian word 'яйце'?", "egg", "burger", "egg", "chicken", "omelet")
//        val question5 = QuizQuestion("What is the English translation of the Bulgarian word 'ориз'?", "rice", "rice", "burger", "beans", "ham")
//        val question6 = QuizQuestion("What is the English translation of the Bulgarian word 'шунка'?", "ham", "rice", "omelet", "ham", "egg")
//        quizTopicsRef.child("food").push().setValue(question4)
//        quizTopicsRef.child("food").push().setValue(question5)
//        quizTopicsRef.child("food").push().setValue(question6)

        // Set click listeners to answer buttons.
        answerA.setOnClickListener(this)
        answerB.setOnClickListener(this)
        answerC.setOnClickListener(this)
        answerD.setOnClickListener(this)

        countDownTimer = CountDownTimerSupport(20000, 1000)
        countDownTimer.setOnCountDownTimerListener(object : OnCountDownTimerListener {
            override fun onFinish() {
                timeUp()
            }

            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = "${millisUntilFinished / 1000}s"
                timeTV.text = secondsLeft
            }

        })

        pickQuizTopic()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_quiz, menu)

        super.onCreateOptionsMenu(menu,inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_leaderboard -> {
                // Open QuizLeaderboard.
                val quizLeaderboardFragment = QuizLeaderboard.newInstance()
                fragmentManager!!
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        quizLeaderboardFragment,
                        quizLeaderboardFragment.javaClass.simpleName
                    )
                    .addToBackStack(null)
                    .commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View?) {
        val clickedButton = v as FButton
        if (clickedButton.text == currentQuestion.answer) { // Correct answer.
            updateMonthlyScore("+")
            if (questionIndex != questions.lastIndex) {
                correctDialog()
                return
            }
            victoryDialog() // All answers correct.
        } else { // Wrong answer.
            updateMonthlyScore("-")
            wrongDialog()
        }
    }

    private fun pickQuizTopic() {
        contentLoading()
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.tag("pickTopicQuiz()").e(p0.toException())
                contentLoading(showProgressBar = false)
            }

            override fun onDataChange(p0: DataSnapshot) {
                contentLoading(showProgressBar = false)

                // Get a list of topics.
                val topicsList = mutableListOf<String>()
                p0.child("quizGame/topics").children.forEach { topicsList.add(it.key!!) }

                // Retrieve the user score for current month and display it.
                val scoreSnapshot = p0.child("users/${fbUser.uid}/quizStats/$yearMonth")
                val score = if (scoreSnapshot.exists()) scoreSnapshot.value.toString() else "0"
                gemTV.text = score

                MaterialDialog(context!!).show {
                    title(text = "Pick a quiz topic:")
                    cancelOnTouchOutside(false)
                    onCancel { fragmentManager?.popBackStack() } // Go to previous fragment.
                    listItemsSingleChoice(items = topicsList) { _, _, topic ->
                        quizTopic = "$topic"
                        loadQuestions()
                    }
                }
            }

        })
    }

    private fun loadQuestions() {
        contentLoading()
        quizTopicsRef.child(quizTopic).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.tag("loadQuestions()").d(p0.toException())
                contentLoading(showProgressBar = false)
            }

            override fun onDataChange(p0: DataSnapshot) {
                contentLoading(showProgressBar = false, showContent = true)
                questions = mutableListOf() // Initialise an empty list.
                p0.children.forEach {
                    // Add each object to the list of Questions.
                    val question = it.getValue(QuizQuestion::class.java)!!
                    questions.add(question)
                }

                questions.shuffle() // Randomise questions.
                questionIndex = 0
                currentQuestion = questions[questionIndex] // Initialise to Question with index 0.
                displayQuestion()
            }
        })
    }

    private fun displayQuestion() {
        // Set values of widgets to display current Question object data.
        questionTV.text = currentQuestion.question
        answerA.text = currentQuestion.optionA
        answerB.text = currentQuestion.optionB
        answerC.text = currentQuestion.optionC
        answerD.text = currentQuestion.optionD

        // Reset the timer to 20 seconds and start countdown.
        countDownTimer.reset()
        countDownTimer.start()
    }

    private fun contentLoading(showProgressBar: Boolean = true, showContent: Boolean = false) {
        // Only show content when everything's loaded.
        if (!showProgressBar && showContent) {
            progressBar.visibility = View.GONE
            topBadgeRL.visibility = View.VISIBLE
            questionLL.visibility = View.VISIBLE
            answersLL.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.VISIBLE
            topBadgeRL.visibility = View.GONE
            questionLL.visibility = View.GONE
            answersLL.visibility = View.GONE
        }
    }

    private fun buildDialog(layout: Int, colorTheme: Int, dialogType: String) {
        onPause() // Pause the countdown timer.

        Dialog(context!!).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE) // Hides the title.
            window?.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT) // Transparent background.
            )
            setContentView(layout)
            setCancelable(dialogType != "correct") // Prevent closure of the dialog window.
            show()

            if (dialogType == "correct") {
                val correctTV: TextView = findViewById(R.id.correctTV)
                val nextButton: FButton = findViewById(R.id.nextButton)

                // Display messages in the appropriate language.
                when (spokenLang) {
                    "Bulgarian" -> {
                        correctTV.text = resources.getString(R.string.correct_bg)
                        nextButton.text = resources.getString(R.string.next_bg)
                    }
                    "Spanish" -> {
                        correctTV.text = resources.getString(R.string.correct_es)
                        nextButton.text = resources.getString(R.string.next_es)
                    }
                    "Russian" -> {
                        correctTV.text = resources.getString(R.string.correct_ru)
                        nextButton.text = resources.getString(R.string.next_ru)
                    }
                    else -> {
                        correctTV.text = resources.getString(R.string.correct_en)
                        nextButton.text = resources.getString(R.string.next_en)
                    }
                }

                nextButton.setOnClickListener { // Listen for button clicks.
                        this.dismiss() // Close the dialog window.

                        // Display the next question.
                        questionIndex++
                        currentQuestion = questions[questionIndex]
                        displayQuestion()
                    }
                    nextButton.buttonColor = resources.getColor(colorTheme, null)

                return
            }

            val playAgainButton: FButton = findViewById(R.id.playAgainButton)
            val pickTopicButton: FButton = findViewById(R.id.pickTopicButton)
            var victoryTV: TextView? = null
            var timeUpTV: TextView? = null
            var wrongTV: TextView? = null

            when (dialogType) {
                "victory" -> victoryTV = findViewById(R.id.victoryTV)
                "timeUp" -> timeUpTV = findViewById(R.id.timeUpTV)
                else -> wrongTV = findViewById(R.id.wrongTV)
            }

            // Display messages in the appropriate language.
            when (spokenLang) {
                "Bulgarian" -> {
                    playAgainButton.text = resources.getString(R.string.play_again_bg)
                    pickTopicButton.text = resources.getString(R.string.pick_topic_bg)
                    if (victoryTV != null) victoryTV.text = resources.getString(R.string.victory_bg)
                    if (timeUpTV != null) timeUpTV.text = resources.getString(R.string.time_up_bg)
                    if (wrongTV != null) wrongTV.text = resources.getString(R.string.wrong_bg)
                }
                "Spanish" -> {
                    playAgainButton.text = resources.getString(R.string.play_again_es)
                    pickTopicButton.text = resources.getString(R.string.pick_topic_es)
                    if (victoryTV != null) victoryTV.text = resources.getString(R.string.victory_es)
                    if (timeUpTV != null) timeUpTV.text = resources.getString(R.string.time_up_es)
                    if (wrongTV != null) wrongTV.text = resources.getString(R.string.wrong_es)
                }
                "Russian" -> {
                    playAgainButton.text = resources.getString(R.string.play_again_ru)
                    pickTopicButton.text = resources.getString(R.string.pick_topic_ru)
                    if (victoryTV != null) victoryTV.text = resources.getString(R.string.victory_ru)
                    if (timeUpTV != null) timeUpTV.text = resources.getString(R.string.time_up_ru)
                    if (wrongTV != null) wrongTV.text = resources.getString(R.string.wrong_ru)
                }
                else -> {
                    playAgainButton.text = resources.getString(R.string.play_again_en)
                    pickTopicButton.text = resources.getString(R.string.pick_topic_en)
                    if (victoryTV != null) victoryTV.text = resources.getString(R.string.victory_en)
                    if (timeUpTV != null) timeUpTV.text = resources.getString(R.string.time_up_en)
                    if (wrongTV != null) wrongTV.text = resources.getString(R.string.wrong_en)
                }
            }

            playAgainButton.setOnClickListener { // Listen for button clicks.
                    this.dismiss() // Close the dialog window.
                    loadQuestions() // Start a new game.
                }
            playAgainButton.buttonColor = resources.getColor(colorTheme, null)


            pickTopicButton.setOnClickListener { // Listen for button clicks.
                    this.dismiss() // Close the dialog window.
                    pickQuizTopic() // Provide topic options.
                }
            pickTopicButton.buttonColor = resources.getColor(colorTheme, null)

            setOnCancelListener { // Handle clicks outside the dialog.
                // Disable answer buttons.
                for (button in arrayOf(answerA, answerB, answerC, answerD))
                    button.apply {
                        isEnabled = false
                        buttonColor = resources.getColor(R.color.fbutton_color_silver, null)
                        setTextColor(resources.getColor(R.color.fbutton_color_clouds, null))
                    }
            }
        }
    }

    private fun updateMonthlyScore(operator: String) {
        userStats.child(yearMonth).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.tag("correctDialog()").e(p0.toException())
            }

            override fun onDataChange(p0: DataSnapshot) {
                val currentScore = Integer.valueOf(gemTV.text.toString())
                val updatedScore = with(currentScore) {
                    if (operator == "+") this + 1 // Increment score by 1.
                    else if (operator == "-" && this != 0) this - 1 // Decrement by 1 if score != 0.
                    else this // Do nothing.
                }
                gemTV.text = "$updatedScore"
                userStats.child(yearMonth).setValue(updatedScore)
            }

        })
    }

    private fun correctDialog() {
        buildDialog(R.layout.dialog_correct, R.color.fbutton_color_green_sea, "correct")
    }

    private fun wrongDialog() {
        buildDialog(R.layout.dialog_wrong, R.color.fbutton_color_pomegranate, "wrong")
    }

    private fun victoryDialog() {
        buildDialog(R.layout.dialog_victory, R.color.fbutton_color_nephritis, "victory")
    }

    private fun timeUp() {
        buildDialog(R.layout.dialog_time_up, R.color.fbutton_color_pumpkin, "timeUp")
    }

    override fun onPause() {
        super.onPause()
        countDownTimer.pause()
    }

    override fun onResume() {
        super.onResume()
        // Change toolbar title.
        (activity as? AppCompatActivity)?.supportActionBar?.title =
            resources.getString(R.string.quizTitle)

        countDownTimer.resume()
    }
}