package fi.sabriina.urbanhuikka

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import fi.sabriina.urbanhuikka.player.*


class SetPlayersActivity : AppCompatActivity() {
    private lateinit var fab: FloatingActionButton
    private lateinit var playerInput: TextInputEditText

    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((application as PlayersApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_players)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = PlayerListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        playerViewModel.allPlayers.observe(this, Observer { players ->
            // Update the cached copy of the words in the adapter.
            players?.let { adapter.submitList(it) }
        })
        playerInput = findViewById(R.id.textInputPlayer)
        fab = findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            val name = playerInput.text.toString().replaceFirstChar { it.uppercase() }
            val player = Player(name)
            playerViewModel.insert(player)
            playerInput.text?.clear()
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called
                // when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                // below line is to get the position
                // of the item at that position.
                val position = viewHolder.adapterPosition

                // this method is called when we swipe our item to right direction.
                // on below line we are getting the item at a particular position.
                val deletedCourse = adapter.getItemId(position)

                val list = adapter.currentList
                val pelaaja = list[position]


                // this method is called when item is swiped.
                // below line is to remove item from our array list.
                playerViewModel.delete(pelaaja)

                // below line is to display our snackbar with action.
                // below line is to display our snackbar with action.
                // below line is to display our snackbar with action.
                Snackbar.make(recyclerView, "Deleted " + pelaaja.name, Snackbar.LENGTH_LONG)
                    .setAction(
                        "Undo",
                        View.OnClickListener {
                            playerViewModel.insert(pelaaja)
                        }).show()
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(recyclerView)
    }
}