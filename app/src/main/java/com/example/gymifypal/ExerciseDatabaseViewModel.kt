package com.example.gymifypal

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.gymifypal.UniDatabase.Companion.getDatabase


class ExerciseDatabaseViewModel(context: Context) : ViewModel() {
    val mDao: UniDao
    init {
        val db = getDatabase(context)
        mDao = db.UniDao()
    }

    fun getAllExercises() = mDao.allExercises()
}