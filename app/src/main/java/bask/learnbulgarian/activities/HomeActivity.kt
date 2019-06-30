package bask.learnbulgarian.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import bask.learnbulgarian.R
import bask.learnbulgarian.main.App
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    lateinit var app: App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val firebaseAuth: FirebaseAuth = App.getFirebaseAuth()
        val googleSignInClient: GoogleSignInClient = App.getGoogleClient(this)

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

        // Bring the user to the AuthActivity when logoutBtn is pressed.
        logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            googleSignInClient.signOut()

            finish()
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }
}
