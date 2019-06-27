package bask.learnbulgarian.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import bask.learnbulgarian.R
import bask.learnbulgarian.fragments.LoginFragment


class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth)

        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.add(R.id.fragmentContainer, LoginFragment.newInstance())
        transaction.commit()
    }
}
