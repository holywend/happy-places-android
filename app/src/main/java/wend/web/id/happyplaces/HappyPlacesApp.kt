package wend.web.id.happyplaces

import android.app.Application
import wend.web.id.happyplaces.databases.HappyPlaceDatabase

class HappyPlacesApp: Application() {
    val db by lazy {
        HappyPlaceDatabase.getInstance(this)
    }
}