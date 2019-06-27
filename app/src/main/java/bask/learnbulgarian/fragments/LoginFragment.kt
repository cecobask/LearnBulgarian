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
import bask.learnbulgarian.activities.AuthActivity
import bask.learnbulgarian.activities.HomeActivity
import com.androidadvance.topsnackbar.TSnackbar
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment() {

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameET = view.findViewById<EditText>(R.id.usernameET)
        val passwordET = view.findViewById<EditText>(R.id.passwordET)
        val loginBtn = view.findViewById<Button>(R.id.loginBtn)


        loginBtn.setOnClickListener {
            val email: String = usernameET.text.toString().trim()
            val password: String = passwordET.text.toString().trim()
            signIn(it, email, password)
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

        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(context as AuthActivity) { task ->
            if (task.isSuccessful) {
                activity?.finish()
                val intent = Intent(context, HomeActivity::class.java)
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