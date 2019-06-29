package bask.learnbulgarian.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import bask.learnbulgarian.R
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Instantiate widgets.
        val usernameTV = findViewById<TextView>(R.id.usernameTV)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        // This activity is the first to start after the app launches.
        // The AuthStateListener decides whether user is brought to the AuthActivity.
        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
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
            finish()
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }
}
