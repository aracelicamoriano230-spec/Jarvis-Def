package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    // Chat Message operations
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE category = :category ORDER BY timestamp ASC")
    fun getMessagesByCategoryFlow(category: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    suspend fun getAllMessagesDirect(): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteChatMessageById(id: Int)

    // Classified Memory operations
    @Query("SELECT * FROM classified_memories ORDER BY timestamp DESC")
    fun getClassifiedMemoriesFlow(): Flow<List<ClassifiedMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassifiedMemory(memory: ClassifiedMemory)

    @Query("DELETE FROM classified_memories WHERE id = :id")
    suspend fun deleteClassifiedMemory(id: Int)

    // Project Goals operations
    @Query("SELECT * FROM project_goals ORDER BY timestamp DESC")
    fun getProjectGoalsFlow(): Flow<List<ProjectGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectGoal(goal: ProjectGoal)

    @Query("DELETE FROM project_goals WHERE id = :id")
    suspend fun deleteProjectGoal(id: Int)

    // Alarm/Timer operations
    @Query("SELECT * FROM alarm_timers ORDER BY timestamp DESC")
    fun getAlarmTimersFlow(): Flow<List<AlarmTimer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarmTimer(alarm: AlarmTimer)

    @Query("UPDATE alarm_timers SET isActive = :active WHERE id = :id")
    suspend fun updateAlarmStatus(id: Int, active: Boolean)

    @Query("DELETE FROM alarm_timers WHERE id = :id")
    suspend fun deleteAlarmTimer(id: Int)

    // Social accounts linked profiles
    @Query("SELECT * FROM linked_profiles")
    fun getLinkedProfilesFlow(): Flow<List<LinkedProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinkedProfile(profile: LinkedProfile)

    @Update
    suspend fun updateLinkedProfile(profile: LinkedProfile)

    @Query("DELETE FROM linked_profiles WHERE id = :id")
    suspend fun deleteProfile(id: Int)
}
