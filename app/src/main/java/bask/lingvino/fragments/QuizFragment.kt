package bask.lingvino.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import bask.lingvino.R
import bask.lingvino.models.QuizQuestion
import com.dyhdyh.support.countdowntimer.CountDownTimerSupport
import com.dyhdyh.support.countdowntimer.OnCountDownTimerListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import info.hoang8f.widget.FButton
import timber.log.Timber

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
    private lateinit var questionsRef: DatabaseReference
    private lateinit var fbUser: FirebaseUser
    private lateinit var progressBar: ProgressBar
    private var questionIndex: Int = 0

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

        questionsRef = FirebaseDatabase.getInstance().reference
            .child("quizGame")
            .child("questions")
        fbUser = FirebaseAuth.getInstance().currentUser!!

//        val question1 = QuizQuestion("What is the English translation of the Bulgarian word 'патица'?", "duck", "cat", "duck", "chicken", "pig")
//        val question2 = QuizQuestion("What is the English translation of the Bulgarian word 'куче'?", "dog", "dog", "cat", "duck", "pigeon")
//        val question3 = QuizQuestion("What is the English translation of the Bulgarian word 'слон'?", "elephant", "wolf", "dolphin", "pigeon", "elephant")
//        databaseRef.child("quizGame").child("questions").push().setValue(question1)
//        databaseRef.child("quizGame").child("questions").push().setValue(question2)
//        databaseRef.child("quizGame").child("questions").push().setValue(question3)

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

        loadQuestions()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_quiz, menu)

        super.onCreateOptionsMenu(menu,inflater)
    }

    override fun onClick(v: View?) {
        val clickedButton = v as FButton
        if (clickedButton.text == currentQuestion.answer) { // Correct answer.
            if (questionIndex != questions.lastIndex) {
                correctDialog()
                return
            }
            Timber.tag("seleccc").d("Congratulations! You answered all questions correctly.")
        } else Timber.tag("seleccc").d("Incorrect answer!")
    }

    private fun loadQuestions() {
        questionsRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Timber.tag("loadQuestions()").d(p0.toException())
                progressBar.visibility = View.GONE
            }

            override fun onDataChange(p0: DataSnapshot) {
                showContent()
                questions = mutableListOf() // Initialise an empty list.
                p0.children.forEach {
                    // Add each object to the list of Questions.
                    val question = it.getValue(QuizQuestion::class.java)!!
                    questions.add(question)
                }

                questions.shuffle() // Randomise questions.
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
        gemTV.text = "0"

        // Reset the timer to 20 seconds and start countdown.
        countDownTimer.reset()
        countDownTimer.start()
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        topBadgeRL.visibility = View.VISIBLE
        questionLL.visibility = View.VISIBLE
        answersLL.visibility = View.VISIBLE
    }

    private fun correctDialog() {
        onPause() // Pause the countdown timer.

        Dialog(context!!).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE) // Hides the title.
            window?.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT) // Transparent background.
            )
            setContentView(R.layout.dialog_correct)
            setCancelable(false) // Prevent closure of the dialog window.
            show()

            // Listen for button clicks.
            findViewById<FButton>(R.id.nextButton).also { button ->
                button.setOnClickListener { // Listen for button clicks.
                    this.dismiss() // Close the dialog window.

                    // Display the next question.
                    questionIndex++
                    currentQuestion = questions[questionIndex]
                    displayQuestion()
                }
                button.buttonColor = resources.getColor(R.color.fbutton_color_green_sea, null)
            }
        }
    }

    private fun timeUp() {
        Dialog(context!!).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE) // Hides the title.
            window?.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT) // Transparent background.
            )
            setContentView(R.layout.dialog_time_up)
            setCancelable(false) // Prevent closure of the dialog window.
            show()

            findViewById<FButton>(R.id.playAgainButton).also { button ->
                button.setOnClickListener { // Listen for button clicks.
                    this.dismiss() // Close the dialog window.
                    loadQuestions() // Start a new game.
                }
                button.buttonColor = resources.getColor(R.color.fbutton_color_pomegranate, null)
            }
        }
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