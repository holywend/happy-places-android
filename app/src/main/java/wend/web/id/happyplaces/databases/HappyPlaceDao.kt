package wend.web.id.happyplaces.databases

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HappyPlaceDao {

    @Insert
    suspend fun insert(happyPlaceEntity: HappyPlaceEntity): Long

    @Update
    suspend fun update(happyPlaceEntity: HappyPlaceEntity): Int

    @Delete
    suspend fun delete(happyPlaceEntity: HappyPlaceEntity): Int

    @Query("SELECT * FROM `tbl-happy-place`")
    fun fetchAllPlace(): Flow<List<HappyPlaceEntity>>

    @Query("SELECT * FROM `tbl-happy-place` WHERE id=:id")
    fun fetchPlaceById(id: Int): Flow<List<HappyPlaceEntity>>


}