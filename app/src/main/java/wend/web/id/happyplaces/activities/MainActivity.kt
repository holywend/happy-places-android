package wend.web.id.happyplaces.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import wend.web.id.happyplaces.HappyPlacesApp
import wend.web.id.happyplaces.databases.HappyPlaceDao
import wend.web.id.happyplaces.databases.HappyPlaceEntity
import wend.web.id.happyplaces.databases.HappyPlacesAdapter
import wend.web.id.happyplaces.databinding.ActivityMainBinding
import wend.web.id.happyplaces.utils.SwipeToDeleteCallBack
import wend.web.id.happyplaces.utils.SwipeToEditCallBack

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        const val HAPPY_PLACE_ENTITY = "HAPPY_PLACE_ENTITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddPlacesActivity::class.java)
            startActivity(intent)
        }
        val placeDao = (application as HappyPlacesApp).db.happyPlaceDao()
        getAllPlace(placeDao)
    }

    private fun getAllPlace(placeDao: HappyPlaceDao) {
        lifecycleScope.launch {
            placeDao.fetchAllPlace().collect { items ->
                if (items.isNotEmpty()) {
                    binding.rvPlace.layoutManager = LinearLayoutManager(this@MainActivity)
                    val happyPlacesAdapter = HappyPlacesAdapter(ArrayList(items))
                    binding.rvPlace.adapter = happyPlacesAdapter
                    // passing override method to adapter
                    happyPlacesAdapter.setOnClickListener(object :
                        HappyPlacesAdapter.OnClickListener {
                        override fun onClick(position: Int, entity: HappyPlaceEntity) {
                            val intent =
                                Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                            intent.putExtra(HAPPY_PLACE_ENTITY, entity)
                            startActivity(intent)
                        }
                    })
                    val editSwipeHandler = object : SwipeToEditCallBack(this@MainActivity) {
                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val adapter = binding.rvPlace.adapter as HappyPlacesAdapter
                            adapter.notifyEditItem(
                                this@MainActivity,
                                viewHolder.adapterPosition,
                                ADD_PLACE_ACTIVITY_REQUEST_CODE
                            )
                        }
                    }
                    val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
                    editItemTouchHelper.attachToRecyclerView(binding.rvPlace)

                    val deleteSwipeHandler =
                        object : SwipeToDeleteCallBack(this@MainActivity) {
                            override fun onSwiped(
                                viewHolder: RecyclerView.ViewHolder,
                                direction: Int
                            ) {
                                val adapter = binding.rvPlace.adapter as HappyPlacesAdapter
                                val deleteDialog = AlertDialog.Builder(this@MainActivity)
                                deleteDialog.setTitle("Delete")
                                deleteDialog.setMessage("Are you sure want to delete this place?")
                                deleteDialog.setPositiveButton("Yes") { _, _ ->
                                    lifecycleScope.launch{
                                        adapter.removeAt(applicationContext,viewHolder.adapterPosition)
                                    }
                                }
                                deleteDialog.setNegativeButton("No") { dialog, _ ->
                                    adapter.notifyItemChanged(viewHolder.adapterPosition)
                                    dialog.dismiss()
                                }
                                deleteDialog.show()
                            }
                        }
                    val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
                    deleteItemTouchHelper.attachToRecyclerView(binding.rvPlace)

                }
            }
        }
    }
}