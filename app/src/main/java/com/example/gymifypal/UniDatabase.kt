package com.example.gymifypal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Callback
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(entities = [Exercise::class], version = 1)
abstract class UniDatabase : RoomDatabase() {
    abstract fun UniDao(): UniDao
    companion object {
        @Volatile
        private var Instance: UniDatabase? = null
        fun getDatabase(context: Context): UniDatabase {
            return Instance ?: synchronized(this) {
                val tempInstance = Room.databaseBuilder(
                    context.applicationContext,
                    UniDatabase::class.java,
                    "uni_database"
                ).build()
                Instance = tempInstance
                tempInstance
            }
        }
    }
}