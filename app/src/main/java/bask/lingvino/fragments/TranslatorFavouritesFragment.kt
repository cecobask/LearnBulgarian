package bask.lingvino.fragments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
import bask.lingvino.utils.TranslationFilter
import bask.lingvino.utils.TranslationsCollectionDetailsLookup
import bask.lingvino.utils.TranslationsCollectionKeyProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import kotlin.collections.ArrayList

class TranslatorFavouritesFragment : Fragment(), View.OnClickListener {

    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: TranslationsAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var tracker: SelectionTracker<String>
    private var actionModeCallback: ActionModeCallback? = null
    private lateinit var collectionName: String
    private lateinit var translationFilter: TranslationFilter
    private lateinit var langFiltersCG: ChipGroup
    private lateinit var fromLangChip: Chip
    private lateinit var toLangChip: Chip
    private lateinit var clearFiltersIV: ImageView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var translationsList: MutableList<Translation>

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
        setHasOptionsMenu(true)

        // Init widgets.
        langFiltersCG = view.findViewById(R.id.langFiltersCG)
        fromLangChip = view.findViewById(R.id.fromLangChip)
        toLangChip = view.findViewById(R.id.toLangChip)
        clearFiltersIV = view.findViewById(R.id.clearFiltersIV)
        sharedPref = activity!!.getSharedPreferences("learnBulgarian", 0)

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
                        translationsList = mutableListOf()
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
                                    val selection = tracker.selection.map { it }
                                    val targetLang = adapter.getSelectedItemsById(selection)[0].targetLang
                                    actionModeCallback = ActionModeCallback(
                                        collectionName, context!!, targetLang
                                    )
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

                        translationFilter = TranslationFilter(adapter,
                            translationsList as ArrayList<Translation>
                        )
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

        // Set click listeners.
        fromLangChip.setOnClickListener(this)
        toLangChip.setOnClickListener(this)
        clearFiltersIV.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)

        // Make SearchView take up the whole width of the ActionBar.
        val searchView = menu.findItem(R.id.action_search)?.actionView as SearchView
        searchView.maxWidth = Int.MAX_VALUE
        searchView.queryHint = "Search for translations..."

        // Change the colour of clear input button and hint text colour of SearchView.
        val searchClose: ImageView = searchView.findViewById(androidx.appcompat.R.id.search_close_btn)
        searchClose.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP)
        val searchEditText: EditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        searchEditText.setHintTextColor(Color.GRAY)

        // Listen for user input into SearchView and filter based on the query.
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                // Perform filtering based on input value.
                translationFilter.filter(query)
                return true
            }

        })

        super.onCreateOptionsMenu(menu,inflater)
    }

    override fun onResume() {
        super.onResume()
        // Change toolbar title.
        (activity as? AppCompatActivity)?.supportActionBar?.title = collectionName
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fromLangChip -> {
                showLanguageFilterDialog(fromLangChip)
            }
            R.id.toLangChip -> {
                showLanguageFilterDialog(toLangChip)
            }
            R.id.clearFiltersIV -> {
                clearFilters()
            }
        }
    }

    // Displays a dialog, where the user can select languages to filter by.
    private fun showLanguageFilterDialog(chip: Chip) {
        MaterialDialog(context!!).show {
            // Only check the chip if its value is not "All languages".
            chip.isChecked = chip.text != "All languages"
            showHideClearFiltersIV()
            title(text = "Pick a language filter:")
            listItemsSingleChoice(
                items = listOf("English", "Bulgarian", "Spanish", "Russian", "All languages")
            ) { _, _, text ->
                chip.text = text // Update the text of the relevant chip.
                // Perform filtering of phrases based on the values of the chips.
                translationFilter.filterBySourceAndTarget(
                    fromLangChip.text.toString(),
                    toLangChip.text.toString()
                )
                // Only check the chip if its value is not "All languages".
                chip.isChecked = text != "All languages"
                showHideClearFiltersIV()
            }
        }
    }

    private fun showHideClearFiltersIV() {
        // Hide the view if there are no filters applied.
        clearFiltersIV.visibility =
            if (fromLangChip.text != "All languages" || toLangChip.text != "All languages") View.VISIBLE
            else View.GONE
    }

    private fun clearFilters() {
        // Modify chips.
        fromLangChip.apply {
            text = getString(R.string.allLanguagesFilter)
            isChecked = false
        }
        toLangChip.apply {
            text = getString(R.string.allLanguagesFilter)
            isChecked = false
        }

        // Reset filter results.
        translationFilter.filterBySourceAndTarget(
            fromLangChip.text.toString(), toLangChip.text.toString()
        )

        showHideClearFiltersIV()
    }
}