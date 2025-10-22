package com.example.gymifypal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UniDao {
    //Exercise
    @Insert
    fun insert(exercise: Exercise): Long

    @Query("SELECT * FROM Exercise")
    fun allExercises(): Flow<List<Exercise>>

    @Query("DELETE FROM Exercise")
    fun deleteAllExercise()

    @Query("DELETE FROM Exercise WHERE id=:id")
    fun deleteExercise(id: Long)

    //Fatigue
    @Upsert
    suspend fun insertFatigue(fatigue: MuscleFatigue)

    @Query("SELECT * FROM muscle_fatigue")
    fun allFatigues(): Flow<List<MuscleFatigue>>

    @Query("DELETE FROM muscle_fatigue WHERE muscleName = :muscleName")
    suspend fun deleteFatigue(muscleName: String)

    @Query("DELETE FROM muscle_fatigue")
    suspend fun deleteAllFatigues()


}