package bask.lingvino.utils

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.recyclerview.selection.SelectionTracker
import bask.lingvino.R
import bask.lingvino.adapters.TranslationsAdapter
import bask.lingvino.adapters.WordOfTheDayAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


/**
 * Helper class for ActionMode.
 */
class ActionModeCallback(private val collectionName: String) : ActionMode.Callback {

    // Store values when ActionMode is created.
    private lateinit var tracker: SelectionTracker<Long>
    private lateinit var trackerStr: SelectionTracker<String>
    private var mode: ActionMode? = null
    @MenuRes
    private var menuResId: Int = 0
    private var adapter: WordOfTheDayAdapter? = null
    private var adapterStr: TranslationsAdapter? = null

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        this.mode = mode
        mode.menuInflater.inflate(menuResId, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
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
        // Clicked on delete button.
        if (item.itemId == R.id.deleteItems) {
            if (::tracker.isInitialized) {
                val favWordsRef = FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("favWords")

                // Remove selected words from the DB and RecyclerView.
                val selectedIds: List<String> = tracker.selection.map { it.toString() }
                adapter?.removeItems(adapter?.getSelectedItemsById(selectedIds)!!, favWordsRef)
            } else {
                val collectionRef = FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("translatorCollections")
                    .child(collectionName)

                // Remove selected collections from the DB and RecyclerView.
                val selectedIds: List<String> = trackerStr.selection.map { it }
                adapterStr?.removeItems(adapterStr?.getSelectedItemsById(selectedIds)!!, collectionRef)
            }
        }
        mode.finish()
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

    fun finishActionMode() {
        mode?.finish()
    }
}