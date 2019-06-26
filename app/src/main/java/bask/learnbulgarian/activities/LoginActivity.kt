package bask.learnbulgarian.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import bask.learnbulgarian.R
import com.androidadvance.topsnackbar.TSnackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val usernameET = findViewById<EditText>(R.id.usernameET)
        val passwordET = findViewById<EditText>(R.id.passwordET)
        val loginBtn = findViewById<Button>(R.id.loginBtn)


        loginBtn.setOnClickListener { view ->
            val email: String = usernameET.text.toString().trim()
            val password: String = passwordET.text.toString().trim()
            signIn(view, email, password)
        }

        val textWatcher: TextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email: String = usernameET.text.toString().trim()
                val password: String = passwordET.text.toString().trim()
                loginBtn.isEnabled = (email.isNotEmpty() && password.isNotEmpty())
            }
        }

        usernameET.addTextChangedListener(textWatcher)
        passwordET.addTextChangedListener(textWatcher)
    }

    private fun signIn(view: View, email: String, password: String) {
        showMessage(view, "Authenticating...")

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                finish()
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("username", firebaseAuth.currentUser?.email)
                startActivity(intent)
            } else showMessage(view, "Error: ${task.exception?.message}")
        }

    }

    private fun showMessage(view: View, message: String) {
        TSnackbar.make(
            view, HtmlCompat.fromHtml("<font color=\"#ffffff\">$message</font>", HtmlCompat.FROM_HTML_MODE_LEGACY),
            TSnackbar.LENGTH_LONG
        ).show()
    }
}