package bask.lingvino.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import bask.lingvino.R
import bask.lingvino.adapters.LanguageAdapter
import bask.lingvino.models.LanguageItem

class LanguagePickerFragment : Fragment() {

    private lateinit var spokenLangTV: TextView
    private lateinit var targetLangTV: TextView
    private lateinit var spokenLangSpinner: Spinner
    private lateinit var targetLangSpinner: Spinner
    private lateinit var languageAdapter: LanguageAdapter

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

        spokenLangTV = view.findViewById(R.id.spokenLangTV)
        targetLangTV = view.findViewById(R.id.targetLangTV)
        spokenLangSpinner = view.findViewById(R.id.spokenLangSpinner)
        targetLangSpinner = view.findViewById(R.id.targetLangSpinner)

        // Prepare adapter with a list of languages to select from.
        languageAdapter = LanguageAdapter(context!!, initLanguagesList())

        spokenLangSpinner.adapter = languageAdapter
        targetLangSpinner.adapter = languageAdapter

        spokenLangSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Toast.makeText(
                    context,
                    parent?.getItemAtPosition(position).toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    private fun initLanguagesList(): MutableList<LanguageItem> {
        val bg = LanguageItem("Bulgarian", R.drawable.ic_bulgarian)
        val en = LanguageItem("English", R.drawable.ic_english)
        val rand = LanguageItem("Other", R.drawable.ic_export)

        return mutableListOf(bg, en, rand)
    }
}