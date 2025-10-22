package com.example.gymifypal

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.sql.Timestamp
import java.util.concurrent.TimeUnit

/*
@Entity(tableName = "muscle_fatigue")
@Serializable
data class MuscleFatigue(
    @PrimaryKey val muscleName: String,
    val savedFatigueLevel: Float,
    val lastUpdatedTimestamp: Long
)

// recovery calc
val MuscleFatigue.currentFatigueLevel: Float
    get() {
        val recoveryRatePerMS = 1.0f / TimeUnit.DAYS.toMillis(1).toFloat()

        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - lastUpdatedTimestamp
        val decrement = recoveryRatePerMS * elapsedTime.toFloat()

        return (savedFatigueLevel - decrement).coerceIn(0.0f,1.0f)
    }
*/
@Entity
@Serializable
data class Exercise(@PrimaryKey(autoGenerate = true) val id: Long=0,
                    val exerciseName: String,
                    val type: String,
                    val muscle: String,
                    val difficulty: String,
                    val week: Int,
                    val sets: Int,
                    val reps: Int
)