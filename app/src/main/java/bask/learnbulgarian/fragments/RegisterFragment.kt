package bask.learnbulgarian.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import bask.learnbulgarian.R
import bask.learnbulgarian.activities.HomeActivity
import bask.learnbulgarian.models.User
import com.androidadvance.topsnackbar.TSnackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class RegisterFragment : Fragment() {

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
        val usernameET = view.findViewById<EditText>(R.id.usernameET)
        val emailET = view.findViewById<EditText>(R.id.emailET)
        val passwordET = view.findViewById<EditText>(R.id.passwordET)
        val confirmPasswordET = view.findViewById<EditText>(R.id.confirmPasswordET)
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
                passwordET.error = "Passwords must match!"
                confirmPasswordET.error = "Passwords must match!"
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
                registerBtn.isEnabled = (username.isNotEmpty() && email.isNotEmpty() &&
                        password.isNotEmpty() && confirmPassword.isNotEmpty())
            }
        }

        // Attach the TextWatcher to EditTexts.
        usernameET.addTextChangedListener(textWatcher)
        emailET.addTextChangedListener(textWatcher)
        passwordET.addTextChangedListener(textWatcher)
        confirmPasswordET.addTextChangedListener(textWatcher)
    }

    // Register user and open HomeActivity if the signing up is a success.
    private fun signUp(view: View, username: String, email: String, password: String) {
        showMessage(view, "Creating your account...")

        val firebaseAuth: FirebaseAuth? = FirebaseAuth.getInstance()
        firebaseAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Create User object and use it to store data on FirebaseDatabase
                val newUser = User(firebaseAuth.currentUser!!.uid, username, email)
                val usersDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
                usersDatabase.child(newUser.userID).setValue(newUser)

                activity?.finish()
                startActivity(Intent(context, HomeActivity::class.java))
            } else showMessage(view, "Error: ${task.exception?.message}")
        }
    }

    // Use TopSnackBar to show meaningful messages to the user.
    private fun showMessage(view: View, message: String) {
        TSnackbar.make(view, HtmlCompat.fromHtml("<font color=\"#ffffff\">$message</font>", HtmlCompat.FROM_HTML_MODE_LEGACY),
            TSnackbar.LENGTH_LONG).show()
    }
}