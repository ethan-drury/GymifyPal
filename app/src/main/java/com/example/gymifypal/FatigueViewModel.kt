package com.example.gymifypal

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FatigueViewModel(context: Context) : ViewModel() {
    private val dao: UniDao = UniDatabase.getDatabase(context).UniDao()

    fun getAllFatigues() = dao.allFatigues()

    fun updateFatigue(muscleName: String, fatigueLevel: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertFatigue(
                MuscleFatigue(
                    muscleName = muscleName,
                    savedFatigueLevel = fatigueLevel,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

}