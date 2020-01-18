package bask.lingvino.utils

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.recyclerview.selection.SelectionTracker
import bask.lingvino.R
import bask.lingvino.adapters.WordOfTheDayAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


/**
 * Helper class for ActionMode.
 */
class ActionModeCallback : ActionMode.Callback {

    // Store values when ActionMode is created.
    private lateinit var tracker: SelectionTracker<Long>
    private var mode: ActionMode? = null
    @MenuRes
    private var menuResId: Int = 0
    private var adapter: WordOfTheDayAdapter? = null

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
        tracker.clearSelection()
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        // Delete button from the ActionMode menu clicked.
        if (item.itemId == R.id.deleteItems) {
            val favWordsRef = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("favWords")

            // Remove selected words from the DB and RecyclerView.
            val selectedIds: List<String> = tracker.selection.map { it.toString() }
            adapter?.removeItems(adapter?.getSelectedItemsById(selectedIds)!!, favWordsRef)
        }
        mode.finish()
        return true
    }

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

    fun finishActionMode() {
        mode?.finish()
    }
}