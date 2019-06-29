package bask.learnbulgarian.main

import android.app.Application
import bask.learnbulgarian.BuildConfig
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logger
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}