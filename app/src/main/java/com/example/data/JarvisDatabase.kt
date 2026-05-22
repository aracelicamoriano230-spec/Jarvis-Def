package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChatMessage::class,
        ClassifiedMemory::class,
        ProjectGoal::class,
        AlarmTimer::class,
        LinkedProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun jarvisDao(): JarvisDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_v8_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate default profiles
                        scope.launch(Dispatchers.IO) {
                            // Wait for INSTANCE to be set (max 2 seconds)
                            var count = 0
                            while (INSTANCE == null && count < 20) {
                                kotlinx.coroutines.delay(100)
                                count++
                            }
                            val dao = INSTANCE?.jarvisDao()
                            dao?.insertLinkedProfile(
                                LinkedProfile(platform = "WhatsApp", username = "+54 9 11 1234-5678", isConnected = true, unreadCount = 4, lastNotification = "Mensaje nuevo de Pepper Potts")
                            )
                            dao?.insertLinkedProfile(
                                LinkedProfile(platform = "Email", username = "tony@starkindustries.com", isConnected = true, unreadCount = 12, lastNotification = "Aviso de Seguridad de F.R.I.D.A.Y.")
                            )
                            dao?.insertLinkedProfile(
                                LinkedProfile(platform = "Telegram", username = "iron_boss_85", isConnected = false, unreadCount = 0)
                            )
                            dao?.insertLinkedProfile(
                                LinkedProfile(platform = "Facebook", username = "Tony Starck", isConnected = true, unreadCount = 2, lastNotification = "Solicitud de amistad de Happy Hogan")
                            )

                            // Prepopulate some default targets/goals
                            dao?.insertProjectGoal(
                                ProjectGoal(title = "Construir Mark 86", description = "Refinar nanotecnología para mayor velocidad de ensamblaje holográfico.", category = "Proyecto", status = "ACTIVE")
                            )
                            dao?.insertProjectGoal(
                                ProjectGoal(title = "Estudios Multiverso", description = "Analizar anomalías temporales encontradas por el reactor de fusión cuántica.", category = "Creatividad", status = "PLANNING")
                            )

                            // Prepopulate default alarms/events
                            dao?.insertAlarmTimer(
                                AlarmTimer(type = "ALARM", labelString = "07:00 AM", description = "Rutina Stark de ejercicio diario")
                            )
                            dao?.insertAlarmTimer(
                                AlarmTimer(type = "TIMER", labelString = "10 segundos", description = "Calibración del reactor Arc", isActive = false, durationSeconds = 10)
                            )
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
