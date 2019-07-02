package bask.learnbulgarian.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import bask.learnbulgarian.R
import bask.learnbulgarian.main.App
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val googleSignInClient: GoogleSignInClient =
            App.getGoogleClient(this, getString(R.string.default_web_client_id))

        // Instantiate widgets.
        val usernameTV = findViewById<TextView>(R.id.usernameTV)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        // This activity is the first to start after the app launches.
        // The AuthStateListener decides whether user is brought to the AuthActivity.
        firebaseAuth.addAuthStateListener {
            if (firebaseAuth.currentUser == null) {
                finish()
                startActivity(Intent(this, AuthActivity::class.java))
            }
        }

        usernameTV.text = firebaseAuth.currentUser?.email

        // Logout current user and bring it to the AuthActivity when logoutBtn is pressed.
        logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            googleSignInClient.signOut()
            LoginManager.getInstance().logOut()

            finish()
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }
}
