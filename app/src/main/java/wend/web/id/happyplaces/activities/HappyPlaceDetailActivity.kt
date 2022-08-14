package wend.web.id.happyplaces.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import wend.web.id.happyplaces.databases.HappyPlaceEntity
import wend.web.id.happyplaces.databinding.ActivityHappyPlaceDetailBinding

class HappyPlaceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHappyPlaceDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var happyPlaceEntity: HappyPlaceEntity? = null
        if (intent.hasExtra(MainActivity.HAPPY_PLACE_ENTITY)) {
            happyPlaceEntity = intent.getParcelableExtra(MainActivity.HAPPY_PLACE_ENTITY)
        }
        if (happyPlaceEntity != null) {
            // toolbar
            setSupportActionBar(binding.tbAddPlace)
            if (supportActionBar != null) {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.title = happyPlaceEntity.title
            }
            binding.tbAddPlace.setNavigationOnClickListener {
                onBackPressed()
            }
            binding.ivPlaceImage.setImageURI(Uri.parse(happyPlaceEntity.image))
            binding.tvDescription.text = happyPlaceEntity.description
            binding.tvLocation.text = happyPlaceEntity.location
//            binding.tvPlaceName.text = happyPlaceEntity.placeName
//            binding.tvPlaceDescription.text = happyPlaceEntity.placeDescription
//            binding.tvPlaceLatitude.text = happyPlaceEntity.placeLatitude
//            binding.tvPlaceLongitude.text = happyPlaceEntity.placeLongitude
        }
    }
}