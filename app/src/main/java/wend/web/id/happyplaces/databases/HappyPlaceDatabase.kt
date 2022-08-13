package wend.web.id.happyplaces.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HappyPlaceEntity::class], version = 1, exportSchema = false)
abstract class HappyPlaceDatabase: RoomDatabase() {
    abstract fun happyPlaceDao():HappyPlaceDao

    companion object{
        @Volatile
        private var INSTANCE: HappyPlaceDatabase? = null

        fun getInstance(context: Context):HappyPlaceDatabase{
            // use synchronized to make sure only one instance exist
            synchronized(this){
                var instance = INSTANCE
                // make sure there is only one database
                // create new database if it does not exist yet
                // or return the existing database instance
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        HappyPlaceDatabase::class.java,
                        "db_history"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }}