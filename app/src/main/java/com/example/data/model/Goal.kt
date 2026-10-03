package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: String, // "Career", "Health", "Engineering", "Finance", "Learning", "Creative"
    val deadline: String, // e.g. "2026-11-30"
    val progress: Float = 0f, // 0 to 100
    val status: String = "active", // "active", "completed", "paused"
    val milestonesJson: String = "[]", // List of Milestone items
    val createdAt: Long = System.currentTimeMillis()
)

data class MilestoneItem(
    val id: String,
    val title: String,
    val targetWeek: String,
    val isDone: Boolean = false
)
