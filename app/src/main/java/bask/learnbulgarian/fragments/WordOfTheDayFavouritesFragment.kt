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

        val word1 = WordOfTheDay("20-9-2019","кокошка", "kokoshka",
            "noun", "A female bird, especially the adult female chicken.",
            "The term hen-cocks is, in consequence, often applied to them; but although the sickle feathers are thus modified, no bird possesses higher courage, or a more gallant carriage.",
            "Вследствие на това терминът кокошки петли често се прилага към тях; но въпреки че сърповите пера са модифицирани по този начин, никоя птица не притежава по-голяма смелост или по-галантна карета.",
            "https://storage.googleapis.com/learnbulgarian-8e7ea.appspot.com/20-9-2019.mpeg")
        val word2 = WordOfTheDay("19-9-2019","видра","vidra","noun",
            "Any of various aquatic or semiaquatic carnivorous mammals of the mustelid subfamily Lutrinae, having webbed feet and dense, dark brown fur.",
            "The terminally cute sea otter is a marine weasel into rough sex.",
            "Крайната сладка морска вид е морска невестулка в груб секс.",
            "https://storage.googleapis.com/learnbulgarian-8e7ea.appspot.com/19-9-2019.mpeg")
        val word3 = WordOfTheDay("18-9-2019","щраус", "shtraus", "noun",
            "Either of two large, swift-running flightless birds (Struthio camelus or S. molybdophanes) of Africa, characterized by a long bare neck, small head, and two-toed feet. Ostriches are the largest living birds.",
            "Then he began to lose his birds by accident, by the destructive propensities of the goblin and a vicious old hen or two; and lastly, some kind of epidemic, which they dubbed ostrich chicken-pox, carried the young birds off wholesale.",
            "Тогава той започна да губи птиците си случайно, от разрушителните склонности на таласъма и порочна стара кокошка или две; и накрая, някаква епидемия, която те нарекоха щраусова шарка, отнесе младите птици на едро.",
            "https://storage.googleapis.com/learnbulgarian-8e7ea.appspot.com/18-9-2019.mpeg")

        favWordsRef.child("20-9-2019").setValue(word1)
        favWordsRef.child("19-9-2019").setValue(word2)
        favWordsRef.child("18-9-2019").setValue(word3)

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
                        adapter = WordOfTheDayAdapter(favouriteWords as ArrayList<WordOfTheDay>, wotdFavouritesRV, fragmentManager)
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