package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "Trabajo", "Programacion", "Estudio", "Variado"
    val sender: String, // "user" or "jarvis"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val fileAttachmentPath: String? = null,
    val fileMimeType: String? = null
) : Serializable

@Entity(tableName = "classified_memories")
data class ClassifiedMemory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val section: String, // "Trabajo", "Estudio", "Personal"
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "project_goals")
data class ProjectGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "Creatividad", "Meta", "Proyecto"
    val status: String, // "PLANNING", "ACTIVE", "COMPLETED"
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "alarm_timers")
data class AlarmTimer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "ALARM", "TIMER", "CALENDAR"
    val labelString: String, // time "07:30 AM", "10 seconds", "2026-05-24"
    val description: String,
    val isActive: Boolean = true,
    val durationSeconds: Int = 0, // for timers
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "linked_profiles")
data class LinkedProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val platform: String, // "WhatsApp", "Email", "Facebook", "Telegram"
    val username: String,
    val isConnected: Boolean = false,
    val unreadCount: Int = 0,
    val lastNotification: String? = null
) : Serializable
