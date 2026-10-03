package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val category: String, // "Work Habit", "Goal Context", "Energy Pattern", "Preference"
    val content: String,
    val confidence: Float = 0.95f,
    val source: String = "AI Observation", // "AI Observation", "User Note", "Goal Planner"
    val createdAt: Long = System.currentTimeMillis()
)
