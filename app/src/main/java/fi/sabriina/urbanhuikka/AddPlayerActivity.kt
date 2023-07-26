package fi.sabriina.urbanhuikka

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import fi.sabriina.urbanhuikka.roomdb.HuikkaApplication
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.viewmodel.PlayerViewModel
import fi.sabriina.urbanhuikka.viewmodel.PlayerViewModelFactory

class AddPlayerActivity : AppCompatActivity() {

    private lateinit var addPlayerButton: Button
    private lateinit var playerInput: TextInputEditText

    private var selectedImageResId = 0

    private val imageOptions = arrayOf(
        R.drawable.gamer,
        R.drawable.man,
        R.drawable.woman,
        R.drawable.man_1,
        R.drawable.man_2,
        R.drawable.woman_1
    )

    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((application as HuikkaApplication).playerRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_player)

        addPlayerButton = findViewById(R.id.addPlayerButton)
        playerInput = findViewById(R.id.textInputPlayer)
        selectedImageResId = imageOptions[0]

        playerInput.doOnTextChanged { _, _, _, count ->
            addPlayerButton.isEnabled = count != 0
        }

        addPlayerButton.setOnClickListener {
            val name = playerInput.text.toString().replaceFirstChar { it.uppercase() }
            playerViewModel.insert(Player(0, name, selectedImageResId))
            finish()
        }

        val gridView: GridView = findViewById(R.id.imageGridView)
        gridView.adapter = ImageOptionAdapter()
    }

    private inner class ImageOptionAdapter : BaseAdapter() {
        private var selectedItemPosition = 0

        override fun getCount(): Int {
            return imageOptions.size
        }

        override fun getItem(position: Int): Any {
            return imageOptions[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view: View
            val imageView: ImageView
            val checkmarkView: ImageView
            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(applicationContext)
                val gridItemView = layoutInflater.inflate(R.layout.profile_picture, parent, false)
                imageView = gridItemView.findViewById(R.id.imageOptionView)
                checkmarkView = gridItemView.findViewById(R.id.checkmarkImageView)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setPadding(4, 4, 4, 4)
                view = gridItemView
            } else {
                view = convertView
                imageView = view.findViewById(R.id.imageOptionView)
                checkmarkView = view.findViewById(R.id.checkmarkImageView)
            }
            imageView.setImageResource(imageOptions[position])

            imageView.setOnClickListener {
                selectedItemPosition = position
                selectedImageResId = imageOptions[position]
                notifyDataSetChanged()
            }

            if (selectedItemPosition == position) {
                checkmarkView.visibility = View.VISIBLE
            } else {
                checkmarkView.visibility = View.GONE
            }

            return view
        }
    }
}