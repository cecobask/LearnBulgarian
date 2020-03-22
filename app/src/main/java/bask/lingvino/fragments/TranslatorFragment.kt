package bask.lingvino.fragments

import android.Manifest
import android.app.Activity
import android.content.*
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import bask.lingvino.R
import bask.lingvino.models.Translation
import bask.lingvino.utils.CognitiveServices
import bask.lingvino.utils.FirebaseDataCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.isItemChecked
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer
import com.mindorks.paracamera.Camera
import kotlinx.android.synthetic.main.translator.*
import kotlinx.android.synthetic.main.translator.view.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.util.*

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
    private lateinit var sourceLangIV: ImageView
    private lateinit var targetLangIV: ImageView
    private lateinit var camera: Camera
    private lateinit var sharedPref: SharedPreferences
    private lateinit var mView: View
    private lateinit var translatorCollections: DatabaseReference
    private lateinit var databaseRef: DatabaseReference
    private lateinit var fbUser: FirebaseUser
    private lateinit var spokenLangName: String
    private lateinit var targetLangName: String
    private lateinit var cognitiveServices: CognitiveServices
    private val REQUESTCODESPEECH = 10001
    private val REQUESTCODECAMERA = 10002
    private val REQUESTCODESETTINGS = 10003

    companion object {
        fun newInstance(): TranslatorFragment {
            return TranslatorFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_translator, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_saved_translations -> {
                getCollectionNames(object : FirebaseDataCallback {
                    override fun onData(collections: MutableList<String>) {
                        // Open up a dialog with selection of all user's collections.
                        showChooseCollectionDialog(collections)
                    }
                })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.translator, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sourceLangTV = view.findViewById(R.id.sourceLangTV)
        sourceLangIV = view.findViewById(R.id.sourceLangIV)
        targetLangTV = view.findViewById(R.id.targetLangTV)
        targetLangIV = view.findViewById(R.id.targetLangIV)
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
        sharedPref = activity!!.getSharedPreferences("learnBulgarian", 0)
        spokenLangName = sharedPref.getString("SPOKEN_LANG_NAME", "English")!!
        val spokenLangFlag = sharedPref.getInt("SPOKEN_LANG_FLAG", R.drawable.ic_english)
        targetLangName = sharedPref.getString("TARGET_LANG_NAME", "Bulgarian")!!
        val targetLangFlag = sharedPref.getInt("TARGET_LANG_FLAG", R.drawable.ic_bulgarian)

        firebaseNaturalLanguage = FirebaseNaturalLanguage.getInstance()
        firebaseTranslator = firebaseNaturalLanguage.getTranslator(
            setTranslateOptions(
                spokenLangName,
                spokenLangFlag,
                targetLangName,
                targetLangFlag
            )
        )

        // Instantiate FirebaseVision API, used to recognise text in images.
        textDetector = FirebaseVision.getInstance().onDeviceTextRecognizer

        // Make TextView vertically scrollable.
        translationTV.movementMethod = ScrollingMovementMethod()

        userInputTIL.apply {
            setEndIconOnClickListener {
                // Hide translation layout and clear text from the EditText.
                mView.translationRL.visibility = View.GONE
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
        pronounceBtn.setOnClickListener(this)
        favBtn.setOnClickListener(this)

        // Set up the camera settings.
        camera = Camera.Builder()
            .resetToCorrectOrientation(true)
            .setTakePhotoRequestCode(REQUESTCODECAMERA)
            .setDirectory("pics")
            .setName("text_recognition_${System.currentTimeMillis()}")
            .setImageFormat(Camera.IMAGE_JPEG)
            .setCompression(75)//6
            .build(this)

        databaseRef = FirebaseDatabase.getInstance().reference
        fbUser = FirebaseAuth.getInstance().currentUser!!
        translatorCollections = databaseRef.child("users")
            .child(fbUser.uid)
            .child("translatorCollections")

        // This class handles pronunciations.
        cognitiveServices = CognitiveServices(context!!)

        // Create empty 'Favourites' collection if it doesn't exist.
        createFavouritesCollection()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.switchLangBtn -> {
                // Swap source and target languages and icons.
                val tempStr = targetLangTV.text
                val tempInt = targetLangIV.tag
                targetLangTV.text = sourceLangTV.text
                sourceLangTV.text = tempStr
                targetLangIV.setImageResource(sourceLangIV.tag as Int)
                sourceLangIV.setImageResource(tempInt as Int)

                // Swap ImageView tags.
                targetLangIV.tag = sourceLangIV.tag
                sourceLangIV.tag = tempInt

                // Update Translator options.
                firebaseTranslator = firebaseNaturalLanguage.getTranslator(
                    setTranslateOptions(
                        sourceLangTV.text as String, sourceLangIV.tag as Int,
                        targetLangTV.text as String, targetLangIV.tag as Int
                    )
                )

                voiceBtn.isEnabled = sourceLangTV.text != "Bulgarian"
            }
            R.id.copyBtn -> {
                // Copy translation to clipboard.
                val myClip: ClipData = ClipData.newPlainText("translation", translationTV.text)
                clipboardManager.setPrimaryClip(myClip)

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
                openCamera()
            }
            R.id.pronounceBtn -> {
                cognitiveServices.pronounceText(translationTV.text.toString(), targetLangName)
            }
            R.id.favBtn -> {
                showSaveToCollectionDialog(
                    input = userInputTIET.text.toString(),
                    translation = translationTV.text.toString()
                )
            }
        }
    }

    private fun createFavouritesCollection() {
        translatorCollections.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.tag("translatorCollections").e(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                // Check if 'Favourites' collection exists.
                val collectionExists = p0.children.any { collection ->
                    collection.key == "Favourites"
                }
                if (!collectionExists)
                // Create empty 'Favourites' collection if it doesn't exist.
                    translatorCollections.child("Favourites").setValue("null")
            }
        })
    }

    // Gets existing user collections and enables the creation of new collections.
    // Afterwards the user is prompted to select which collection/s to add a translation to.
    private fun showSaveToCollectionDialog(input: String, translation: String) {
        // Load collection names from Firebase.
        getCollectionNames(object : FirebaseDataCallback {
            override fun onData(collections: MutableList<String>) {
                // Show a dialog with checkboxes that match user's collection names.
                MaterialDialog(context!!).show {
                    title(text = "Save to collection/s:")
                    positiveButton(text = "Done")
                    negativeButton(text = "Cancel")
                    @Suppress("DEPRECATION")
                    neutralButton(text = "Create collection") {
                        this.cancel()
                        showInputDialog()
                    }

                    // Hide progress bar.
                    mView.progressBar.visibility = View.GONE
                    mView.translationRL.visibility = View.VISIBLE

                    // Triggers this callback when the user clicks on "Done" button.
                    listItemsMultiChoice(items = collections) { _, _, items ->
                        val translationObj = Translation(input, translation, spokenLangName,
                            targetLangName)
                        translationObj.expanded = null // Trick to not push value for expanded to DB.

                        // Change button tint to red.
                        mView.favBtn.setColorFilter(ContextCompat.getColor(context, R.color.red))

                        // Push the Translation object to selected collections.
                        items.forEach { item ->
                            val collection = item.toString().substringBeforeLast(" (")
                            checkIfTranslationExists(
                                collection, input, translation, object : FirebaseDataCallback {
                                    override fun onData(exists: Boolean) {
                                        when (exists) {
                                            true ->
                                                Snackbar.make(
                                                    mView.translateBtn,
                                                    "Translation already exists in collection '$collection'.",
                                                    Snackbar.LENGTH_SHORT
                                                ).show()
                                            false -> {
                                                translatorCollections.child(collection).push()
                                                    .setValue(translationObj)
                                                Snackbar.make(
                                                    mView.translateBtn,
                                                    "Successfully added to collection '$collection'.",
                                                    Snackbar.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                })
                        }
                    }
                }
            }
        })
    }

    private fun checkIfTranslationExists(collection: String,
                                         inputStr: String,
                                         translationStr: String,
                                         callback: FirebaseDataCallback
    ) {
        translatorCollections.child(collection)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) =
                    Timber.tag("translatorCollections").e(p0.message)

                override fun onDataChange(p0: DataSnapshot) {
                    // Check if any translations from the collection match the newly created one.
                    val translationExists = p0.children.any { translationSnap ->
                        val translationObj = translationSnap.getValue(Translation::class.java)
                        translationObj?.translation?.toLowerCase(Locale.getDefault()) ==
                                translationStr.toLowerCase(Locale.getDefault())
                                &&
                                translationObj.input?.toLowerCase(Locale.getDefault()) ==
                                inputStr.toLowerCase(Locale.getDefault())
                    }

                    // Pass boolean value to the callback.
                    callback.onData(translationExists)
                }

            })
    }

    private fun getCollectionNames(callback: FirebaseDataCallback
    ) {
        // Show progress bar.
        mView.progressBar.visibility = View.VISIBLE

        // Retrieves existing collections.
        translatorCollections.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.tag("translatorCollections").e(p0.message)
                mView.progressBar.visibility = View.GONE
                mView.translationRL.visibility = View.VISIBLE
            }

            override fun onDataChange(p0: DataSnapshot) {
                val collectionNames = mutableListOf<String>()
                p0.children.forEach { collectionNames.add("${it.key!!} (${it.childrenCount})") }

                // Trigger callback to make collection names accessible outside this scope.
                callback.onData(collectionNames)

                // Hide progress bar.
                mView.progressBar.visibility = View.GONE
            }

        })
    }

    // Prompts the user to input collection name.
    private fun showInputDialog() {
        MaterialDialog(context!!).show {
            positiveButton(text = "Create") {
                it.cancel()
                // Create empty collection.
                val input = it.getInputField().text.toString()
                translatorCollections.child(input).setValue("null")
                // Display the checkbox dialog again, but this time including the newly added collection.
                showSaveToCollectionDialog(
                    mView.userInputTIET.text.toString(),
                    mView.translationTV.text.toString()
                )
            }
            negativeButton(text = "Cancel")

            // Validate user input.
            input(
                hint = "Enter collection name",
                inputType = InputType.TYPE_CLASS_TEXT,
                maxLength = 25,
                waitForPositiveButton = false
            ) { dialog, text ->
                val inputField = dialog.getInputField()
                val error = arrayOf(".", "#", "$", "[", "]").any { it in text }
                inputField.error = if (error) "Names must not contain . # $ [ ]" else null
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, !error)
            }
        }
    }

    private fun showChooseCollectionDialog(collections: List<String>) {
        MaterialDialog(context!!).show {
            title(text = "Choose collection:")

            // Disable 'Remove' button initially, as there is no selection.
            this.setActionButtonEnabled(WhichButton.NEUTRAL, false)

            listItemsSingleChoice(
                items = collections, waitForPositiveButton = false
            ) { _, _, collection ->
                // 'Go' button is only enabled if the selected collection is not empty.
                this.setActionButtonEnabled(WhichButton.POSITIVE, !collection.endsWith("(0)"))
                // 'Remove' button is always disabled for 'Favourites' collection and enabled for the rest.
                this.setActionButtonEnabled(
                    WhichButton.NEUTRAL,
                    collection.toString().substringBeforeLast(" (") != "Favourites"
                )
            }

            positiveButton(text = "Go") { dialog ->
                // Open selected collection in a new Fragment.
                val selectedIndex = collections.indices.find { dialog.isItemChecked(it) }
                val translatorFavsFragment = TranslatorFavouritesFragment.newInstance(
                    collections[selectedIndex!!].substringBeforeLast(" (")
                )
                fragmentManager!!
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        translatorFavsFragment,
                        translatorFavsFragment.javaClass.simpleName
                    )
                    .addToBackStack(null)
                    .commit()
            }
            negativeButton(text = "Cancel")
            @Suppress("DEPRECATION")
            neutralButton(text = "Remove") { dialog ->
                // Determine which collection was selected and remove it.
                val selectedIndex = collections.indices.find { dialog.isItemChecked(it) }
                translatorCollections
                    .child(collections[selectedIndex!!].substringBeforeLast(" ("))
                    .removeValue()
            }
        }
    }

    private fun setTranslateOptions(spokenLangName: String = "English",
                                    spokenLangFlag: Int = R.drawable.ic_english,
                                    targetLangName: String = "Bulgarian",
                                    targetLangFlag: Int = R.drawable.ic_bulgarian
    ): FirebaseTranslatorOptions {
        return FirebaseTranslatorOptions.Builder().apply {
            when (spokenLangName) {
                "English" -> setSourceLanguage(FirebaseTranslateLanguage.EN)
                "Bulgarian" -> setSourceLanguage(FirebaseTranslateLanguage.BG)
                "Spanish" -> setSourceLanguage(FirebaseTranslateLanguage.ES)
                "Russian" -> setSourceLanguage(FirebaseTranslateLanguage.RU)
            }

            when (targetLangName) {
                "English" -> setTargetLanguage(FirebaseTranslateLanguage.EN)
                "Bulgarian" -> setTargetLanguage(FirebaseTranslateLanguage.BG)
                "Spanish" -> setTargetLanguage(FirebaseTranslateLanguage.ES)
                "Russian" -> setTargetLanguage(FirebaseTranslateLanguage.RU)
            }

            sourceLangTV.text = spokenLangName
            sourceLangIV.setImageResource(spokenLangFlag)
            sourceLangIV.tag = spokenLangFlag

            targetLangTV.text = targetLangName
            targetLangIV.setImageResource(targetLangFlag)
            targetLangIV.tag = targetLangFlag
        }.build()
    }

    private fun translateText(text: String) {
        // Download language model for offline translations.
        firebaseTranslator.downloadModelIfNeeded()
            .addOnFailureListener {
                // Log the error and hide progress bar.
                Timber.tag("translateModel").d(it)
                progressBar.visibility = View.GONE
            }
            .addOnSuccessListener {
                firebaseTranslator.translate(text)
                    .addOnSuccessListener { translation ->
                        // Show translation layout and display the translation text.
                        translationRL.visibility = View.VISIBLE
                        translationTV.text = translation
                        mView.favBtn.setColorFilter(ContextCompat.getColor(context!!, R.color.white))
                    }
                    // Log the error.
                    .addOnFailureListener { Timber.tag("translation").d(it) }
                    // Hide progress bar.
                    .addOnCompleteListener { progressBar.visibility = View.GONE }
            }
    }

    private fun speak() {
        // Start an intent that lets the user speak their query instead of inputting text.
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // Works only with English.
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now, please...")
        }

        try {
            startActivityForResult(intent, REQUESTCODESPEECH)
        } catch (e: Exception) {
            Timber.tag("speechh").d(e)
        }
    }

    @AfterPermissionGranted(10002)
    private fun openCamera() {
        // Check if the required permissions were granted.
        val perms = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(context!!, *perms)) {
            try {
                // Bring up the camera and let the user capture an image for text recognition.
                camera.takePicture()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else
            // Request the required permissions.
            EasyPermissions.requestPermissions(
                this,
                "CAMERA and WRITE_EXTERNAL_STORAGE permissions are required for this feature.",
                REQUESTCODECAMERA,
                *perms
            )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        // User has ticked the "Don't ask again" checkbox when asked for permission.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms))
        // Redirect user to the App Settings menu.
            AppSettingsDialog.Builder(this).setRequestCode(REQUESTCODESETTINGS)
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

        // Let EasyPermissions handle the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUESTCODESPEECH -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    // Set userInputTIET's value to the text from speech recognition.
                    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                    userInputTIET.setText(result[0])
                    // Translate the text.
                    translateBtn.performClick()
                }
            }
            REQUESTCODECAMERA -> {
                // Fetch the captured image.
                val bitmap = camera.cameraBitmap
                if (resultCode == Activity.RESULT_OK && bitmap != null) {
                    val image = FirebaseVisionImage.fromBitmap(bitmap)
                    textDetector.processImage(image)
                        .addOnSuccessListener {
                            // Translate recognised text from the image.
                            userInputTIET.setText(it.text)
                            translateBtn.performClick()
                        }
                        .addOnFailureListener { Timber.tag("textDetect").e(it.localizedMessage) }
                }
            }
            REQUESTCODESETTINGS -> {
                // Open the camera if all permissions were granted.
                if (EasyPermissions.hasPermissions(
                        context!!,
                        Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
                    openCamera()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Change toolbar title.
        (activity as? AppCompatActivity)?.supportActionBar?.title =
            resources.getString(R.string.translatorTitle)
    }
}