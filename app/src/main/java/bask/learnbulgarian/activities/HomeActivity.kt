package bask.learnbulgarian.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import bask.learnbulgarian.R
import bask.learnbulgarian.fragments.WordOfTheDayFragment
import bask.learnbulgarian.main.App
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception

class HomeActivity : AppCompatActivity() {

    private lateinit var mDrawer: DrawerLayout
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var navView: NavigationView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var headerView: View
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Instantiate widgets.
        toolbar = findViewById(R.id.toolbar)
        mDrawer = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)
        headerView = navView.getHeaderView(0)

        // Set a Toolbar to replace the ActionBar.
        setSupportActionBar(toolbar)
        setupDrawerContent(navView)

        // Tie DrawerLayout events to the ActionBarToggle.
        drawerToggle = ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close)
        mDrawer.addDrawerListener(drawerToggle)

        // Authentication providers.
        firebaseAuth = FirebaseAuth.getInstance()
        googleSignInClient = App.getGoogleClient(this, getString(R.string.default_web_client_id))

        // This activity is the first to start after the app launches.
        // The AuthStateListener decides whether user is brought to the AuthActivity.
        firebaseAuth.addAuthStateListener {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                finish()
                startActivity(Intent(this, AuthActivity::class.java))
            } else {
                val userEmailTV: TextView = headerView.findViewById(R.id.userEmailTV)
                val userPhotoIV: CircleImageView = headerView.findViewById(R.id.userPhotoIV)

                if (currentUser.photoUrl != null) {
                    Glide.with(applicationContext).load(currentUser.photoUrl).into(userPhotoIV)
                } else {
                    Glide.with(applicationContext).load(R.drawable.ic_user_default).into(userPhotoIV)
                }
                userEmailTV.text = currentUser.email
            }
        }
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener {
            selectDrawerItem(it)
            true
        }
    }

    private fun selectDrawerItem(menuItem: MenuItem) {
        var fragmentClass: Class<*>? = null
        var currentFragment: Fragment? = null

        when (menuItem.itemId) {
            R.id.navItemLogOut -> {
                // Logout current user and bring it to the AuthActivity when logoutBtn is pressed.
                firebaseAuth.signOut()
                googleSignInClient.signOut()
                LoginManager.getInstance().logOut()
                finish()
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
            }
            R.id.navItemWOTD -> {
                fragmentClass = WordOfTheDayFragment::class.java
            }
        }

        try {
            currentFragment = fragmentClass?.newInstance() as Fragment?
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Insert the fragment by replacing any existing fragment.
        val fragmentManager = supportFragmentManager
        fragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, currentFragment!!)
            .addToBackStack(null)
            .commit()

        // Highlight the selected item.
        menuItem.isChecked = true
        mDrawer.closeDrawers()

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) return true
        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Pass any configuration change to the drawer toggles.
        drawerToggle.onConfigurationChanged(newConfig)
    }
}
