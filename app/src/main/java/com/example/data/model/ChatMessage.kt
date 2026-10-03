package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val role: String, // "user", "assistant"
    val content: String,
    val relatedEntityType: String? = null, // "goal", "task", "plan", null
    val timestamp: Long = System.currentTimeMillis()
)
