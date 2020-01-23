package bask.lingvino.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import bask.lingvino.R
import bask.lingvino.fragments.LanguagePickerFragment

class LanguageSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.language_selection)

        val fragment = LanguagePickerFragment.newInstance()
        supportFragmentManager.apply {
            beginTransaction()
                .replace(R.id.languageSelectionFragment, fragment, fragment.javaClass.simpleName)
                .commit()
        }
    }

    override fun onBackPressed() {
        // Do not allow users to go back and close the app instead.
        finishAffinity()
    }
}