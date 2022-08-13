package wend.web.id.happyplaces.databases

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl-happy-place")
data class HappyPlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val image: String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
)
