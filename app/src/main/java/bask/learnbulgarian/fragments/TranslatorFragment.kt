package bask.learnbulgarian.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import bask.learnbulgarian.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

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

        val sourceLangTV: TextView = view.findViewById(R.id.sourceLangTV)
        val switchLangBtn: AppCompatImageButton = view.findViewById(R.id.switchLangBtn)
        val targetLangTV: TextView = view.findViewById(R.id.targetLangTV)
        val userInputTIET: TextInputEditText = view.findViewById(R.id.userInputTIET)
        val cameraBtn: AppCompatButton = view.findViewById(R.id.cameraBtn)
        val voiceBtn: AppCompatButton = view.findViewById(R.id.voiceBtn)
        val translateBtn: AppCompatButton = view.findViewById(R.id.translateBtn)
        val translationRL: RelativeLayout = view.findViewById(R.id.translationRL)
        val translationTV: TextView = view.findViewById(R.id.translationTV)
        val pronounceBtn: AppCompatImageButton = view.findViewById(R.id.pronounceBtn)
        val favBtn: AppCompatImageButton = view.findViewById(R.id.favBtn)
        val exportBtn: AppCompatImageButton = view.findViewById(R.id.exportBtn)
        val copyBtn: AppCompatImageButton = view.findViewById(R.id.copyBtn)
        val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Swap source and target languages.
        switchLangBtn.setOnClickListener {
            val tempStr = targetLangTV.text
            targetLangTV.text = sourceLangTV.text
            sourceLangTV.text = tempStr
        }

        // Make TextView vertically scrollable.
        translationTV.movementMethod = ScrollingMovementMethod()

        userInputTIET.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Enable/Disable translateBtn depending on if the user has entered any text.
                    val userInput = userInputTIET.text.toString().trim()
                    translateBtn.isEnabled = userInput.isNotBlank()
                }

            })

            // Set IME options and input type.
            imeOptions = EditorInfo.IME_ACTION_DONE
            setRawInputType(InputType.TYPE_CLASS_TEXT)

            setOnEditorActionListener { v, actionId, _ ->
                // Listen for IME button clicks.
                if (actionId == EditorInfo.IME_ACTION_DONE && v.text.isNotBlank())
                    // Simulate click on translateBtn.
                    translateBtn.performClick()
                false
            }
        }

        copyBtn.setOnClickListener {
            // Copy translation to clipboard.
            val myClip: ClipData = ClipData.newPlainText("translation", translationTV.text)
            clipboardManager.primaryClip = myClip

            // Show a SnackBar to inform the user.
            Snackbar.make(translationTV, "Translation copied.", Snackbar.LENGTH_SHORT).show()
        }

    }


}