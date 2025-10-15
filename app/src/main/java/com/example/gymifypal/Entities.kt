package com.example.gymifypal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Exercise(@PrimaryKey(autoGenerate = true) val id: Long=0,
                    val exerciseName: String,
                    val type: String,
                    val muscle: String,
                    val difficulty: String,
                    val week: Int,
                    val sets: Int,
                    val reps: Int)