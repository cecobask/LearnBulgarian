package bask.learnbulgarian.fragments

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import bask.learnbulgarian.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import com.mindorks.paracamera.Camera
import kotlinx.android.synthetic.main.translator.*
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.lang.Exception
import java.util.*
import java.util.jar.Manifest

class TranslatorFragment : Fragment(), View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private lateinit var firebaseNaturalLanguage: FirebaseNaturalLanguage
    private lateinit var firebaseTranslator: FirebaseTranslator
    private lateinit var textDetector: FirebaseVisionTextRecognizer
    private lateinit var translationRL: RelativeLayout
    private lateinit var translationTV: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var targetLangTV: TextView
    private lateinit var sourceLangTV: TextView
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var userInputTIET: TextInputEditText
    private lateinit var translateOptions: FirebaseTranslatorOptions
    private lateinit var camera: Camera
    private val REQUESTCODESPEECH = 10001
    private val REQUESTCODECAMERA = 10002

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

        sourceLangTV = view.findViewById(R.id.sourceLangTV)
        targetLangTV = view.findViewById(R.id.targetLangTV)
        val switchLangBtn: AppCompatImageButton = view.findViewById(R.id.switchLangBtn)
        val userInputTIL: TextInputLayout = view.findViewById(R.id.userInputTIL)
        userInputTIET = view.findViewById(R.id.userInputTIET)
        val cameraBtn: AppCompatButton = view.findViewById(R.id.cameraBtn)
        val voiceBtn: AppCompatButton = view.findViewById(R.id.voiceBtn)
        val translateBtn: AppCompatButton = view.findViewById(R.id.translateBtn)
        translationRL = view.findViewById(R.id.translationRL)
        translationTV = view.findViewById(R.id.translationTV)
        val pronounceBtn: AppCompatImageButton = view.findViewById(R.id.pronounceBtn)
        val favBtn: AppCompatImageButton = view.findViewById(R.id.favBtn)
        val exportBtn: AppCompatImageButton = view.findViewById(R.id.exportBtn)
        val copyBtn: AppCompatImageButton = view.findViewById(R.id.copyBtn)
        clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        progressBar = view.findViewById(R.id.progressBar)

        // Set the default source to English & target to Bulgarian.
        firebaseNaturalLanguage = FirebaseNaturalLanguage.getInstance()
        firebaseTranslator = firebaseNaturalLanguage.getTranslator(
            FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(FirebaseTranslateLanguage.BG)
                .build()
        )
        textDetector = FirebaseVision.getInstance().onDeviceTextRecognizer

        // Make TextView vertically scrollable.
        translationTV.movementMethod = ScrollingMovementMethod()

        userInputTIL.apply {
            setEndIconOnClickListener {
                // Hide translation layout and clear text from the EditText.
                translationRL.visibility = View.GONE
                userInputTIET.text?.clear()
            }
        }

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

        // Set onClickListeners.
        copyBtn.setOnClickListener(this)
        translateBtn.setOnClickListener(this)
        switchLangBtn.setOnClickListener(this)
        voiceBtn.setOnClickListener(this)
        cameraBtn.setOnClickListener(this)

        camera = Camera.Builder()
            .resetToCorrectOrientation(true)
            .setTakePhotoRequestCode(REQUESTCODECAMERA)
            .setDirectory("pics")
            .setName("text_recognition_${System.currentTimeMillis()}")
            .setImageFormat(Camera.IMAGE_JPEG)
            .setCompression(75)//6
            .build(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.switchLangBtn -> {
                // Swap source and target languages.
                val tempStr = targetLangTV.text
                targetLangTV.text = sourceLangTV.text
                sourceLangTV.text = tempStr

                translateOptions =
                    if (sourceLangTV.text == "English")
                        FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(FirebaseTranslateLanguage.EN)
                            .setTargetLanguage(FirebaseTranslateLanguage.BG)
                            .build()
                    else FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.BG)
                        .setTargetLanguage(FirebaseTranslateLanguage.EN)
                        .build()

                firebaseTranslator = firebaseNaturalLanguage.getTranslator(translateOptions)
            }
            R.id.copyBtn -> {
                // Copy translation to clipboard.
                val myClip: ClipData = ClipData.newPlainText("translation", translationTV.text)
                clipboardManager.primaryClip = myClip

                // Show a SnackBar to inform the user.
                Snackbar.make(translationTV, "Translation copied.", Snackbar.LENGTH_SHORT).show()
            }
            R.id.translateBtn -> {
                // Translate user input.
                progressBar.visibility = View.VISIBLE
                translateText(userInputTIET.text.toString())
            }
            R.id.voiceBtn -> {
                speak()
            }
            R.id.cameraBtn -> {
                requestPermisssions()
                try {
                    camera.takePicture()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun translateText(text: String) {
        // Download language model for offline translations.
        firebaseTranslator.downloadModelIfNeeded()
            .addOnFailureListener {
                // Log the error and hide progress bar.
                Timber.tag("translateModel").d(it)
                progressBar.visibility = View.GONE
            }

        firebaseTranslator.translate(text)
            .addOnSuccessListener { translation ->
                // Show translation layout and display the translation text.
                translationRL.visibility = View.VISIBLE
                translationTV.text = translation
            }
            // Log the error.
            .addOnFailureListener { Timber.tag("translation").d(it) }
            // Hide progress bar.
            .addOnCompleteListener { progressBar.visibility = View.GONE }
    }

    private fun speak() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now, please...")
        }

        try {
            startActivityForResult(intent, REQUESTCODESPEECH)
        } catch (e: Exception) {
            Timber.tag("speechh").d(e)
        }
    }

    private fun requestPermisssions() {
        if (EasyPermissions.hasPermissions(context!!, android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Timber.tag("perms").d("ALL PERMISSIONS GRANTED!")
        } else {
            EasyPermissions.requestPermissions(this,
                "Please, grant permission to use the camera and store the photos on the device.",
                REQUESTCODECAMERA,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Timber.tag("permsDenied").d(perms.toString())
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Timber.tag("permsGranted").d(perms.toString())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUESTCODESPEECH -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    userInputTIET.setText(result[0])
                    translateBtn.performClick()
                    Timber.tag("speechh").d(result[0])
                }
            }
            REQUESTCODECAMERA -> {
                val bitmap = camera.cameraBitmap
                if (resultCode == Activity.RESULT_OK && bitmap != null) {
                    val image = FirebaseVisionImage.fromBitmap(bitmap)
                    textDetector.processImage(image)
                        .addOnSuccessListener { Timber.tag("textDetect").d(it.text) }
                        .addOnFailureListener { Timber.tag("textDetect").d(it.localizedMessage) }
                }
            }
        }
    }
}