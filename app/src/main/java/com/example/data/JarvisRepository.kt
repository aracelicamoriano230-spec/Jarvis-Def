package com.example.data

import kotlinx.coroutines.flow.Flow

class JarvisRepository(private val jarvisDao: JarvisDao) {
    // Flows mapping
    val allMessagesFlow: Flow<List<ChatMessage>> = jarvisDao.getAllMessagesFlow()
    val classifiedMemoriesFlow: Flow<List<ClassifiedMemory>> = jarvisDao.getClassifiedMemoriesFlow()
    val projectGoalsFlow: Flow<List<ProjectGoal>> = jarvisDao.getProjectGoalsFlow()
    val alarmTimersFlow: Flow<List<AlarmTimer>> = jarvisDao.getAlarmTimersFlow()
    val linkedProfilesFlow: Flow<List<LinkedProfile>> = jarvisDao.getLinkedProfilesFlow()

    // Query categories
    fun getMessagesByCategory(category: String): Flow<List<ChatMessage>> {
        return jarvisDao.getMessagesByCategoryFlow(category)
    }

    suspend fun getAllMessagesDirect(): List<ChatMessage> {
        return jarvisDao.getAllMessagesDirect()
    }

    // Chat write
    suspend fun insertMessage(message: ChatMessage) {
        jarvisDao.insertChatMessage(message)
    }

    suspend fun clearHistory() {
        jarvisDao.clearChatHistory()
    }

    suspend fun deleteMessage(id: Int) {
        jarvisDao.deleteChatMessageById(id)
    }

    // Classified section
    suspend fun insertClassified(memory: ClassifiedMemory) {
        jarvisDao.insertClassifiedMemory(memory)
    }

    suspend fun deleteClassified(id: Int) {
        jarvisDao.deleteClassifiedMemory(id)
    }

    // Projects writing
    suspend fun insertGoal(goal: ProjectGoal) {
        jarvisDao.insertProjectGoal(goal)
    }

    suspend fun deleteGoal(id: Int) {
        jarvisDao.deleteProjectGoal(id)
    }

    // Alarms writing
    suspend fun insertAlarm(alarm: AlarmTimer) {
        jarvisDao.insertAlarmTimer(alarm)
    }

    suspend fun updateAlarm(id: Int, active: Boolean) {
        jarvisDao.updateAlarmStatus(id, active)
    }

    suspend fun deleteAlarm(id: Int) {
        jarvisDao.deleteAlarmTimer(id)
    }

    // Profiles writing
    suspend fun insertProfile(profile: LinkedProfile) {
        jarvisDao.insertLinkedProfile(profile)
    }

    suspend fun updateProfile(profile: LinkedProfile) {
        jarvisDao.updateLinkedProfile(profile)
    }

    suspend fun deleteProfile(id: Int) {
        jarvisDao.deleteProfile(id)
    }
}
