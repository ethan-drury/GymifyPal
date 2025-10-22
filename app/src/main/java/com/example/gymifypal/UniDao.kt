package com.example.gymifypal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UniDao {
    @Insert
    fun insert(exercise: Exercise): Long

    @Query("SELECT * FROM Exercise")
    fun allExercises(): Flow<List<Exercise>>

    @Query("DELETE FROM Exercise")
    fun deleteAllExercise()

    @Query("DELETE FROM Exercise WHERE id=:id")
    fun deleteExercise(id: Long)

    // Fatigue Statements:

    //Inserts new fatigue or replaces if there's already one there
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun replaceFatigue(fatigue: MuscleFatigue)

    // Gets latest fatigue for one
    @Query("SELECT * FROM muscle_fatigue WHERE muscleName = :muscleName")
    fun getFatigue(muscleName: String): Flow<MuscleFatigue?>

    // Gets latest fatigue for all
    @Query("SELECT * FROM muscle_fatigue")
    fun getAllFatigue(): Flow<List<MuscleFatigue>>

}