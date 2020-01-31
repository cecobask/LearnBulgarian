package bask.lingvino.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import bask.lingvino.R
import bask.lingvino.activities.HomeActivity
import bask.lingvino.adapters.LanguageAdapter
import bask.lingvino.models.LanguageItem

class LanguagePickerFragment : Fragment() {

    private lateinit var spokenLangTV: TextView
    private lateinit var targetLangTV: TextView
    private lateinit var spokenLangSpinner: Spinner
    private lateinit var targetLangSpinner: Spinner
    private lateinit var spokenLangAdapter: LanguageAdapter
    private lateinit var targetLangAdapter: LanguageAdapter
    private lateinit var continueBtn: Button
    private lateinit var sharedPref: SharedPreferences

    companion object {
        fun newInstance(): LanguagePickerFragment {
            return LanguagePickerFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.language_picker, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind widgets.
        spokenLangTV = view.findViewById(R.id.spokenLangTV)
        targetLangTV = view.findViewById(R.id.targetLangTV)
        spokenLangSpinner = view.findViewById(R.id.spokenLangSpinner)
        targetLangSpinner = view.findViewById(R.id.targetLangSpinner)
        continueBtn = view.findViewById(R.id.continueBtn)

        sharedPref = activity!!.getSharedPreferences("learnBulgarian", 0)
        val spokenLang = sharedPref.getString("SPOKEN_LANG_NAME", "English")!!
        val targetLang = sharedPref.getString("TARGET_LANG_NAME", "Bulgarian")!!

        // Prepare Adapters with a list of languages to select from.
        // Set the default selection for "spoken" to Bulgarian and exclude it from from "target".
        spokenLangAdapter = LanguageAdapter(context!!, initLanguagesList(selectedLang = spokenLang))
        targetLangAdapter = LanguageAdapter(context!!, initLanguagesList(spokenLang, targetLang))

        spokenLangSpinner.apply {
            // Set an adapter.
            adapter = spokenLangAdapter

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("not implemented")
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    // When the user picks a language from the "spoken" Spinner, automatically
                    // update the contents of "target" Spinner to exclude that language.
                    targetLangAdapter.updateData(
                        initLanguagesList(
                            parent?.getItemAtPosition(position).toString(), targetLang
                        )
                    )
                }
            }
        }

        targetLangSpinner.apply {
            // Set an adapter.
            adapter = targetLangAdapter
        }

        sharedPref = context!!.getSharedPreferences("learnBulgarian", 0)
        continueBtn.setOnClickListener {
            sharedPref.edit()
                .apply {
                    val spoken = spokenLangSpinner.selectedItem as LanguageItem
                    val target = targetLangSpinner.selectedItem as LanguageItem
                    putString("SPOKEN_LANG_NAME", spoken.languageName)
                    putInt("SPOKEN_LANG_FLAG", spoken.flag)
                    putString("TARGET_LANG_NAME", target.languageName)
                    putInt("TARGET_LANG_FLAG", target.flag)
                    apply()
                }

            finishCurrentAndStartHomeActivity()
        }
    }

    private fun initLanguagesList(exclusion: String = "none", selectedLang: String = "none"):
            MutableList<LanguageItem> {
        val bg = LanguageItem("Bulgarian", R.drawable.ic_bulgarian)
        val en = LanguageItem("English", R.drawable.ic_english)
        val es = LanguageItem("Spanish", R.drawable.ic_spanish)
        val ru = LanguageItem("Russian", R.drawable.ic_russian)

        // Order list, making sure the 'selectLang' is the first index.
        val list = when (selectedLang) {
            "Bulgarian" -> mutableListOf(bg, en, es, ru)
            "English" -> mutableListOf(en, bg, es, ru)
            "Spanish" -> mutableListOf(es, bg, en, ru)
            "Russian" -> mutableListOf(ru, bg, en, es)
            else -> mutableListOf(bg, en, es, ru)
        }

        // Returns the whole list if the function is called with default value of "none".
        // Otherwise, it will return the list excluding the specified element.
        return if (exclusion === "none") list
        else list.filter { lang -> lang.languageName !== exclusion } as MutableList<LanguageItem>

    }

    private fun finishCurrentAndStartHomeActivity() {
        activity?.finish()
        startActivity(Intent(context, HomeActivity::class.java))
    }
}