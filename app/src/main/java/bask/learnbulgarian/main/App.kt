package bask.learnbulgarian.main

import android.app.Application
import android.content.Context
import bask.learnbulgarian.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

class App : Application() {

    companion object {
        fun getFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
        fun getGoogleClient(context: Context): GoogleSignInClient {
            val mGoogleSignInOptions: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("270442265431-g4ev29fu2nmuslkn1qv26nhpho81aer7.apps.googleusercontent.com")
                .requestEmail()
                .build()
            return GoogleSignIn.getClient(context, mGoogleSignInOptions)
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logger
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}