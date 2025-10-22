package com.example.gymifypal

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.gymifypal.UniDatabase.Companion.getDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Had to extend AndroidViewModel and include a factory to stop crashing due to suspected mem leak
class FatigueDatabaseViewModel (context: Context) : ViewModel(){

    val uniDao: UniDao
    init {
        val db = getDatabase(context)
        uniDao = db.UniDao()
    }
    fun getAllFatigueEntities(): Flow<List<MuscleFatigue>>{
        return uniDao.getAllFatigue()
    }

    fun getAllMuscleFatigueMaps(onMuscleClick: (String) -> Unit): Flow<List<MuscleFatigueMap>> {
        return getAllFatigueEntities().map { fatigueList ->
            val fatigueMap = fatigueList.associateBy { it.muscleName }
            MuscleGroup.entries.map { muscleGroup ->
                val entity = fatigueMap[muscleGroup.name]
                val currentFatigue = entity?.currentFatigueLevel ?: 0.0f

                MuscleFatigueMap(
                    name = muscleGroup.displayName,
                    drawableResId = getDrawableResId(muscleGroup),
                    fatigueLevel = currentFatigue,
                    onClick = onMuscleClick

                )
            }
        }
    }

    private fun getDrawableResId(muscleGroup: MuscleGroup) : Int {
        return when (muscleGroup) {
            MuscleGroup.Abdominal -> R.drawable.abs
            MuscleGroup.Biceps -> R.drawable.biceps
            MuscleGroup.Chest -> R.drawable.chest
            MuscleGroup.FrontDeltoid -> R.drawable.front_deltoids
            MuscleGroup.SideDeltoid -> R.drawable.side_deltoids
            MuscleGroup.FrontForearm -> R.drawable.front_forearms
            MuscleGroup.Quads -> R.drawable.quads
            MuscleGroup.Calves -> R.drawable.calves
            MuscleGroup.Glutes -> R.drawable.glutes
            MuscleGroup.Hamstring -> R.drawable.hamstrings
            MuscleGroup.RearDeltoid -> R.drawable.rear_deltoids
            MuscleGroup.Latissimus -> R.drawable.lats
            MuscleGroup.RearTraps -> R.drawable.rear_traps
            MuscleGroup.Triceps -> R.drawable.triceps
        }
    }
}