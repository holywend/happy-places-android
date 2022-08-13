package wend.web.id.happyplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import wend.web.id.happyplaces.HappyPlacesApp
import wend.web.id.happyplaces.databases.HappyPlaceDao
import wend.web.id.happyplaces.databases.HappyPlacesAdapter
import wend.web.id.happyplaces.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

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
                    Log.e("get all", items.toString())
                    binding.rvPlace.layoutManager = LinearLayoutManager(this@MainActivity)
                    val happyPlacesAdapter = HappyPlacesAdapter(ArrayList(items))
                    binding.rvPlace.adapter = happyPlacesAdapter
                }
            }
        }
    }
}