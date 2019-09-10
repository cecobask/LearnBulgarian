package bask.learnbulgarian.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bask.learnbulgarian.R
import bask.learnbulgarian.adapters.WordOfTheDayAdapter
import bask.learnbulgarian.models.WordOfTheDay
import bask.learnbulgarian.utils.SwipeToDeleteCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class WordOfTheDayFavouritesFragment : Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: WordOfTheDayAdapter
    private lateinit var firebaseAuth: FirebaseAuth

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
                        linearLayoutManager = LinearLayoutManager(context)
                        wotdFavouritesRV.layoutManager = linearLayoutManager

                        // Add bottom border to each RV element.
                        wotdFavouritesRV.addItemDecoration(DividerItemDecoration(
                                wotdFavouritesRV.context,
                                linearLayoutManager.orientation))

                        // Link RV and its adapter.
                        adapter = WordOfTheDayAdapter(favouriteWords as ArrayList<WordOfTheDay>)
                        wotdFavouritesRV.adapter = adapter

                        // Callback for RV elements swiping.
                        val swipeHandler = object : SwipeToDeleteCallback(context!!) {
                            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                                // Pass position and db reference to adapter method.
                                adapter.removeAt(viewHolder.adapterPosition, favWordsRef)
                            }
                        }

                        // Attach swipe handler callback to RV.
                        ItemTouchHelper(swipeHandler).attachToRecyclerView(wotdFavouritesRV)
                    }
                }

            })
    }

}