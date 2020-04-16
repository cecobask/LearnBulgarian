package bask.lingvino.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import bask.lingvino.R
import bask.lingvino.activities.HomeActivity
import bask.lingvino.activities.LanguageSelectionActivity
import bask.lingvino.models.User
import com.androidadvance.topsnackbar.TSnackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.register.*


class RegisterFragment : Fragment(), TextView.OnEditorActionListener {

    private lateinit var usernameET: TextInputEditText
    private lateinit var usernameLayout: TextInputLayout
    private lateinit var emailET: TextInputEditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordET: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordET: TextInputEditText
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var scrollView: ScrollView
    private lateinit var sharedPref: SharedPreferences

    companion object {
        fun newInstance(): RegisterFragment {
            return RegisterFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.register, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate widgets.
        usernameET = view.findViewById(R.id.usernameET)
        usernameLayout = view.findViewById(R.id.usernameLayout)
        emailET = view.findViewById(R.id.emailET)
        emailLayout = view.findViewById(R.id.emailLayout)
        passwordET = view.findViewById(R.id.passwordET)
        passwordLayout = view.findViewById(R.id.passwordLayout)
        confirmPasswordET = view.findViewById(R.id.confirmPasswordET)
        confirmPasswordLayout = view.findViewById(R.id.confirmPasswordLayout)
        scrollView = view.findViewById(R.id.scrollViewRegister)
        sharedPref = activity!!.getSharedPreferences("learnBulgarian", 0)
        val registerBtn = view.findViewById<Button>(R.id.registerBtn)

        // Get user input from fields and attempt to call signUp() with provided params.
        registerBtn.setOnClickListener {
            val username: String = usernameET.text.toString().trim()
            val email: String = emailET.text.toString().trim()
            val password: String = passwordET.text.toString().trim()
            val confirmPassword: String = confirmPasswordET.text.toString().trim()

            // Check if passwords match.
            if (password == confirmPassword) {
                signUp(view, username, email, password)
            } else {
                passwordLayout.error = "Passwords must match!"
                confirmPasswordLayout.error = "Passwords must match!"
                emailLayout.error = null
            }
        }

        // Make sure there are no empty fields. After all fields are filled out, loginBtn gets enabled.
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val username: String = usernameET.text.toString().trim()
                val email: String = emailET.text.toString().trim()
                val password: String = passwordET.text.toString().trim()
                val confirmPassword: String = confirmPasswordET.text.toString().trim()
                registerBtn.isEnabled = (username.isNotBlank() && email.isNotBlank() &&
                        password.isNotBlank() && confirmPassword.isNotBlank())
            }
        }

        // Attach the TextWatcher to EditTexts.
        usernameET.addTextChangedListener(textWatcher)
        emailET.addTextChangedListener(textWatcher)
        passwordET.addTextChangedListener(textWatcher)
        confirmPasswordET.addTextChangedListener(textWatcher)

        // Listen for action clicks.
        usernameET.setOnEditorActionListener(this)
        emailET.setOnEditorActionListener(this)
        passwordET.setOnEditorActionListener(this)
        confirmPasswordET.setOnEditorActionListener(this)
    }

    // Register user and open HomeActivity if the signing up is a success.
    @Throws(FirebaseAuthException::class)
    private fun signUp(view: View, username: String, email: String, password: String) {
        showMessage(view, "Creating your account...")

        val firebaseAuth: FirebaseAuth? = FirebaseAuth.getInstance()
        firebaseAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Create User object and use it to store data on FirebaseDatabase
                val newUser = User(firebaseAuth.currentUser!!.uid, username, email)
                val usersDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
                usersDatabase.child(newUser.userID).setValue(newUser)

                // Check if user has picked spoken and target language for the app.
                if (!hasUserPickedLanguages()) {
                    finishActivityAndStartLanguageSelection()
                } else {
                    finishActivityAndStartHome()
                }
            } else {
                // Handle common exception scenarios.
                when (val errorMessage = task.exception?.localizedMessage) {
                    "The email address is badly formatted." -> {
                        emailLayout.error = errorMessage
                        emailLayout.requestFocus()
                        passwordLayout.error = null
                        confirmPasswordLayout.error = null
                    }
                    "The email address is already in use by another account." -> {
                        emailLayout.error = errorMessage
                        emailLayout.requestFocus()
                        passwordLayout.error = null
                        confirmPasswordLayout.error = null
                    }
                    "The given password is invalid. [ Password should be at least 6 characters ]" -> {
                        passwordLayout.error = errorMessage
                        confirmPasswordLayout.error = errorMessage
                        passwordLayout.requestFocus()
                        emailLayout.error = null
                    }
                    else -> {
                        showMessage(view, errorMessage!!)
                        passwordLayout.error = null
                        confirmPasswordLayout.error = null
                    }

                }
            }
        }
    }

    // Use TopSnackBar to show meaningful messages to the user.
    private fun showMessage(view: View, message: String) {
        TSnackbar.make(view, HtmlCompat.fromHtml("<font color=\"#ffffff\">$message</font>", HtmlCompat.FROM_HTML_MODE_LEGACY),
            TSnackbar.LENGTH_LONG).show()
    }

    private fun hasUserPickedLanguages(): Boolean {
        return sharedPref.contains("SPOKEN_LANG_NAME")
    }

    private fun finishActivityAndStartHome() {
        activity?.finishAffinity()
        startActivity(Intent(context, HomeActivity::class.java))
    }

    private fun finishActivityAndStartLanguageSelection() {
        activity?.finishAffinity()
        startActivity(Intent(context, LanguageSelectionActivity::class.java))
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        return when (v?.id) {
            R.id.usernameET -> {
                emailET.requestFocus()
                true
            }
            R.id.emailET -> {
                passwordET.requestFocus()
                true
            }
            R.id.passwordET -> {
                scrollView.post {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    confirmPasswordET.requestFocus()}
                true
            }
            R.id.confirmPasswordET -> {
                if (v.text.isNotEmpty()) registerBtn.performClick()
                true
            }
            else -> {
                false
            }
        }
    }
}