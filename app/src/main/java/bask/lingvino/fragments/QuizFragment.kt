package bask.lingvino.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import bask.lingvino.R
import bask.lingvino.models.QuizQuestion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import info.hoang8f.widget.FButton
import timber.log.Timber

class QuizFragment: Fragment() {

    private lateinit var gemTV: TextView
    private lateinit var questions: MutableList<QuizQuestion>
    private lateinit var currentQuestion: QuizQuestion
    private var questionIndex: Int = 0
    private lateinit var questionTV: TextView
    private lateinit var answerA: FButton
    private lateinit var answerB: FButton
    private lateinit var answerC: FButton
    private lateinit var answerD: FButton
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var timeTV: TextView
    private lateinit var questionsRef: DatabaseReference
    private lateinit var fbUser: FirebaseUser

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
        gemTV = view.findViewById(R.id.gemTV)
        questionTV = view.findViewById(R.id.questionTV)
        answerA = view.findViewById(R.id.answerA)
        answerB = view.findViewById(R.id.answerB)
        answerC = view.findViewById(R.id.answerC)
        answerD = view.findViewById(R.id.answerD)
        timeTV = view.findViewById(R.id.timeTV)

        questionsRef = FirebaseDatabase.getInstance().reference
            .child("quizGame")
            .child("questions")
        fbUser = FirebaseAuth.getInstance().currentUser!!
        questions = mutableListOf()

//        val question1 = QuizQuestion("What is the English translation of the Bulgarian word 'патица'?", "duck", "cat", "duck", "chicken", "pig")
//        val question2 = QuizQuestion("What is the English translation of the Bulgarian word 'куче'?", "dog", "dog", "cat", "duck", "pigeon")
//        val question3 = QuizQuestion("What is the English translation of the Bulgarian word 'слон'?", "elephant", "wolf", "dolphin", "pigeon", "elephant")
//        databaseRef.child("quizGame").child("questions").push().setValue(question1)
//        databaseRef.child("quizGame").child("questions").push().setValue(question2)
//        databaseRef.child("quizGame").child("questions").push().setValue(question3)

        loadQuestions()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_quiz, menu)

        super.onCreateOptionsMenu(menu,inflater)
    }

    override fun onResume() {
        super.onResume()
        // Change toolbar title.
        (activity as? AppCompatActivity)?.supportActionBar?.title =
            resources.getString(R.string.quizTitle)
    }

    private fun loadQuestions() {
        questionsRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Timber.tag("loadQuestions()").d(p0.toException())
            }

            override fun onDataChange(p0: DataSnapshot) {
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
    }
}