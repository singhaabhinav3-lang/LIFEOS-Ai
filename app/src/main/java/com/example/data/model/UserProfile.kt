package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val productivityStyle: String = "Deep Work Specialist", // "Deep Work", "Pomodoro Sprints", "Agile Flow", "Time Blocker"
    val focusAreas: String = "Engineering, AI Architecture, Health, High Performance",
    val primeHours: String = "08:00 - 12:00 (Peak Morning)",
    val dailyTargetHours: Int = 6,
    val isOnboarded: Boolean = true,
    val themePreference: String = "dark", // "dark", "light", "system"
    val isDemoMode: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
