package com.example.data

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class JarvisVoiceManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val TAG = "JarvisVoiceManager"

    // Voice States
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _speechStatus = MutableStateFlow("Sistemas estables")
    val speechStatus: StateFlow<String> = _speechStatus

    // Configurable ElevenLabs API Key and Voice ID
    var elevenLabsApiKey: String = ""
    var voiceId: String = "rkfB2vVeeZ0K1lAtVv8c" // Elegant English/Multilingual male voice ID (Brian clone)

    // Android Native TTS Fallback compiled with premium grave options
    private var nativeTts: TextToSpeech? = null
    private var isNativeTtsReady = false

    // Players
    private var mediaPlayer: MediaPlayer? = null
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    init {
        initializeNativeTts()
    }

    private fun initializeNativeTts() {
        try {
            nativeTts = TextToSpeech(context) { status ->
                try {
                    if (status == TextToSpeech.SUCCESS) {
                        val localeEs = Locale("es", "MX") // Spanish Latino
                        val result = nativeTts?.setLanguage(localeEs)
                        if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                            // Try general Spanish fallback
                            nativeTts?.setLanguage(Locale("es"))
                        }
                        // Custom adjustments for Jarvis Personality (grave, fast)
                        nativeTts?.setPitch(0.82f) // Grave, mature masculine pitch
                        nativeTts?.setSpeechRate(1.15f) // Natural, fast speech style

                        nativeTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                _isSpeaking.value = true
                            }

                            override fun onDone(utteranceId: String?) {
                                _isSpeaking.value = false
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                _isSpeaking.value = false
                            }

                            override fun onError(utteranceId: String?, errorCode: Int) {
                                _isSpeaking.value = false
                            }
                        })
                        isNativeTtsReady = true
                        Log.d(TAG, "Native TTS initialized successfully in Spanish.")
                    } else {
                        Log.e(TAG, "Failed to initialize standard TTS with status: $status")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error inside TTS setup listener: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating TextToSpeech: ${e.message}", e)
            isNativeTtsReady = false
        }
    }

    fun speak(text: String) {
        // Stop any currently playing audio
        stopSpeaking()

        // Clean text from bullet points or markdown syntax for clean voicing
        val cleanText = text
            .replace(Regex("[*#_`~>•]"), "")
            .replace(Regex("\\(.*\\)"), "")
            .trim()

        if (cleanText.isEmpty()) return

        // 1. Check if ElevenLabs key is present either configured or in BuildConfig
        val apiKey = elevenLabsApiKey.ifEmpty {
            try {
                // Return compiled secret if present
                com.example.BuildConfig.ELEVENLABS_API_KEY
            } catch (e: Exception) {
                ""
            }
        }

        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "MY_ELEVENLABS_API_KEY" && !apiKey.contains("PLACEHOLDER")) {
            // Use ElevenLabs API
            scope.launch(Dispatchers.IO) {
                _speechStatus.value = "Generando voz Jarvis (ElevenLabs)..."
                val audioFile = fetchElevenLabsSpeech(cleanText, apiKey)
                if (audioFile != null) {
                    playAudioFile(audioFile)
                } else {
                    // Fallback to Native TTS if API fails
                    Log.e(TAG, "ElevenLabs API failed. Falling back to Spanish Native TTS.")
                    speakNative(cleanText)
                }
            }
        } else {
            // Use Spanish Native TTS directly
            _speechStatus.value = "Voz nativa optimizada activa"
            speakNative(cleanText)
        }
    }

    private fun speakNative(text: String) {
        if (!isNativeTtsReady || nativeTts == null) {
            Log.e(TAG, "Native TTS not ready yet.")
            return
        }
        try {
            _isSpeaking.value = true
            val params = android.os.Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "jarvis_utterance")
            nativeTts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "jarvis_utterance")
        } catch (e: Exception) {
            _isSpeaking.value = false
            Log.e(TAG, "Error executing speakNative: ${e.message}", e)
        }
    }

    private suspend fun fetchElevenLabsSpeech(text: String, apiKey: String): File? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.elevenlabs.io/v1/text-to-speech/$voiceId?output_format=mp3_44100_128"
            
            val jsonObject = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2") // Great quality Spanish model
                put("voice_settings", JSONObject().apply {
                    put("stability", 0.45)
                    put("similarity_boost", 0.75)
                })
            }

            val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("xi-api-key", apiKey)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "ElevenLabs HTTP Error: ${response.code} ${response.message}")
                return@withContext null
            }

            val body = response.body ?: return@withContext null
            val speechFile = File(context.cacheDir, "jarvis_speech.mp3")
            if (speechFile.exists()) {
                speechFile.delete()
            }

            FileOutputStream(speechFile).use { fos ->
                body.byteStream().use { inputStream ->
                    inputStream.copyTo(fos)
                }
            }
            speechFile
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching ElevenLabs voice: ${e.message}", e)
            null
        }
    }

    private fun playAudioFile(file: File) {
        scope.launch(Dispatchers.Main) {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepare()
                    setOnPreparedListener {
                        _isSpeaking.value = true
                        _speechStatus.value = "Hablando..."
                        start()
                    }
                    setOnCompletionListener {
                        _isSpeaking.value = false
                        _speechStatus.value = "Sistemas estables"
                        release()
                        mediaPlayer = null
                    }
                    setOnErrorListener { _, _, _ ->
                        _isSpeaking.value = false
                        _speechStatus.value = "Error de reproducción"
                        release()
                        mediaPlayer = null
                        true
                    }
                }
            } catch (e: Exception) {
                _isSpeaking.value = false
                Log.e(TAG, "Error initializing MediaPlayer: ${e.message}", e)
            }
        }
    }

    fun stopSpeaking() {
        try {
            if (_isSpeaking.value) {
                nativeTts?.stop()
                mediaPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    release()
                }
                mediaPlayer = null
                _isSpeaking.value = false
                _speechStatus.value = "Sistemas estables"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping voice: ${e.message}", e)
        }
    }

    fun release() {
        stopSpeaking()
        nativeTts?.shutdown()
        nativeTts = null
    }
}
