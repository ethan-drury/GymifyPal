package com.example.gymifypal

import android.content.Context
import android.util.Log
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
                Room.databaseBuilder(context.applicationContext, UniDatabase::class.java, "uni_database")
                    .addCallback(roomDatabaseCallback)
                    .build()
                    .also { Instance = it }
            }

            // I need this below code for when we don't want to forcefully insert data
            // I am just inserting data forcefully for testing purposes.
            // So please don't delete :) or owe me campus kebab

//            return Instance ?: synchronized(this) {
//                val tempInstance = Room.databaseBuilder(
//                    context.applicationContext,
//                    UniDatabase::class.java,
//                    "uni_database"
//                ).build()
//                Instance = tempInstance
//                tempInstance
//            }
        }

        private val roomDatabaseCallback: Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                CoroutineScope(Dispatchers.IO).launch {
                    val dao: UniDao = Instance!!.UniDao()
                    dao.deleteExercise()
                    val test = dao.insert(
                        Exercise(
                            exerciseName = "Push-up",
                            type = "Strength",
                            muscle = "Chest",
                            difficulty = "Easy",
                            week = 1,
                            sets = 3,
                            reps = 12
                        )
                    )

                    val test2 = dao.insert(
                        Exercise(
                            exerciseName = "Bench Press",
                            type = "Strength",
                            muscle = "Chest",
                            difficulty = "Intermediate",
                            week = 1,
                            sets = 3,
                            reps = 8
                        )
                    )
                    Log.i("db", "Database Populated")
                }
            }
        }
    }
}