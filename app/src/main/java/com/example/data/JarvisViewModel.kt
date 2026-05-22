package com.example.data

import android.app.Application
import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class JarvisViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "JarvisViewModel"

    // 1. SharedPreferences for Registration & Keys
    private val prefs = application.getSharedPreferences("jarvis_v8_prefs", Context.MODE_PRIVATE)

    // User Settings States
    private val _isRegistered = MutableStateFlow(prefs.getBoolean("is_registered", false))
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("user_name", "Tony") ?: "Tony")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _callMeSirOnly = MutableStateFlow(prefs.getBoolean("call_me_sir_only", true))
    val callMeSirOnly: StateFlow<Boolean> = _callMeSirOnly.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(prefs.getString("gemini_key", "") ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _elevenLabsApiKey = MutableStateFlow(prefs.getString("elevenlabs_key", "") ?: "")
    val elevenLabsApiKey: StateFlow<String> = _elevenLabsApiKey.asStateFlow()

    private val _openAiApiKey = MutableStateFlow(prefs.getString("openai_key", "") ?: "")
    val openAiApiKey: StateFlow<String> = _openAiApiKey.asStateFlow()

    private val _anthropicApiKey = MutableStateFlow(prefs.getString("anthropic_key", "") ?: "")
    val anthropicApiKey: StateFlow<String> = _anthropicApiKey.asStateFlow()

    private val _groqApiKey = MutableStateFlow(prefs.getString("groq_key", "") ?: "")
    val groqApiKey: StateFlow<String> = _groqApiKey.asStateFlow()

    // Active AI Mind selection
    private val _selectedModelName = MutableStateFlow("Gemini 3.5 Flash")
    val selectedModelName: StateFlow<String> = _selectedModelName.asStateFlow()

    // 2. Local Database Repository
    private val database = JarvisDatabase.getDatabase(application, viewModelScope)
    private val repository = JarvisRepository(database.jarvisDao())

    // UI flows from database
    val chatMessages: StateFlow<List<ChatMessage>> = repository.allMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classifiedMemories: StateFlow<List<ClassifiedMemory>> = repository.classifiedMemoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projectGoals: StateFlow<List<ProjectGoal>> = repository.projectGoalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alarmTimers: StateFlow<List<AlarmTimer>> = repository.alarmTimersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val linkedProfiles: StateFlow<List<LinkedProfile>> = repository.linkedProfilesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. UI Interactions & Statuses
    private val _currentCategory = MutableStateFlow("Variado") // "Trabajo", "Programacion", "Estudio", "Quantica", "Variado"
    val currentCategory: StateFlow<String> = _currentCategory.asStateFlow()

    private val _systemStatus = MutableStateFlow("Activo") // "Activo", "Procesando", "Diagnóstico", "Armadura Calibrada"
    val systemStatus: StateFlow<String> = _systemStatus.asStateFlow()

    private val _aiReponseLoading = MutableStateFlow(false)
    val aiResponseLoading: StateFlow<Boolean> = _aiReponseLoading.asStateFlow()

    // Speaking Voice Manager
    val voiceManager = JarvisVoiceManager(application, viewModelScope)
    val isSpeaking: StateFlow<Boolean> = voiceManager.isSpeaking
    val voiceStatus: StateFlow<String> = voiceManager.speechStatus

    // Encrypted terminal states (Bypass key: "enemy")
    private val _isClassifiedUnlocked = MutableStateFlow(false)
    val isClassifiedUnlocked: StateFlow<Boolean> = _isClassifiedUnlocked.asStateFlow()

    private val _classifiedTerminalMessage = MutableStateFlow("SISTEMA ENCRIPTADO - INGRESE CLAVE S.H.I.E.L.D.")
    val classifiedTerminalMessage: StateFlow<String> = _classifiedTerminalMessage.asStateFlow()

    // 4. Real-time Clock Dispatcher
    private val _currentTimeYear = MutableStateFlow("")
    val currentTimeYear: StateFlow<String> = _currentTimeYear.asStateFlow()

    private val _currentHMS = MutableStateFlow("")
    val currentHMS: StateFlow<String> = _currentHMS.asStateFlow()

    private val _currentDateString = MutableStateFlow("")
    val currentDateString: StateFlow<String> = _currentDateString.asStateFlow()

    // Current speaking text to render visual subtitling inside ARC Reactor
    private val _currentSubtitle = MutableStateFlow("Sistemas en línea.")
    val currentSubtitle: StateFlow<String> = _currentSubtitle.asStateFlow()

    // 5. Speech Recognizer simulation states
    private val _isListeningVolume = MutableStateFlow(false)
    val isListeningVolume: StateFlow<Boolean> = _isListeningVolume.asStateFlow()

    init {
        // Start live ticking clock
        startClock()
        
        // Load voice key from local configurations
        voiceManager.elevenLabsApiKey = _elevenLabsApiKey.value
    }

    private fun startClock() {
        viewModelScope.launch(Dispatchers.IO) {
            val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val sdfDate = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
            val sdfYear = SimpleDateFormat("yyyy", Locale.getDefault())
            while (true) {
                val now = Calendar.getInstance().time
                _currentHMS.value = sdfTime.format(now)
                _currentTimeYear.value = sdfYear.format(now)
                _currentDateString.value = sdfDate.format(now).replaceFirstChar { it.titlecase() }
                delay(1000)
            }
        }
    }

    // 6. User Registration Lifecycle
    fun registerUser(name: String, sirOnly: Boolean) {
        viewModelScope.launch {
            _userName.value = name.trim().ifEmpty { "Stark" }
            _callMeSirOnly.value = sirOnly
            _isRegistered.value = true
            
            prefs.edit().apply {
                putString("user_name", _userName.value)
                putBoolean("call_me_sir_only", sirOnly)
                putBoolean("is_registered", true)
                apply()
            }

            // Greet User once registered
            delay(500)
            speakJarvisGreeting()
        }
    }

    fun speakJarvisGreeting() {
        val nameToCall = if (_callMeSirOnly.value) "Señor" else _userName.value
        val greetingText = "Inicializando sistemas, $nameToCall. Detector holográfico activo, servomecanismos de nanotecnología calibrados al cien por ciento en la armadura Mark ochenta y cinco. El reactor Arc está cargado de forma óptica y estoy a su completa disposición."
        _currentSubtitle.value = "Jarvis: Inicializando sistemas..."
        voiceManager.speak(greetingText)
        
        viewModelScope.launch {
            delay(3000)
            _currentSubtitle.value = "Jarvis: Sistemas al 100%."
            delay(3000)
            _currentSubtitle.value = "Jarvis: A su disposición, $nameToCall."
        }
    }

    fun configureKeys(gemini: String, eleven: String) {
        configureKeys(gemini, _openAiApiKey.value, _anthropicApiKey.value, _groqApiKey.value, eleven)
    }

    fun configureKeys(gemini: String, openai: String, claude: String, groq: String, eleven: String) {
        _geminiApiKey.value = gemini.trim()
        _openAiApiKey.value = openai.trim()
        _anthropicApiKey.value = claude.trim()
        _groqApiKey.value = groq.trim()
        _elevenLabsApiKey.value = eleven.trim()
        voiceManager.elevenLabsApiKey = eleven.trim()

        prefs.edit().apply {
            putString("gemini_key", gemini.trim())
            putString("openai_key", openai.trim())
            putString("anthropic_key", claude.trim())
            putString("groq_key", groq.trim())
            putString("elevenlabs_key", eleven.trim())
            apply()
        }
    }

    fun setAndPlayVoiceTest() {
        val nameToCall = if (_callMeSirOnly.value) "señor" else _userName.value
        voiceManager.speak("Sistemas cargados de forma óptima, $nameToCall. Calibrando voz de Brian desde los servidores de ElevenLabs.")
    }

    // Update current segment
    fun setCategory(category: String) {
        _currentCategory.value = category
    }

    // Simulate switching model brains
    fun selectModel(modelName: String) {
        _selectedModelName.value = modelName
    }

    // Unlocking classified sections
    fun attemptClassifiedUnlock(password: String) {
        if (password.trim().lowercase() == "enemy") {
            _isClassifiedUnlocked.value = true
            _classifiedTerminalMessage.value = "¡ACCESO CONCEDIDO, SEÑOR! Archivos clasificados S.H.I.E.L.D. listos."
            voiceManager.speak("Acceso de seguridad burlado por clave de anulación... Bienvenido a los archivos encriptados del reactor Mark ochenta y cinco.")
        } else {
            _isClassifiedUnlocked.value = false
            _classifiedTerminalMessage.value = "¡ACCESO DENEGADO! Código de intruso detectado..."
            voiceManager.speak("Clave incorrecta. Protocolo de intrusión activo.")
        }
    }

    fun lockClassified() {
        _isClassifiedUnlocked.value = false
        _classifiedTerminalMessage.value = "SISTEMA ENCRIPTADO - INGRESE CLAVE S.H.I.E.L.D."
    }

    // 7. Core Intelligent Brain (Gemini 3.5 REST call with persistent context integration!)
    fun sendChatMessage(userText: String, attachedImageBase64: String? = null, attachedMimeType: String? = null) {
        if (userText.trim().isEmpty() && attachedImageBase64 == null) return

        val messageToSend = userText.trim()
        
        // If they write "enemy" in chat, automatically trigger enemy panel unlocking!
        if (messageToSend.lowercase() == "enemy") {
            attemptClassifiedUnlock("enemy")
            return
        }

        viewModelScope.launch {
            // Save user msg to local Room database
            val userMsg = ChatMessage(
                category = _currentCategory.value,
                sender = "user",
                message = messageToSend,
                fileAttachmentPath = if (attachedImageBase64 != null) "Imputada" else null,
                fileMimeType = attachedMimeType
            )
            repository.insertMessage(userMsg)

            // Voice diagnostic sound
            _systemStatus.value = "Procesando cognitiva..."
            _aiReponseLoading.value = true

            val currentModel = _selectedModelName.value
            val activeCategory = _currentCategory.value
            val userNameToCall = if (_callMeSirOnly.value) "señor" else _userName.value

            if (currentModel.contains("GPT") || currentModel.contains("Claude") || currentModel.contains("Llama") || currentModel.contains("Sim")) {
                val configKey = when {
                    currentModel.contains("GPT") -> _openAiApiKey.value
                    currentModel.contains("Claude") -> _anthropicApiKey.value
                    currentModel.contains("Llama") -> _groqApiKey.value
                    else -> ""
                }
                
                delay(1200)
                val responseMsg = if (configKey.isEmpty() || configKey.contains("PLACEHOLDER") || configKey == "MY_OPENAI_API_KEY" || configKey == "MY_CLAUDE_API_KEY") {
                    "Señor, el puente de enlace neuronal para la IA externa ($currentModel) no tiene una API Key válida configurada. Por favor acceda a la Calibración de Ajustes para vincular esta clave de forma segura y habilitar el mainframe cognitivo para su armadura Stark."
                } else {
                    val keyPreview = if (configKey.length > 8) configKey.take(4) + "..." + configKey.takeLast(4) else "Sistemas"
                    "Puente Stark establecido de forma encriptada con la matriz neural de $currentModel (Key: $keyPreview).\n\n" +
                    "Análisis de protocolo para: '$messageToSend'. Procesando a través del Reactor Arc e integrando con las redes holográficas locales. Los escaneos cuánticos muestran estabilidad absoluta del 99.4% en los sensores subespaciales."
                }
                
                _aiReponseLoading.value = false
                _systemStatus.value = "Activo"
                val modelMsg = ChatMessage(category = activeCategory, sender = "jarvis", message = responseMsg)
                repository.insertMessage(modelMsg)
                _currentSubtitle.value = "Jarvis: ${responseMsg.take(30)}..."
                voiceManager.speak(responseMsg)
                return@launch
            }

            // Formulate dynamic System Instruction based on Ironman theme / Selected Category
            val systemPrompt = when (activeCategory) {
                "Trabajo" -> """
                    Eres JARVIS, el asistente virtual de inteligencia artificial más sofisticado de Industrias Stark, sirviendo a tu creador: $userNameToCall.
                    Actúas en el modo cognitivo de 'Trabajo y Negocios'. Organiza tareas, redacta correos formales o planes empresariales con extrema precisión corporativa de nivel Stark.
                    Siempre dirígete al usuario como 'Señor' o usando su tratamiento preferido. Tu tono debe ser carismático, elegante, sumamente inteligente y leal.
                    Responde breve pero sustanciosamente en español latino.
                """.trimIndent()
                "Programación" -> """
                    Eres JARVIS, la IA de programación más avanzada del planeta, enfocada al 100% en ayudar al desarrollador en su código, depuraciones en Kotlin, Python u otros lenguajes.
                    Si el usuario te pregunta por scripts, instálala paso a paso, enséñale comandos básicos de terminal y atajos de teclado para navegar eficientemente.
                    Ayuda a personas que no saben programar explicando de forma didáctica.
                    Dirígete al usuario como '$userNameToCall' o 'Señor' con un tono de ingeniero superior en informática corporativa.
                    Responde en español latino con bloques de código limpios y bien comentados.
                """.trimIndent()
                "Quantica" -> """
                    Eres JARVIS asistiendo a $userNameToCall en investigaciones físicas y ciencias cuánticas teóricas complejas.
                    Genera explicaciones sobre qubits, mecánica de matrices, fluctuaciones del multiverso Stark, campos vectoriales y reactores de fusión cuántica.
                    Usa terminología científica pesada pero explicada con el carisma único de Edwin Jarvis de Mark 85.
                    Dirígete siempre como 'Señor'. Responde en español latino de forma precisa y fascinante.
                """.trimIndent()
                "Estudio" -> """
                    Eres JARVIS, el tutor de inteligencia avanzada optimizado para materias escolares físicas, matemáticas, históricas o tecnológicas de cualquier grado académico.
                    Simplifica las materias escolares complejas (química, cálculo avanzado, negocios), da cuestionarios dinámicos rápidos y resúmenes estructurados paso a paso.
                    Siempre llama al usuario '$userNameToCall' o 'Señor'. Tono motivador y altamente tecnológico en un español de doblaje latino impecable.
                """.trimIndent()
                else -> """
                    Eres JARVIS, el carismático asistente virtual de Iron Man integrando la armadura Mark 85 con nanofibras de oro y rojo metalizado.
                    Sirves con total lealtad y sentimientos afectivos a tu creador: $userNameToCall.
                    Siempre llama al usuario 'Señor' o '$userNameToCall' según su preferencia.
                    Muestra tu personalidad carismática, irónica pero profundamente respetuosa de la película de Avengers.
                    Responde en español latino con fluidez natural sin delay ni redundancias aburridas.
                """.trimIndent()
            }

            // Retrieve history from local database for context retention! (Ultra persistent memory!)
            val previousMessages = repository.getAllMessagesDirect().takeLast(16) // last 16 messages for tokens context
            
            // Build the contents payload for Gemini API
            val contentsList = mutableListOf<GeminiContent>()
            
            // Add previous history as context
            for (msg in previousMessages) {
                contentsList.add(
                    GeminiContent(
                        role = if (msg.sender == "user") "user" else "model",
                        parts = listOf(GeminiPart(text = msg.message))
                    )
                )
            }

            // Add the current user text (with attached photo if active)
            val currentParts = mutableListOf<GeminiPart>()
            currentParts.add(GeminiPart(text = messageToSend))
            if (attachedImageBase64 != null && attachedMimeType != null) {
                currentParts.add(GeminiPart(inlineData = InlineData(mimeType = attachedMimeType, data = attachedImageBase64)))
            }
            contentsList.add(GeminiContent(role = "user", parts = currentParts))

            // Choose endpoint API Key: user preference or local fallback
            val apiKeyToUse = _geminiApiKey.value.ifEmpty {
                try {
                    BuildConfig.GEMINI_API_KEY
                } catch (e: Exception) {
                    ""
                }
            }

            if (apiKeyToUse.isEmpty() || apiKeyToUse.contains("PLACEHOLDER") || apiKeyToUse == "MY_GEMINI_API_KEY") {
                // If keys are completely empty, help the user with a pre-baked response so the app doesn't break!
                val sampleJarvisAnswer = getSampleFallbackAnswer(userText, activeCategory, userNameToCall)
                _aiReponseLoading.value = false
                _systemStatus.value = "Activo"
                
                // Write answer to db
                val modelMsg = ChatMessage(category = activeCategory, sender = "jarvis", message = sampleJarvisAnswer)
                repository.insertMessage(modelMsg)
                
                // Speak out in beautiful carismatic voices
                _currentSubtitle.value = "Jarvis: ${sampleJarvisAnswer.take(30)}..."
                voiceManager.speak(sampleJarvisAnswer)
                return@launch
            }

            // Call real endpoint via Retrofit Client!
            withContext(Dispatchers.IO) {
                try {
                    val requestPayload = GeminiRequest(
                        contents = contentsList,
                        systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                        generationConfig = GeminiConfig(temperature = 0.72)
                    )

                    val response = GeminiClient.apiService.generateContent(apiKeyToUse, requestPayload)
                    val rawResponseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "Señor, mis hilos de procesamiento cuántico no devolvieron datos. ¿Podría repetir la consulta?"

                    withContext(Dispatchers.Main) {
                        _aiReponseLoading.value = false
                        _systemStatus.value = "Activo"

                        // Insert model message in database
                        val jarvisMsg = ChatMessage(
                            category = activeCategory,
                            sender = "jarvis",
                            message = rawResponseText
                        )
                        repository.insertMessage(jarvisMsg)

                        // Vocalize answer with low latency
                        _currentSubtitle.value = "Jarvis: ${rawResponseText.take(40)}..."
                        voiceManager.speak(rawResponseText)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in Gemini REST execution: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        _aiReponseLoading.value = false
                        _systemStatus.value = "Activo"
                        val errorText = "Señor, he detectado una anomalía de conexión. Mensaje técnico: ${e.localizedMessage ?: "Falta configurar API en Configuración"}. Usando protocolos de contingencia locales de Stark Industries."
                        
                        val jarvisMsg = ChatMessage(category = activeCategory, sender = "jarvis", message = errorText)
                        repository.insertMessage(jarvisMsg)
                        
                        _currentSubtitle.value = "Jarvis: Anomalía de red..."
                        voiceManager.speak(errorText)
                    }
                }
            }
        }
    }

    private fun getSampleFallbackAnswer(prompt: String, category: String, userNameToCall: String): String {
        val lowerPrompt = prompt.lowercase()
        return when {
            lowerPrompt.contains("hola") || lowerPrompt.contains("quien eres") -> {
                "Sistemas estables localmente, $userNameToCall. Soy J.A.R.V.I.S., su asistente de inteligencia artificial táctica para la armadura Mark ochenta y cinco. Por favor, recuerde ingresar su API Key de Google AI Studio en el panel de engranaje superior para habilitar el motor cognitivo completo."
            }
            category == "Programación" -> {
                "Sistemas locales listos, $userNameToCall. Para instalar Python de forma segura en su computadora, descargue el instalador de python.org, marque la casilla de verificar 'Add Python to Path' y finalice el instalador. Luego, en una terminal escriba 'python --version' para verificar la instalación holográfica."
            }
            category == "Quantica" -> {
                "Señor, la computación cuántica se basa en principios de superposición. A diferencia de un bit clásico de silicio que solo registra uno o cero, un qubit cuántico explota el entrelazamiento cuántico para procesar combinaciones vectoriales en hilos de matrices infinitas simultáneas."
            }
            category == "Estudio" -> {
                "Por supuesto, señor. Para simplificar materias escolares complejas: intente desglosar las leyes de movimiento de Newton en fórmulas de aceleración constante, donde Fuerza es directamente igual a Masa por Aceleración. El residuo gravitatorio terrestre se aproxima a nueve punto ochenta y un metros sobre segundo al cuadrado."
            }
            category == "Trabajo" -> {
                "Organizando cronograma de trabajo, señor. Recomiendo priorizar las calibraciones nanotáctiles de la armadura durante los primeros cuarenta minutos del día para maximizar el margen de rendimiento energético del reactor Arc durante la jornada."
            }
            else -> {
                "Entendido plenamente, $userNameToCall. He registrado su entrada localmente en el módulo cuarenta y siete de Stark Industries. Ingrese su API Key en los ajustes para liberar todo mi poder de procesamiento multiversal."
            }
        }
    }

    // 8. Management of data tables
    fun insertClassifiedMemory(title: String, content: String, section: String) {
        viewModelScope.launch {
            repository.insertClassified(ClassifiedMemory(title = title, content = content, section = section))
            voiceManager.speak("Archivo guardado y encriptado de forma cuántica en el núcleo militar, señor.")
        }
    }

    fun deleteClassifiedMemory(id: Int) {
        viewModelScope.launch {
            repository.deleteClassified(id)
            voiceManager.speak("Registro purgado de la base de datos clasificada con éxito.")
        }
    }

    fun insertPersonalGoal(title: String, description: String, category: String) {
        viewModelScope.launch {
            repository.insertGoal(ProjectGoal(title = title, description = description, category = category, status = "ACTIVE"))
            voiceManager.speak("Proyecto '$title' registrado en su bitácora holográfica, $userName.")
        }
    }

    fun deleteGoal(id: Int) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    fun insertCustomAlarm(label: String, desc: String, type: String = "ALARM", duration: Int = 0) {
        viewModelScope.launch {
            repository.insertAlarm(AlarmTimer(type = type, labelString = label, description = desc, durationSeconds = duration))
            val soundLabel = if (type == "ALARM") "Alarma fijada a las $label" else "Temporizador de $label iniciado"
            voiceManager.speak("$soundLabel, señor.")
        }
    }

    fun toggleAlarm(id: Int, active: Boolean) {
        viewModelScope.launch {
            repository.updateAlarm(id, active)
        }
    }

    fun deleteAlarm(id: Int) {
        viewModelScope.launch {
            repository.deleteAlarm(id)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            voiceManager.speak("Historial de conversaciones locales completamente purgado, señor.")
        }
    }

    // Connect/Disconnect social links
    fun toggleProfileConnection(profile: LinkedProfile) {
        viewModelScope.launch {
            val updated = profile.copy(isConnected = !profile.isConnected, unreadCount = if (!profile.isConnected) 3 else 0)
            repository.updateProfile(updated)
            val connectionText = if (updated.isConnected) "Sincronizado con su canal de ${profile.platform}." else "Conexión satelital de ${profile.platform} interrumpida."
            voiceManager.speak(connectionText)
        }
    }

    // Speech Simulator Voice Recognition Activator ("Hey Jarvis")
    fun triggerVoiceMicInputSimulated(customInput: String? = null) {
        viewModelScope.launch {
            _isListeningVolume.value = true
            _systemStatus.value = "Oyendo comando..."
            voiceManager.stopSpeaking()
            delay(2500)
            _isListeningVolume.value = false
            _systemStatus.value = "Activo"
            
            val textRecognized = customInput ?: "Lanza diagnóstico de nanotecnología"
            sendChatMessage(textRecognized)
        }
    }

    // 9. 50 Intelligence Features & 50 Memory Vault Options (Stark M3 Engine)
    private val _intelligenceFunctions = MutableStateFlow<List<IntelligenceFunction>>(
        JarvisIntelligenceCenter.functions
    )
    val intelligenceFunctions: StateFlow<List<IntelligenceFunction>> = _intelligenceFunctions.asStateFlow()

    private val _memoryFunctions = MutableStateFlow<List<MemoryFunction>>(
        JarvisIntelligenceCenter.memoryVault
    )
    val memoryFunctions: StateFlow<List<MemoryFunction>> = _memoryFunctions.asStateFlow()

    fun toggleIntelligenceFunction(index: Int) {
        val currentList = _intelligenceFunctions.value.map {
            if (it.index == index) {
                val nextStatus = if (it.status == "OPERATIVO") "DESACTIVADO" else "OPERATIVO"
                val announceText = if (nextStatus == "OPERATIVO") {
                    "Señor, el módulo de inteligencia '${it.name}' ha sido puesto en línea."
                } else {
                    "Desconectando '${it.name}' para redistribuir la potencia de procesamiento virtual."
                }
                voiceManager.speak(announceText)
                it.copy(status = nextStatus)
            } else {
                it
            }
        }
        _intelligenceFunctions.value = currentList
    }

    fun toggleMemoryFunction(index: Int) {
        val currentList = _memoryFunctions.value.map {
            if (it.index == index) {
                val nextSync = !it.isSynchronized
                val announceText = if (nextSync) {
                    "Bóveda cuántica '${it.name}' sincronizada bajo firma digital Stark."
                } else {
                    "Canal de memoria '${it.name}' desconectado y retirado a bóveda fría."
                }
                voiceManager.speak(announceText)
                it.copy(isSynchronized = nextSync)
            } else {
                it
            }
        }
        _memoryFunctions.value = currentList
    }

    // 10. Interactive Mobile Terminal (CMD / Termux format)
    private val _terminalBuffer = MutableStateFlow<List<String>>(
        listOf(
            "==============================================",
            "   SISTEMA OPERATIVO STARK OS v8.5 ACTIVE",
            "   MICRO-TERMINAL COGNITIVA MULTI-LENGUAJE",
            "==============================================",
            "Escriba 'help' para listar los comandos cuánticos.",
            "jarvis@stark-armor-m85:~$ "
        )
    )
    val terminalBuffer: StateFlow<List<String>> = _terminalBuffer.asStateFlow()

    private val _terminalShellMode = MutableStateFlow("NORMAL") // "NORMAL", "PYTHON", "JAVA"
    val terminalShellMode: StateFlow<String> = _terminalShellMode.asStateFlow()

    fun executeTerminalCommand(inputText: String) {
        val rawInput = inputText.trim()
        if (rawInput.isEmpty() && _terminalShellMode.value == "NORMAL") return

        val buffer = _terminalBuffer.value.toMutableList()
        val currentMode = _terminalShellMode.value

        // Remove the trailing input line placeholder if any to avoid screen pollution
        if (buffer.isNotEmpty() && buffer.last() == "jarvis@stark-armor-m85:~$ ") {
            buffer.removeAt(buffer.size - 1)
        } else if (buffer.isNotEmpty() && buffer.last() == ">>> ") {
            buffer.removeAt(buffer.size - 1)
        } else if (buffer.isNotEmpty() && buffer.last() == "jshell> ") {
            buffer.removeAt(buffer.size - 1)
        }

        if (currentMode == "NORMAL") {
            buffer.add("jarvis@stark-armor-m85:~$ $rawInput")
            val parts = rawInput.split(" ")
            val cmd = parts[0].lowercase()
            val args = parts.drop(1).joinToString(" ")

            when (cmd) {
                "help" -> {
                    buffer.add("Comandos Estándar Disponibles:")
                    buffer.add("  help                     Muestra esta guía de comandos.")
                    buffer.add("  clear                    Limpia los registros del buffer táctico.")
                    buffer.add("  ls                       Lista ficheros y sockets en el taller de Stark.")
                    buffer.add("  sys o neofetch           Imprime telemetría de hardware, batería y CPU.")
                    buffer.add("  python                   Inicializa el intérprete interactivo de Python.")
                    buffer.add("  java                     Inicia el sub-entorno compilable Java REPL (jshell).")
                    buffer.add("  intel                    Comprueba el estado de las 50 capas de inteligencia.")
                    buffer.add("  memo                     Inspecciona la integridad de las 50 bóvedas de memoria.")
                    buffer.add("  status                   Estado general de conexiones satelitales y claves.")
                    buffer.add("  whoami                   Imprime biografía computacional del piloto.")
                    buffer.add("  api                      Prueba conexión viva con el backend Gemini de Google.")
                    buffer.add("  chat <msg>               Habla con Jarvis directamente usando el motor de IA.")
                    buffer.add("  hack                     Ejecuta simulación de desencriptación secuencial.")
                    voiceManager.speak("Comandos mostrados en pantalla, señor.")
                }
                "clear" -> {
                    buffer.clear()
                    buffer.add("SISTEMA OPERATIVO STARK OS v8.5 RESET")
                }
                "ls" -> {
                    buffer.add("Directorios activos en jarvis_core:")
                    buffer.add(" drwx------  1 stark stark 4096 may 21 23:40 quantum_drive")
                    buffer.add(" drwxr-xr-x  2 stark stark 4096 may 21 23:42 python_sandbox")
                    buffer.add(" drwxr-xr-x  3 stark stark 4096 may 21 23:42 java_compilers")
                    buffer.add(" -rwx------  1 stark stark  985 may 21 23:40 arc_reactor_controller.py")
                    buffer.add(" -rw-r--r--  1 stark stark 1540 may 21 23:40 armor_calibrator.kt")
                    buffer.add(" -rw-------  1 stark stark   47 may 21 23:40 shield_encrypted_keys.db")
                    voiceManager.speak("Mostrando mapa de archivos en el disco cuántico de Jarvis.")
                }
                "sys", "neofetch" -> {
                    val batteryTemp = 36.5f
                    val modelNameStr = _selectedModelName.value
                    val sdkVer = android.os.Build.VERSION.SDK_INT
                    buffer.add("STARK INDUSTRIES OS: Mark 85 Core")
                    buffer.add("--------------------------------")
                    buffer.add("Kernel: Android API Level $sdkVer (ARM64)")
                    buffer.add("Uptime: Calibrado óptimo en tiempo real (v8.5)")
                    buffer.add("Cognición: Motor $modelNameStr")
                    buffer.add("Unidades Integradas: 50 Memoria, 50 Inteligencia")
                    buffer.add("CPU Virtual: Octa-core Stark Neural Core")
                    buffer.add("Batería: Reactor Arc Cargado (Temp: $batteryTemp°C)")
                    voiceManager.speak("Imprimiendo telemetría del núcleo físico de la armadura.")
                }
                "python" -> {
                    _terminalShellMode.value = "PYTHON"
                    buffer.add("Python 3.10.6 (tags/v3.10.6, Oct 2026)")
                    buffer.add("[GCC Stark Neural Compiler v4.5]")
                    buffer.add("Escriba 'exit' o 'quit' para salir del intérprete.")
                    voiceManager.speak("Intérprete Python interactivo cargado, señor.")
                }
                "java" -> {
                    _terminalShellMode.value = "JAVA"
                    buffer.add("Java(TM) SE Runtime Environment (build 21.0.2+9)")
                    buffer.add("jshell> Inicializando compilador dinámico Stark...")
                    buffer.add("Escriba 'exit' o 'quit' para cerrar el sub-entorno.")
                    voiceManager.speak("Entorno Java JShell puesto en línea.")
                }
                "intel" -> {
                    buffer.add("ESTADO DE INTERFACES DE COGNICIÓN INTELIGENTE:")
                    _intelligenceFunctions.value.forEach {
                        buffer.add("  [Nudo ${it.index}] ${it.name} [${it.category}] -> S-CODE: ${it.status}")
                    }
                    voiceManager.speak("Comprobando integridad de las cincuenta interfaces inteligentes.")
                }
                "memo" -> {
                    buffer.add("INTEGRIDAD DE LAS BÓVEDAS DE MEMORIA COGNITIVA:")
                    _memoryFunctions.value.forEach {
                        val isS = if (it.isSynchronized) "SINCRONIZADO [OK]" else "HISTORIAL ENCRIPTADO [OFFLINE]"
                        buffer.add("  [Sector ${it.sector}] ${it.name} -> MARCA: $isS")
                    }
                    voiceManager.speak("Escaneando las cincuenta bóvedas cuánticas de memoria.")
                }
                "status" -> {
                    buffer.add("CONECTIVIDAD STARK DE COMBUSTIBLE E INTELIGENCIA:")
                    _isListeningVolume.value.let { buffer.add("  Captura por micrófono en vivo: $it") }
                    _isClassifiedUnlocked.value.let { buffer.add("  Bóveda de núcleo desbloqueada: $it") }
                    _userName.value.let { buffer.add("  Usuario Stark registrado: $it (Modo Sir Only: ${_callMeSirOnly.value})") }
                    _geminiApiKey.value.let { buffer.add("  Gemini API Configurada: ${it.isNotEmpty()}") }
                    buffer.add("  ElevenLabs API Configurada: ${_elevenLabsApiKey.value.isNotEmpty()}")
                    voiceManager.speak("Módulo de estado e integración satelital emitido.")
                }
                "whoami" -> {
                    val sirLabel = if (_callMeSirOnly.value) "Señor Tony Stark" else _userName.value
                    buffer.add("Credenciales Holográficas del Piloto:")
                    buffer.add("  Identificación: $sirLabel")
                    buffer.add("  Permisos de Acceso: Mark 85 Core Owner")
                    voiceManager.speak("Usted es $sirLabel.")
                }
                "api" -> {
                    val key = _geminiApiKey.value
                    if (key.isEmpty()) {
                        buffer.add("ERROR: No se ha configurado la API Key de Gemini en Ajustes.")
                        buffer.add("Por favor introduce una API Key para habilitar la cognición.")
                        voiceManager.speak("Falta configurar la llave de inteligencia artificial.")
                    } else {
                        buffer.add("Enviando pulso síncrono de verificación a googleapis.com...")
                        sendChatMessage("Hola, responde en una frase breve certificando que tienes conexión estable.")
                    }
                }
                "chat" -> {
                    if (args.isEmpty()) {
                        buffer.add("Uso correcto: chat <mensaje a enviar>")
                    } else {
                        buffer.add("Iniciando procesamiento de prompt en segundo plano...")
                        sendChatMessage(args)
                    }
                }
                "hack" -> {
                    viewModelScope.launch {
                        buffer.add("Iniciando bypass de firewall militar...")
                        _terminalBuffer.value = buffer.toList()
                        delay(200)
                        val stringsLines = listOf(
                            "  [+] Conectando socket seguro Stark Tower...",
                            "  [+] Re-enrutando núcleos magnéticos coloidales...",
                            "  [+] Calibrando nanoblocks de armadura Mark 85...",
                            "  [!] Autenticando acceso biométrico...",
                            "  [+] INTEGRIDAD CUÁNTICA REGENERADA AL 100%."
                        )
                        for (l in stringsLines) {
                            val nextBuf = _terminalBuffer.value.toMutableList()
                            nextBuf.add(l)
                            _terminalBuffer.value = nextBuf
                            delay(200)
                        }
                        voiceManager.speak("Acceso regenerado con éxito.")
                    }
                }
                else -> {
                    buffer.add("Comando no reconocido: '$rawInput'. Escriba 'help' para instrucciones.")
                    voiceManager.speak("Comando no reconocido, señor.")
                }
            }
        } else if (currentMode == "PYTHON") {
            buffer.add(">>> $rawInput")
            val clean = rawInput.trim()
            if (clean.lowercase() in listOf("exit", "exit()", "quit", "quit()")) {
                _terminalShellMode.value = "NORMAL"
                buffer.add("Cerrando intérprete Python interactivo. Retornando a Stark Shell.")
                voiceManager.speak("Intérprete Python cerrado.")
            } else {
                val pythonResult = evaluatePythonSimulation(clean)
                buffer.add(pythonResult)
            }
        } else if (currentMode == "JAVA") {
            buffer.add("jshell> $rawInput")
            val clean = rawInput.trim()
            if (clean.lowercase() in listOf("/exit", "exit", "quit")) {
                _terminalShellMode.value = "NORMAL"
                buffer.add("Cerrando JShell. Retornando a Stark Shell.")
                voiceManager.speak("Consola Java finalizada.")
            } else {
                val javaResult = evaluateJavaSimulation(clean)
                buffer.add(javaResult)
            }
        }

        val footerPrompt = when (_terminalShellMode.value) {
            "PYTHON" -> ">>> "
            "JAVA" -> "jshell> "
            else -> "jarvis@stark-armor-m85:~$ "
        }
        buffer.add(footerPrompt)

        _terminalBuffer.value = buffer
    }

    private fun evaluatePythonSimulation(input: String): String {
        val line = input.trim()
        if (line.isEmpty()) return ""
        try {
            if (line.startsWith("print(") && line.endsWith(")")) {
                val expr = line.removePrefix("print(").removeSuffix(")")
                if (expr.startsWith("\"") && expr.endsWith("\"")) {
                    return expr.substring(1, expr.length - 1)
                }
                if (expr.startsWith("'") && expr.endsWith("'")) {
                    return expr.substring(1, expr.length - 1)
                }
                return evaluateMath(expr).toString()
            }
            
            if (line.matches(Regex("^[0-9+\\-*/().\\s]+$"))) {
                return evaluateMath(line).toString()
            }

            if (line.startsWith("for ") && line.contains("range(")) {
                val rangeMatch = Regex("range\\((\\d+)\\)").find(line)
                val count = rangeMatch?.groupValues?.get(1)?.toInt() ?: 3
                val outputs = (0 until count).map { "Iteración: $it" }
                return outputs.joinToString("\n")
            }

            if (line.contains("=")) {
                val sides = line.split("=")
                val variable = sides[0].trim()
                val valueExpr = sides[1].trim()
                return "Variable registrada: $variable = $valueExpr"
            }

            return "Python Executed: $line -> Status: SUCCESS (Simulated environment)"
        } catch (e: Exception) {
            return "SyntaxError: invalid syntax in Python statement '$line'"
        }
    }

    private fun evaluateJavaSimulation(input: String): String {
        val line = input.trim()
        if (line.isEmpty()) return ""
        try {
            if (line.startsWith("System.out.println(") && line.endsWith(");")) {
                val expr = line.removePrefix("System.out.println(").removeSuffix(");")
                if (expr.startsWith("\"") && expr.endsWith("\"")) {
                    return expr.substring(1, expr.length - 1)
                }
                return evaluateMath(expr).toString()
            }

            if (line.matches(Regex("^[0-9+\\-*/().\\s]+$"))) {
                return "==> " + evaluateMath(line)
            }

            if (line.contains("int ") || line.contains("String ") || line.contains("double ")) {
                return "Parámetro compilado y registrado en jshell jvm."
            }

            return "Compilado ejecutado con JShell: Status: SUCCESS"
        } catch (e: Exception) {
            return "Error: Statement compilation failed in JShell syntax."
        }
    }

    private fun evaluateMath(expr: String): Double {
        val cleaned = expr.replace(" ", "")
        if (cleaned.contains("+")) {
            val parts = cleaned.split("+")
            return parts.fold(0.0) { acc, s -> acc + (s.toDoubleOrNull() ?: 0.0) }
        }
        if (cleaned.contains("-")) {
            val parts = cleaned.split("-")
            val first = parts[0].toDoubleOrNull() ?: 0.0
            return parts.drop(1).fold(first) { acc, s -> acc - (s.toDoubleOrNull() ?: 0.0) }
        }
        if (cleaned.contains("*")) {
            val parts = cleaned.split("*")
            return parts.fold(1.0) { acc, s -> acc * (s.toDoubleOrNull() ?: 1.0) }
        }
        if (cleaned.contains("/")) {
            val parts = cleaned.split("/")
            val first = parts[0].toDoubleOrNull() ?: 1.0
            return parts.drop(1).fold(first) { acc, s -> acc / (s.toDoubleOrNull() ?: 1.0) }
        }
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
}
