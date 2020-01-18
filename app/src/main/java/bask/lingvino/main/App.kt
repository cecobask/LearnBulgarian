package bask.lingvino.main

import android.app.Application
import android.content.Context
import bask.lingvino.BuildConfig
import bask.lingvino.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import timber.log.Timber

class App : Application() {

    companion object {
        fun getGoogleClient(context: Context, clientId: String): GoogleSignInClient {
            val mGoogleSignInOptions: GoogleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build()
            return GoogleSignIn.getClient(context, mGoogleSignInOptions)
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logger.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Fetch values from FirebaseRemoteConfig.
        FirebaseRemoteConfig.getInstance().apply {
            setDefaultsAsync(R.xml.remote_config_defaults)
            fetch(3600)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        this.activate()
                    }
                }
        }

    }
}