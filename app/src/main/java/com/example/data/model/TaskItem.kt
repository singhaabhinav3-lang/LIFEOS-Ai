package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String = "",
    val priority: String = "P2", // "P1" (High), "P2" (Medium), "P3" (Low)
    val dueDate: String = "", // e.g. "Today", "Tomorrow", "2026-10-15"
    val isCompleted: Boolean = false,
    val goalId: String? = null,
    val goalTitle: String? = null,
    val estimatedMinutes: Int = 45,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
