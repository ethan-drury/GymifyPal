package com.example.gymifypal

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(entities = [Exercise::class, MuscleFatigue::class], version = 2)
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
        }

        private val roomDatabaseCallback: Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                CoroutineScope(Dispatchers.IO).launch {
                    val dao: UniDao = Instance!!.UniDao()
                    dao.deleteAllExercise()

                    val insertAbdominal = dao.insert(
                        Exercise(
                            exerciseName = "Crunches",
                            type = "Strength",
                            muscle = "Abdominal",
                            difficulty = "Easy",
                            week = 1,
                            sets = 3,
                            reps = 15
                        )
                    )

                    val insertBiceps = dao.insert(
                        Exercise(
                            exerciseName = "Bicep Curl",
                            type = "Strength",
                            muscle = "Biceps",
                            difficulty = "Intermediate",
                            week = 1,
                            sets = 3,
                            reps = 12
                        )
                    )

                    val insertChest = dao.insert(
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

                    val insertFrontDeltoid = dao.insert(
                        Exercise(
                            exerciseName = "Front Raise",
                            type = "Strength",
                            muscle = "Front Deltoid",
                            difficulty = "Intermediate",
                            week = 1,
                            sets = 3,
                            reps = 10
                        )
                    )

                    val insertSideDeltoid = dao.insert(
                        Exercise(
                            exerciseName = "Lateral Raise",
                            type = "Strength",
                            muscle = "Side Deltoid",
                            difficulty = "Intermediate",
                            week = 1,
                            sets = 3,
                            reps = 12
                        )
                    )

                    val insertFrontForearm = dao.insert(
                        Exercise(
                            exerciseName = "Wrist Curl",
                            type = "Strength",
                            muscle = "Front Forearm",
                            difficulty = "Easy",
                            week = 1,
                            sets = 3,
                            reps = 15
                        )
                    )

                    val insertQuadriceps = dao.insert(
                        Exercise(
                            exerciseName = "Squat",
                            type = "Strength",
                            muscle = "Quadriceps",
                            difficulty = "Intermediate",
                            week = 1,
                            sets = 4,
                            reps = 10
                        )
                    )

                    val insertCalves = dao.insert(
                        Exercise(
                            exerciseName = "Calf Raise",
                            type = "Strength",
                            muscle = "Calves",
                            difficulty = "Easy",
                            week = 1,
                            sets = 3,
                            reps = 15
                        )
                    )

                    val insertGlutes = dao.insert(
                        Exercise(
                            exerciseName = "Glute Bridge",
                            type = "Strength",
                            muscle = "Glutes",
                            difficulty = "Easy",
                            week = 1,
                            sets = 3,
                            reps = 12
                        )
                    )

                    val insertHamstring = dao.insert(
                        Exercise(
                            exerciseName = "Leg Curl",
                            type = "Strength",
                            muscle = "Hamstring",
                            difficulty = "Intermediate",
                            week = 1,
                            sets = 3,
                            reps = 10
                        )
                    )

                    val insertRearDeltoid = dao.insert(
                        Exercise(
                            exerciseName = "Reverse Fly",
                            type = "Strength",
                            muscle = "Rear Deltoid",
                            difficulty = "Intermediate",
                            week = 1,
                            sets = 3,
                            reps = 12
                        )
                    )

                    val insertLatissimus = dao.insert(
                        Exercise(
                            exerciseName = "Pull-up",
                            type = "Strength",
                            muscle = "Latissimus (Lats)",
                            difficulty = "Advanced",
                            week = 1,
                            sets = 3,
                            reps = 8
                        )
                    )

                    val insertRearTraps = dao.insert(
                        Exercise(
                            exerciseName = "Shrugs",
                            type = "Strength",
                            muscle = "Rear Traps",
                            difficulty = "Intermediate",
                            week = 1,
                            sets = 3,
                            reps = 12
                        )
                    )

                    val insertTriceps = dao.insert(
                        Exercise(
                            exerciseName = "Tricep Dips",
                            type = "Strength",
                            muscle = "Triceps",
                            difficulty = "Intermediate",
                            week = 2,
                            sets = 3,
                            reps = 10
                        )
                    )

                    MuscleGroup.entries.forEach { muscle ->
                        dao.insertFatigue(
                            MuscleFatigue(
                                muscleName = muscle.displayName,
                                savedFatigueLevel = 0.0f,
                                lastUpdatedTimestamp = System.currentTimeMillis()
                            )
                        )
                    }

                    Log.i("db", "Database and Fatigue Populated")
                }
            }
        }
    }
}