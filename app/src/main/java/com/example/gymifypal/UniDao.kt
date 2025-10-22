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



}