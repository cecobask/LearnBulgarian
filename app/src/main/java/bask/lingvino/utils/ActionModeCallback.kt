package bask.lingvino.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.recyclerview.selection.SelectionTracker
import bask.lingvino.R
import bask.lingvino.adapters.TranslationsAdapter
import bask.lingvino.adapters.WordOfTheDayAdapter
import bask.lingvino.models.Translation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import timber.log.Timber
import kotlin.reflect.typeOf


/**
 * Helper class for ActionMode.
 */
class ActionModeCallback(private val collectionName: String, private val context: Context, private val targetLang: String) : ActionMode.Callback {

    // Store values when ActionMode is created.
    private lateinit var tracker: SelectionTracker<Long>
    private lateinit var trackerStr: SelectionTracker<String>
    private lateinit var cognitiveServices: CognitiveServices
    private var mode: ActionMode? = null
    @MenuRes
    private var menuResId: Int = 0
    private var adapter: WordOfTheDayAdapter? = null
    private var adapterStr: TranslationsAdapter? = null
    private lateinit var userRef: DatabaseReference

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        this.mode = mode
        mode.menuInflater.inflate(menuResId, menu)
        cognitiveServices = CognitiveServices(context)
        userRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        if (::trackerStr.isInitialized) {
            val pronounceItem = menu.findItem(R.id.pronounceItem)
            val moveItem = menu.findItem(R.id.moveItem)
            pronounceItem.isVisible = trackerStr.selection.size() == 1
            moveItem.isVisible = true
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        this.mode = null
        // Clear selected items if ActionMode is destroyed.
        if (::tracker.isInitialized) {
            tracker.clearSelection()
        } else {
            trackerStr.clearSelection()
        }
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        // Clicked on 'delete' button.
        if (item.itemId == R.id.deleteItems) {
            // ActionMode is being used by WordOfTheDayFavouritesFragment.
            if (::tracker.isInitialized) {
                // Remove selected words from the DB and RecyclerView.
                val selectedIds: List<String> = tracker.selection.map { it.toString() }
                adapter?.removeItems(
                    adapter?.getSelectedItemsById(selectedIds)!!,
                    userRef.child("favWords")
                )
                mode.finish()
            }
            // ActionMode is being used by TranslatorFavouritesFragment.
            else {
                val collectionRef = userRef
                    .child("translatorCollections")
                    .child(collectionName)

                // Remove selected collections from the DB and RecyclerView.
                val selectedIds = trackerStr.selection.map { it }
                adapterStr?.removeItems(adapterStr?.getSelectedItemsById(selectedIds)!!, collectionRef)
                mode.finish()
            }
        }
        // Clicked on 'pronounce' button.
        else if (item.itemId == R.id.pronounceItem) {
            val selection = trackerStr.selection.map { it }
            val textToPronounce = adapterStr!!.getSelectedItemsById(selection)[0].translation
            cognitiveServices.pronounceText(textToPronounce, targetLang)
        }
        // Clicked on 'copy to' button.
        else {
            val selectedIds = trackerStr.selection.map { it }
            val selectedItems = adapterStr?.getSelectedItemsById(selectedIds)!!
            showCopyDialog(selectedItems)
        }
        return true
    }

    // Function aimed for WordOfTheDayFavouritesFragment.
    fun startActionMode(
        view: View,
        @MenuRes menuResId: Int,
        tracker: SelectionTracker<Long>,
        adapter: WordOfTheDayAdapter
    ) {
        this.menuResId = menuResId
        this.tracker = tracker
        this.adapter = adapter
        view.startActionMode(this)
    }

    // Function aimed for TranslatorFavouritesFragment.
    fun startActionMode(
        view: View,
        @MenuRes menuResId: Int,
        tracker: SelectionTracker<String>,
        adapter: TranslationsAdapter
    ) {
        this.menuResId = menuResId
        this.trackerStr = tracker
        this.adapterStr = adapter
        view.startActionMode(this)
    }

    fun invalidateActionMode() {
        mode?.invalidate()
    }

    fun finishActionMode() {
        mode?.finish()
    }

    /**
     * Granted the user has selected one or more translations from an existing collection,
     * they will be presented with a dialog, where they have to choose which collection/s
     * to copy their selection to.
     */
    private fun showCopyDialog(selectedItems: List<Translation>) {
        MaterialDialog(context).show {
            title(text = "Copy selected items to:")
            positiveButton(text = "Done")
            negativeButton(text = "Cancel")

            userRef.child("translatorCollections")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    @SuppressLint("CheckResult")
                    override fun onDataChange(p0: DataSnapshot) {
                        val collections = mutableListOf<String>()
                        p0.children.forEach { collections.add(it.key!!) }

                        // Triggers this callback when the user clicks on "Done" button.
                        listItemsMultiChoice(
                            // Pass all collection names, except the currently opened one.
                            items = collections.filter { it != collectionName }) { _, _, selectedCollections ->
                            checkIfItemsExistInCollections(selectedCollections, selectedItems)
                        }
                    }
                })
        }
    }

    private fun checkIfItemsExistInCollections(selectedCollections: List<CharSequence>,
                                               selectedItems: List<Translation>
    ) {
        userRef.child("translatorCollections")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Timber.tag("checkIfItemsExistInCollections").e(p0.toException())
                }

                override fun onDataChange(p0: DataSnapshot) {
                    // Get a list of DataSnapshots that match the selected collection names.
                    val relevantCollections = p0.children.filter { collectionSnapshot ->
                        collectionSnapshot.key in selectedCollections.map { it.toString() }
                    }

                    // Loop through relevant collection DataSnapshots.
                    relevantCollections.forEach { collectionSnapshot ->
                        if (collectionSnapshot.hasChildren()) { // The collection is not empty.
                            // Loop through selected Translation objects.
                            selectedItems.forEach { item ->
                                // Construct a List of existing translations.
                                val existingTranslations = mutableListOf<Translation>()
                                collectionSnapshot.children.forEach { translationSnapshot ->
                                    val translation =
                                        translationSnapshot.getValue(Translation::class.java)!!
                                    existingTranslations.add(translation)
                                }
                                // Check if any existing Translations match the selected Translations.
                                if (!existingTranslations.map { it.translation }.contains(item.translation)
                                    && !existingTranslations.map { it.input }.contains(item.input)
                                ) {
                                    // If no match is found, insert the Translation to the collection.
                                    userRef.child("translatorCollections")
                                        .child(collectionSnapshot.key!!).push()
                                        .setValue(item)
                                }
                            }
                        }
                        // The collection is empty.
                        else {
                            // Insert all selected Translations into the collection.
                            selectedItems.forEach { item ->
                                userRef.child("translatorCollections")
                                    .child(collectionSnapshot.key!!).push()
                                    .setValue(item)
                            }
                        }
                    }
                    finishActionMode()
                }
            })
    }
}