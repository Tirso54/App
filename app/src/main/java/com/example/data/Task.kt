package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val durationHours: Double = 1.0, // e.g. 1.5 hours
    val category: String, // "trabajo", "estudio", "deporte", "ocio"
    val priority: String, // "alta", "media", "baja"
    val dayOfWeek: Int, // 1 = Lunes, 2 = Martes, ..., 7 = Domingo
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)
