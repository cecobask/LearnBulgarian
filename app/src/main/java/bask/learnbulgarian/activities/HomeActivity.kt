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

        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val usernameTV = findViewById<TextView>(R.id.usernameTV)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        firebaseAuth.addAuthStateListener {
            if (firebaseAuth.currentUser == null) {
                finish()
            }
        }

        usernameTV.text = intent.getStringExtra("username")

        logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            finish()
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }
}
