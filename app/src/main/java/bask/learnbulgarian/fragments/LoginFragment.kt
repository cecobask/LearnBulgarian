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
import bask.learnbulgarian.main.App
import com.androidadvance.topsnackbar.TSnackbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.login.*


class LoginFragment : Fragment() {

    private val RC_SIGN_IN: Int = 1

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = App.getFirebaseAuth()
        googleSignInClient = App.getGoogleClient(context!!)

        // Instantiate widgets.
        val emailET = view.findViewById<EditText>(R.id.emailET)
        val passwordET = view.findViewById<EditText>(R.id.passwordET)
        val loginBtn = view.findViewById<Button>(R.id.loginBtn)
        val googleBtn = view.findViewById<SignInButton>(R.id.googleBtn)

        // Call signIn() with provided params.
        loginBtn.setOnClickListener {
            val email: String = emailET.text.toString().trim()
            val password: String = passwordET.text.toString().trim()
            signIn(it, email, password)
        }

        googleBtn.setOnClickListener {
            val signInIntent: Intent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // Make sure there no empty fields. After all fields are filled out, loginBtn gets enabled.
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email: String = emailET.text.toString().trim()
                val password: String = passwordET.text.toString().trim()
                loginBtn.isEnabled = (email.isNotEmpty() && password.isNotEmpty())
            }
        }

        // Attach the TextWatcher to EditTexts.
        emailET.addTextChangedListener(textWatcher)
        passwordET.addTextChangedListener(textWatcher)
    }

    // Sign in with Firebase and open HomeActivity.
    private fun signIn(view: View, email: String, password: String) {
        showMessage(view, "Authenticating...")

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                showMessage(googleBtn, "Error: $e")
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                startActivity(Intent(context, HomeActivity::class.java))
            } else {
                showMessage(googleBtn, "Error: $it.exception.toString()")
            }
        }
    }
}