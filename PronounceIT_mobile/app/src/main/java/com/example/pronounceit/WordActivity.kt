package com.example.pronounceit

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pronounceit.databinding.ActivityWordBinding
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.WordEntity
import com.example.pronounceit.network.models.PronunciationCheckResponse
import com.example.pronounceit.network.models.WordResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.util.UUID
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import android.content.pm.PackageManager
import com.example.pronounceit.network.models.PronounciationAttemptPostDTO
import com.example.pronounceit.network.models.ScoreRecordDTO
import com.example.pronounceit.network.models.ScoreRecordEntity
import java.time.LocalDateTime

class WordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWordBinding
    private var mediaPlayer: MediaPlayer? = null
    private var currentWordIndex = 0
    private var words: List<WordEntity> = emptyList()
    private var lessonId: Long = -1
    private lateinit var tts: TextToSpeech
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    private val RECORD_AUDIO_PERMISSION_CODE = 101

    private var attemptCount = 0
    private val maxAttempts = 5

    private var sessionId: String = ""

    private var score = 0
    private var totalWords = 0
    private var scoreRecordId: Long? = null // For updating the same score record

    private var wordScored = false

    private val wordResults = mutableListOf<WordResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lessonId = intent.getLongExtra("lessonId", -1L)
        if (lessonId == -1L) {
            Toast.makeText(this, "Invalid lesson ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("WordActivity", "Language not supported")
                }
            } else {
                Log.e("WordActivity", "TTS initialization failed")
                Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show()
            }
        }

        fetchWords(lessonId)

        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
        }

        binding.recordPronunciationButton.setOnClickListener {
            startRecording()
            binding.recordPronunciationButton.visibility = View.GONE
            binding.stopRecordingButton.visibility = View.VISIBLE
        }

        binding.stopRecordingButton.setOnClickListener {
            stopRecording()
            binding.recordPronunciationButton.visibility = View.VISIBLE
            binding.stopRecordingButton.visibility = View.GONE
        }

        // Generate sessionId ONCE per session
        sessionId = UUID.randomUUID().toString()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Record Audio permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Record Audio permission denied. Cannot record pronunciation.", Toast.LENGTH_LONG).show()
                binding.recordPronunciationButton.isEnabled = false
            }
        }
    }

    private fun fetchWords(lessonId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.getApi(this@WordActivity).getWordsByLessonId(lessonId)
                if (response.isSuccessful) {
                    words = response.body() ?: emptyList()
                    totalWords = words.size
                    score = 0
                    withContext(Dispatchers.Main) {
                        updateScoreTracker() // <-- Move here!
                        if (words.isNotEmpty()) {
                            updateUI()
                        } else {
                            Toast.makeText(this@WordActivity, "No words found for this lesson", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                } else {
                    val errorMessage =
                        "Failed to load words: ${response.code()}, Message: ${response.message()}, Body: ${response.errorBody()?.string()}"
                    Log.e("WordActivity", errorMessage)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WordActivity, "Failed to load words", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Error fetching words: ${e.message}"
                Log.e("WordActivity", errorMessage, e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@WordActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun updateUI() {
        if (currentWordIndex < words.size) {
            val currentWord = words[currentWordIndex]
            binding.lessonNameTextView.text = "Lesson: ${currentWord.lesson.name}"
            binding.wordTextView.text = currentWord.word

            // Add this line to update the word counter
            binding.wordCounterTextView.text = "Word: ${currentWordIndex + 1}/$totalWords"

            val baseUrl = "http://10.0.2.2:8080"
            val imageUrl = if (currentWord.imageURL?.startsWith("/") == true) {
                baseUrl + currentWord.imageURL
            } else {
                baseUrl + "/" + (currentWord.imageURL ?: "")
            }
            Log.d("WordActivity", "Loading image: $imageUrl")

            Glide.with(this)
                .load(imageUrl)
                .into(binding.wordImageView)

            binding.playAudioButton.setOnClickListener {
                currentWord.audioURL?.let { url ->
                    val audioUrl = if (url.startsWith("/")) {
                        baseUrl + url
                    } else {
                        baseUrl + "/" + url
                    }
                    Log.d("WordActivity", "Playing audio: $audioUrl")
                    speakWord(currentWord.word)
                } ?: run {
                    Toast.makeText(this@WordActivity, "Audio not available", Toast.LENGTH_SHORT).show()
                    Log.e("WordActivity", "audioURL is null")
                }
            }

            // Reset attempts for new word
            attemptCount = 0
            wordScored = false
            binding.nextWordButton.isEnabled = false
            binding.recordPronunciationButton.isEnabled = true
            binding.stopRecordingButton.isEnabled = true

            // Show attempts left
            binding.attemptCounterTextView.text = "Attempts left: ${maxAttempts - attemptCount}"

            // Save or update score at the start of the session
            if (currentWordIndex == 0) {
                sendScoreToBackend()
            }
        } else {
            showSessionEndDialog()
        }
    }

    private fun playSound(audioUrl: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer?.setDataSource(audioUrl)
            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener {
                it.start()
            }
            mediaPlayer?.setOnErrorListener { _, what, extra ->
                Log.e("WordActivity", "Error playing audio: what=$what, extra=$extra")
                Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show()
                true
            }
        } catch (e: IOException) {
            Log.e("WordActivity", "Error setting data source: ${e.message}", e)
            Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakWord(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        mediaRecorder?.release()
        mediaRecorder = null
    }

    fun nextWord(view: View) {
        currentWordIndex++
        updateUI()
    }

    private fun startRecording() {
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Record Audio permission not granted.", Toast.LENGTH_SHORT).show()
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
            return
        }

        // Create a unique file name for MP4 (AAC)
        val fileName = UUID.randomUUID().toString() + ".mp4"
        val recordingDir = getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)
        if (recordingDir == null) {
            Log.e("WordActivity", "Cannot get recording directory")
            Toast.makeText(this, "Cannot access storage for recording.", Toast.LENGTH_SHORT).show()
            return
        }
        audioFile = File(recordingDir, fileName)
        Log.d("WordActivity", "Recording to: ${audioFile?.absolutePath}")

        // Set up MediaRecorder for MP4/AAC
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioChannels(1)
            setAudioSamplingRate(16000)
            setOutputFile(audioFile?.absolutePath)
            try {
                prepare()
                start()
                Toast.makeText(this@WordActivity, "Recording started...", Toast.LENGTH_SHORT).show()
                Log.d("WordActivity", "MediaRecorder prepared and started.")
            } catch (e: IOException) {
                Log.e("WordActivity", "prepare() failed: ${e.message}", e)
                Toast.makeText(this@WordActivity, "Recording preparation failed", Toast.LENGTH_SHORT).show()
                binding.recordPronunciationButton.visibility = View.VISIBLE
                binding.stopRecordingButton.visibility = View.GONE
            } catch (e: IllegalStateException) {
                Log.e("WordActivity", "IllegalStateException during recording: ${e.message}", e)
                Toast.makeText(this@WordActivity, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.recordPronunciationButton.visibility = View.VISIBLE
                binding.stopRecordingButton.visibility = View.GONE
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
                Log.d("WordActivity", "MediaRecorder stopped and released.")
                Toast.makeText(this@WordActivity, "Recording stopped. Sending for validation...", Toast.LENGTH_SHORT).show()
            } catch (e: RuntimeException) {
                Log.e("WordActivity", "Stop/release failed: ${e.message}", e)
                Toast.makeText(this@WordActivity, "Recording stop failed. Audio might be corrupt.", Toast.LENGTH_SHORT).show()
                binding.recordPronunciationButton.visibility = View.VISIBLE
                binding.stopRecordingButton.visibility = View.GONE
                audioFile?.delete()
                return
            }
        }
        mediaRecorder = null
        if (audioFile != null && audioFile!!.exists() && audioFile!!.length() > 1000) { // Ensure file is at least 1KB
            Log.d("WordActivity", "Audio file size: ${audioFile!!.length()} bytes")
            CoroutineScope(Dispatchers.IO).launch {
                sendAudioForValidation(audioFile!!)
            }
        } else {
            Log.e("WordActivity", "Audio file invalid: exists=${audioFile?.exists()}, size=${audioFile?.length()}")
            Toast.makeText(this@WordActivity, "No audio recorded or file is too small.", Toast.LENGTH_SHORT).show()
            binding.recordPronunciationButton.visibility = View.VISIBLE
            binding.stopRecordingButton.visibility = View.GONE
            audioFile?.delete()
        }
    }

    private suspend fun sendAudioForValidation(audioFile: File) {
        try {
            val currentWord = words[currentWordIndex]
            val wordId = currentWord.wordId
            Log.d("WordActivity", "Sending pronunciation check for wordId: $wordId, word: ${currentWord.word}")

            val requestFile = audioFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)
            Log.d("WordActivity", "Sending audio file: ${audioFile.name}, size: ${audioFile.length()} bytes, type: audio/mp4")

            val response = RetrofitInstance.getApi(this@WordActivity).checkPronunciation(wordId, audioPart)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val pronunciationCheckResponse = response.body()
                    if (pronunciationCheckResponse != null) {
                        attemptCount++ // Increment on every attempt

                        // Always add the result when the word is finished (correct or max attempts)
                        if (pronunciationCheckResponse.correct && !wordScored) {
                            wordResults.add(WordResult(currentWord.word, true, attemptCount))
                        } else if (attemptCount == maxAttempts && !wordScored) {
                            wordResults.add(WordResult(currentWord.word, false, attemptCount))
                        }

                        if (pronunciationCheckResponse.correct) {
                            if (!wordScored) {
                                score++
                                wordScored = true
                                updateScoreTracker()
                                sendScoreToBackend()
                            }
                            Toast.makeText(this@WordActivity, "Correct Pronunciation!", Toast.LENGTH_SHORT).show()
                            binding.nextWordButton.isEnabled = true
                            binding.recordPronunciationButton.isEnabled = false
                            binding.stopRecordingButton.isEnabled = false
                        } else {
                            if (attemptCount < maxAttempts) {
                                Toast.makeText(
                                    this@WordActivity,
                                    "Incorrect. Attempt $attemptCount of $maxAttempts. Try again.",
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.nextWordButton.isEnabled = false
                            } else {
                                Toast.makeText(
                                    this@WordActivity,
                                    "Sorry, you pronounced the word $maxAttempts times. Moving to next word.",
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.nextWordButton.isEnabled = true
                                binding.recordPronunciationButton.isEnabled = false
                                binding.stopRecordingButton.isEnabled = false
                            }
                        }
                        // Always update the counter after increment
                        val attemptsLeft = (maxAttempts - attemptCount).coerceAtLeast(0)
                        binding.attemptCounterTextView.text = "Attempts left: $attemptsLeft"

                        Log.d("WordActivity", "Transcribed: ${pronunciationCheckResponse.transcribedText}")
                        sendPronunciationAttemptToBackend(pronunciationCheckResponse.correct)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Failed to check pronunciation: ${response.code()}, ${response.message()}, Body: $errorBody"
                    Log.e("WordActivity", errorMessage)
                    Toast.makeText(this@WordActivity, "Error: ${errorBody ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                }
                audioFile.delete()
            }
        } catch (e: Exception) {
            Log.e("WordActivity", "Error sending audio: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@WordActivity, "Error sending audio: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
            audioFile.delete()
        }
    }

    private fun sendPronunciationAttemptToBackend(isCorrect: Boolean) {
        val currentWord = words[currentWordIndex]
        val accuracy = when {
            isCorrect -> 100 / attemptCount
            attemptCount >= maxAttempts -> 0
            else -> 0
        }

        val attemptDTO = PronounciationAttemptPostDTO(
            wordId = currentWord.wordId,
            lessonId = currentWord.lesson.lessonId,
            accuracy = accuracy.toDouble(),
            isCorrect = isCorrect,
            attemptNumber = attemptCount,
            sessionId = sessionId
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.getApi(this@WordActivity).createPronounciationAttempt(attemptDTO)
                if (!response.isSuccessful) {
                    Log.e("WordActivity", "Failed to save attempt: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("WordActivity", "Error saving attempt: ${e.message}", e)
            }
        }
    }

    private fun sendScoreToBackend() {
        val scoreDTO = ScoreRecordDTO(
            lessonId = lessonId,
            score = score.toDouble(),
            attemptsDuration = 0L, // Replace with actual duration if you track it
            correctWords = score,
            incorrectWords = totalWords - score,
            sessionId = sessionId
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.getApi(this@WordActivity).createScoreRecord(scoreDTO)
                // Optionally handle response
            } catch (e: Exception) {
                Log.e("WordActivity", "Error saving score: ${e.message}", e)
            }
        }
    }

    private fun updateScoreTracker() {
        binding.scoreTrackerTextView.text = "$score/$totalWords"
    }

    private fun showSessionEndDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        if (score >= 6) {
            builder.setTitle("Congratulations!")
                .setMessage("You scored $score/$totalWords. Proceed to next level?")
                .setPositiveButton("Proceed to Next Level") { _, _ -> /* TODO: Go to next level */ }
                .setNegativeButton("Try Again") { _, _ -> restartSession() }
                .setNeutralButton("View Score Details") { _, _ ->
                    val intent = Intent(this, ViewScoreActivity::class.java)
                    intent.putExtra("lessonId", lessonId)
                    intent.putExtra("sessionId", sessionId)
                    intent.putParcelableArrayListExtra("wordResults", ArrayList(wordResults))
                    startActivity(intent)
                    finish()
                }
        } else {
            builder.setTitle("Try Again")
                .setMessage("You scored $score/$totalWords. You need at least 6 points to proceed.")
                .setPositiveButton("Try Again") { _, _ -> restartSession() }
                .setNeutralButton("View Score Details") { _, _ -> // <-- Add this block
                    val intent = Intent(this, ViewScoreActivity::class.java)
                    intent.putExtra("lessonId", lessonId)
                    intent.putExtra("sessionId", sessionId)
                    intent.putParcelableArrayListExtra("wordResults", ArrayList(wordResults))
                    startActivity(intent)
                    finish()
                }
        }
        builder.show()
    }

    private fun restartSession() {
        currentWordIndex = 0
        score = 0
        // Generate a new sessionId for a new session
        sessionId = UUID.randomUUID().toString()
        updateScoreTracker()
        updateUI()
    }
}