package fr.uparis.forgetmenot


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Plant::class], version = 6)
abstract class PlantDB : RoomDatabase(){
    abstract fun plantDao() : PlantDao

    companion object {
        @Volatile
        private var instance : PlantDB? = null

        fun getDatabase (context : Context) : PlantDB {
            if (instance != null) {
                return instance!!
            }
            val db = Room.databaseBuilder(context.applicationContext, PlantDB::class.java, "Plants").build()
            instance = db
            return instance!!
        }
    }
}