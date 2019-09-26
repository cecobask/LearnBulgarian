package bask.learnbulgarian.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import bask.learnbulgarian.R

class TranslatorFragment: Fragment() {

    companion object {
        fun newInstance(): TranslatorFragment {
            return TranslatorFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?  = inflater.inflate(R.layout.translator, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Change toolbar title.
        (activity as? AppCompatActivity)?.supportActionBar?.title =
            resources.getString(R.string.translatorTitle)

        val translationTV: TextView = view.findViewById(R.id.translationTV)

        // Make TextView vertically scrollable.
        translationTV.movementMethod = ScrollingMovementMethod()
    }
}