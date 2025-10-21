package com.example.gymifypal

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymifypal.UniDatabase.Companion.getDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ExerciseDatabaseViewModel(context: Context) : ViewModel() {
    val mDao: UniDao
    init {
        val db = getDatabase(context)
        mDao = db.UniDao()
    }

    fun getAllExercises() = mDao.allExercises()

    fun newExercise(
        exerciseName: String,
        type: String,
        muscle: String,
        difficulty: String,
        week: Int,
        sets: Int,
        reps: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val exercise = Exercise(
                exerciseName = exerciseName,
                type = type,
                muscle = muscle,
                difficulty = difficulty,
                week = week,
                sets = sets,
                reps = reps
            )
            mDao.insert(exercise)
            Log.i("ExerciseVM", "New exercise inserted: $exercise")
        }
    }

    fun deleteExercise(id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            mDao.deleteExercise(id)
        }
    }
}