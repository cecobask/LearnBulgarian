package bask.learnbulgarian.utils

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.recyclerview.selection.SelectionTracker
import bask.learnbulgarian.R
import bask.learnbulgarian.adapters.WordOfTheDayAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import timber.log.Timber


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
            // Store item positions as a List of Integers.
            val itemPositions: List<Int> = tracker.selection.map { it.toInt() }
            val favWordsRef = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("favWords")

            // Remove the selected items from the RecyclerView and Firebase DB.
            adapter?.removeItems(ArrayList(itemPositions), favWordsRef)
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