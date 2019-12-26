package bask.learnbulgarian.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import bask.learnbulgarian.R
import bask.learnbulgarian.activities.HomeActivity
import bask.learnbulgarian.main.App
import com.androidadvance.topsnackbar.TSnackbar
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider


class LoginFragment : Fragment() {

    private val signInReqCodeGoogle: Int = 1
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var googleBtn: Button
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get instances of auth providers.
        firebaseAuth = FirebaseAuth.getInstance()
        googleSignInClient = App.getGoogleClient(context!!, getString(R.string.default_web_client_id))
        callbackManager = CallbackManager.Factory.create()

        // Instantiate widgets.
        val emailET = view.findViewById<TextInputEditText>(R.id.emailET)
        val passwordET = view.findViewById<TextInputEditText>(R.id.passwordET)
        val loginBtn = view.findViewById<Button>(R.id.loginBtn)
        googleBtn = view.findViewById(R.id.googleBtnCustom)
        val facebookBtn = view.findViewById<LoginButton>(R.id.facebookBtn)
        val facebookBtnCustom = view.findViewById<Button>(R.id.facebookBtnCustom)
        emailLayout = view.findViewById(R.id.emailLayout)
        passwordLayout = view.findViewById(R.id.passwordLayout)

        // Workaround for implementing custom social media button
        facebookBtnCustom.setOnClickListener { facebookBtn.performClick() }

        // FirebaseAuth using signInWithEmail() with provided params.
        loginBtn.setOnClickListener {
            val email: String = emailET.text.toString().trim()
            val password: String = passwordET.text.toString().trim()
            signInWithEmail(it, email, password)
        }

        // Auth with Google.
        googleBtn.setOnClickListener {
            val signInIntent: Intent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, signInReqCodeGoogle)
        }

        // Auth with Facebook.
        facebookBtn.fragment = this
        facebookBtn.setOnClickListener { signInWithFacebook(it as LoginButton) }

        // Make sure there are no empty fields. After all fields are filled out, loginBtn gets enabled.
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email: String = emailET.text.toString().trim()
                val password: String = passwordET.text.toString().trim()
                loginBtn.isEnabled = (email.isNotBlank() && password.isNotBlank())
            }
        }

        // Attach the TextWatcher to EditTexts.
        emailET.addTextChangedListener(textWatcher)
        passwordET.addTextChangedListener(textWatcher)

        // Listener for IME_ACTION_DONE when the user reaches the password field
        passwordET.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && v.text.isNotEmpty()) {
                loginBtn.performClick()
            }
            false
        }
    }

    // Sign in with Firebase.
    @Throws(FirebaseAuthException::class)
    private fun signInWithEmail(view: View, email: String, password: String) {
        showMessage(view, "Authenticating...")

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                finishCurrentAndStartHomeActivity()
            } else {
                // Handle common exception scenarios.
                when (val errorMessage = task.exception?.localizedMessage) {
                    "The email address is badly formatted." -> {
                        emailLayout.error = errorMessage
                        passwordLayout.error = null
                        emailLayout.requestFocus()
                    }
                    "The password is invalid or the user does not have a password." -> {
                        passwordLayout.error = errorMessage
                        passwordLayout.requestFocus()
                        emailLayout.error = null
                    }
                    "There is no user record corresponding to this identifier. The user may have been deleted." -> {
                        emailLayout.error = errorMessage
                        emailLayout.requestFocus()
                        passwordLayout.error = null
                    }
                    else -> {
                        showMessage(view, errorMessage!!)
                        emailLayout.error = null
                        passwordLayout.error = null
                    }

                }
            }
        }

    }

    // Sign in with Google.
    private fun signInWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                finishCurrentAndStartHomeActivity()
            } else {
                showMessage(googleBtn, "Error: $task.exception")
            }
        }
    }

    // Sign in with Facebook.
    private fun signInWithFacebook(view: LoginButton) {
        // Grant permission to access user's email, friends list and public profile data.
        view.setPermissions(listOf("email", "public_profile", "user_friends"))
        view.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                // Extract token from the result and use it to sign in with Firebase.
                val credential = FacebookAuthProvider.getCredential(loginResult.accessToken.token)
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            finishCurrentAndStartHomeActivity()
                        } else {
                            showMessage(view, "Error: ${task.exception.toString()}")
                        }
                    }
            }

            override fun onCancel() {

            }

            override fun onError(exception: FacebookException) {
                showMessage(view, "Error: $exception")
            }
        })
    }

    // Use TopSnackBar to show meaningful messages to the user.
    private fun showMessage(view: View, message: String) {
        TSnackbar.make(view, HtmlCompat.fromHtml("<font color=\"#ffffff\">$message</font>", HtmlCompat.FROM_HTML_MODE_LEGACY),
            TSnackbar.LENGTH_LONG).show()
    }

    private fun finishCurrentAndStartHomeActivity() {
        activity?.finish()
        startActivity(Intent(context, HomeActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle Google sign-in.
        if (requestCode == signInReqCodeGoogle) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                signInWithGoogle(account)
            } catch (e: ApiException) {
                showMessage(googleBtn, "Error: $e")
            }
        } else {
            // Pass the activity result back to the Facebook SDK.
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }
}