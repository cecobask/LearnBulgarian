package bask.lingvino.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bask.lingvino.R
import bask.lingvino.adapters.TranslationsAdapter
import bask.lingvino.models.Translation
import bask.lingvino.utils.ActionModeCallback
import bask.lingvino.utils.TranslationsCollectionDetailsLookup
import bask.lingvino.utils.TranslationsCollectionKeyProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import kotlin.collections.ArrayList

class TranslatorFavouritesFragment : Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: TranslationsAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var tracker: SelectionTracker<String>
    private var actionModeCallback: ActionModeCallback? = null
    private lateinit var collectionName: String

    companion object {
        private const val argKey = "collectionName"
        fun newInstance(collectionName: String): TranslatorFavouritesFragment {
            val args = Bundle().apply { putString(argKey, collectionName) }
            return TranslatorFavouritesFragment().apply { arguments = args }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("collectionName").let { collectionName = it!! }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.translator_favs, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        val translatorFavouritesRV = view.findViewById<RecyclerView>(R.id.translatorFavouritesRV)
        val collectionsRef = database.reference
            .child("users")
            .child(firebaseAuth.currentUser!!.uid)
            .child("translatorCollections")
            .child(collectionName)

        collectionsRef.apply {
            addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) =
                    Timber.tag("${collectionName}/translations").d(p0.toException())

                // Fetch translations.
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        val translationsList = mutableListOf<Translation>()
                        p0.children.forEach {
                            val translation = it.getValue(Translation::class.java)!!.apply {
                                id = it.key
                                expanded = false
                            }
                            translationsList.add(translation)
                        }

                        // Add layoutManager to RV.
                        val linearLayoutManager = LinearLayoutManager(context)
                        translatorFavouritesRV.layoutManager = linearLayoutManager

                        // Add bottom border to each RV element.
                        translatorFavouritesRV.addItemDecoration(
                            DividerItemDecoration(
                                translatorFavouritesRV.context,
                                linearLayoutManager.orientation
                            )
                        )

                        // Link RV to its adapter.
                        adapter = TranslationsAdapter(
                            translationsList as ArrayList<Translation>,
                            translatorFavouritesRV,
                            activity
                        )
                        translatorFavouritesRV.adapter = adapter
                        translatorFavouritesRV.setHasFixedSize(true)

                        // SelectionTracker that will allow multiple items to be selected.
                        tracker = SelectionTracker.Builder<String>(
                            "collectionSelection",
                            translatorFavouritesRV,
                            TranslationsCollectionKeyProvider(adapter),
                            TranslationsCollectionDetailsLookup(translatorFavouritesRV),
                            StorageStrategy.createStringStorage()
                        ).withSelectionPredicate(
                            SelectionPredicates.createSelectAnything()
                        ).build()

                        tracker.addObserver(object : SelectionTracker.SelectionObserver<String>() {
                            // Keep track of selected items.
                            override fun onSelectionChanged() {
                                super.onSelectionChanged()
                                if (tracker.hasSelection() && actionModeCallback == null) {
                                    // Create callback for ActionMode and initialize it.
                                    actionModeCallback = ActionModeCallback(collectionName)
                                    actionModeCallback?.startActionMode(
                                        view,
                                        R.menu.action_items,
                                        tracker,
                                        adapter
                                    )
                                } else if (tracker.hasSelection() && actionModeCallback != null) {
                                    // Update ActionMode menu entries.
                                    actionModeCallback?.invalidateActionMode()
                                } else if (!tracker.hasSelection() && actionModeCallback != null) {
                                    // Destroy ActionMode if there's no selection.
                                    actionModeCallback?.finishActionMode()
                                    actionModeCallback = null
                                }
                            }
                        })
                        // Attach SelectionTracker to the adapter.
                        adapter.tracker = tracker
                    }
                }

            })

            // Listen for changes in current collection.
            addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                // Kill the fragment if there are no more Translations.
                override fun onDataChange(p0: DataSnapshot) {
                    if (!p0.exists()) {
                        fragmentManager?.popBackStack()
                    }
                }

            })
        }
    }

    override fun onResume() {
        super.onResume()
        // Change toolbar title.
        (activity as? AppCompatActivity)?.supportActionBar?.title = collectionName
    }
}