package bask.learnbulgarian.fragments

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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bask.learnbulgarian.R
import bask.learnbulgarian.adapters.WordOfTheDayAdapter
import bask.learnbulgarian.models.WordOfTheDay
import bask.learnbulgarian.utils.ActionModeCallback
import bask.learnbulgarian.utils.MyItemDetailsLookup
import bask.learnbulgarian.utils.MyItemKeyProvider
import bask.learnbulgarian.utils.SwipeToDeleteCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class WordOfTheDayFavouritesFragment : Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: WordOfTheDayAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var tracker: SelectionTracker<Long>
    private var actionModeCallback: ActionModeCallback? = null

    companion object {
        fun newInstance(): WordOfTheDayFavouritesFragment {
            return WordOfTheDayFavouritesFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.wordofthedayfavourites, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Change toolbar title.
        (activity as? AppCompatActivity)?.supportActionBar?.title =
            resources.getString(R.string.favWords)

        database = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        val wotdFavouritesRV = view.findViewById<RecyclerView>(R.id.wotdFavouritesRV)

        // Retrieve user's favourite words.
        val favWordsRef = database.reference
            .child("users")
            .child(firebaseAuth.currentUser!!.uid)
            .child("favWords")

        val word1 = WordOfTheDay("1-1-2011","котка")
        val word2 = WordOfTheDay("1-1-2013","мама")
        val word3 = WordOfTheDay("1-1-2009","тати")

        favWordsRef.child("1-1-2011").setValue(word1)
        favWordsRef.child("1-1-2013").setValue(word2)
        favWordsRef.child("1-1-2009").setValue(word3)

        favWordsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) =
                    Timber.tag("favWords").d(p0.toException())

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        // A list to store all favourite words of the user.
                        val favouriteWords = mutableListOf<WordOfTheDay>()
                        p0.children.forEach {
                            // Add each object to the list.
                            val word = it.getValue(WordOfTheDay::class.java)!!
                            favouriteWords.add(word)
                        }

                        // Sort the words in descending order (most recent dates on top).
                        favouriteWords.sortDescending()

                        // Add layoutManager to RV.
                        val linearLayoutManager = LinearLayoutManager(context)
                        wotdFavouritesRV.layoutManager = linearLayoutManager

                        // Add bottom border to each RV element.
                        wotdFavouritesRV.addItemDecoration(DividerItemDecoration(
                                wotdFavouritesRV.context,
                                linearLayoutManager.orientation))

                        // Link RV to its adapter.
                        adapter = WordOfTheDayAdapter(favouriteWords as ArrayList<WordOfTheDay>, wotdFavouritesRV)
                        wotdFavouritesRV.adapter = adapter

                        // Callback for RecyclerView elements swiping.
                        val swipeHandler = object : SwipeToDeleteCallback(context!!) {
                            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                                // Get swiped element's id.
                                val selectedIds: List<String> = listOf(viewHolder.itemId.toString())
                                // Convert swiped id to WordOfTheDay object and delete it.
                                adapter.removeItems(adapter.getSelectedItemsById(selectedIds), favWordsRef)
                            }
                        }
                        val itemTouchHelper = ItemTouchHelper(swipeHandler)

                        // Attach swipe handler to RecyclerView.
                        itemTouchHelper.attachToRecyclerView(wotdFavouritesRV)

                        // SelectionTracker that will allow multiple items to be selected.
                        tracker = SelectionTracker.Builder<Long>(
                            "wordSelection",
                            wotdFavouritesRV,
                            MyItemKeyProvider(wotdFavouritesRV),
                            MyItemDetailsLookup(wotdFavouritesRV),
                            StorageStrategy.createLongStorage()
                        ).withSelectionPredicate(
                            SelectionPredicates.createSelectAnything()
                        ).build()

                        tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
                            override fun onSelectionChanged() {
                                super.onSelectionChanged()
                                if (tracker.hasSelection() && actionModeCallback == null) {
                                    // Disable swipe to delete in RecyclerView.
                                    swipeHandler.setSwipingStatus(false)

                                    // Create callback for ActionMode and initialize it.
                                    actionModeCallback = ActionModeCallback()
                                    actionModeCallback?.startActionMode(
                                        view,
                                        R.menu.action_items,
                                        tracker,
                                        adapter
                                    )
                                } else if (!tracker.hasSelection() && actionModeCallback != null) {
                                    // Destroy ActionMode if there's no selection.
                                    actionModeCallback?.finishActionMode()
                                    actionModeCallback = null

                                    // Enable swipe to delete in RecyclerView.
                                    swipeHandler.setSwipingStatus(true)
                                }

                            }
                        })
                        // Attach SelectionTracker to the adapter.
                        adapter.tracker = tracker
                    }
                }

            })

        favWordsRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()) {
                    fragmentManager?.popBackStack()
                }
            }

        })
    }

}