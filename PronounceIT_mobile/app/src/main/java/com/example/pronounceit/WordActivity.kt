package com.example.pronounceit

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
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
import java.io.File
import java.util.UUID
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import android.content.pm.PackageManager
import android.graphics.Color
import com.example.pronounceit.network.models.PronounciationAttemptPostDTO
import com.example.pronounceit.network.models.ScoreRecordDTO
import nl.dionsegijn.konfetti.KonfettiView
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size

class WordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWordBinding
    private var mediaPlayer: MediaPlayer? = null
    private var currentWordIndex = 0
    private var words: List<WordEntity> = emptyList()
    private var lessonId: Long = -1
    private lateinit var tts: TextToSpeech
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false

    // Animation properties
    private var micAnimation: AnimationDrawable? = null
    private var textBlinkAnimation: Animation? = null
    private lateinit var konfettiView: KonfettiView

    // Sound effects
    private var correctSoundEffect: MediaPlayer? = null
    private var errorSoundEffect: MediaPlayer? = null

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

        // Initialize konfetti view
        konfettiView = binding.konfettiView

        val animatedBorder = binding.imageFrameLayout.background as? AnimationDrawable
        animatedBorder?.start()

        lessonId = intent.getLongExtra("lessonId", -1L)
        if (lessonId == -1L) {
            finish()
            return
        }

        // Set up text blinking animation
        textBlinkAnimation = AlphaAnimation(1.0f, 0.0f).apply {
            duration = 500
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("WordActivity", "Language not supported")
                }
            } else {
                Log.e("WordActivity", "TTS initialization failed")
            }
        }

        fetchWords(lessonId)

        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }

        // Show and animate the curved "Press Me" text when idle
        binding.curvedTextView.visibility = View.VISIBLE
        binding.curvedTextView.startBlinkAnimation()

        binding.recordPronunciationButton.setOnClickListener {
            startRecording()
            startRecordingAnimation()
            // Hide and stop animation when recording starts
            binding.curvedTextView.visibility = View.GONE
            binding.curvedTextView.stopBlinkAnimation()
            // Enable stop button while recording
            binding.stopRecordingButton.isEnabled = true
            binding.recordPronunciationButton.isEnabled = false
        }

        binding.stopRecordingButton.setOnClickListener {
            stopRecording()
            stopRecordingAnimation()
            // Show and animate again when recording stops
            binding.curvedTextView.visibility = View.VISIBLE
            binding.curvedTextView.startBlinkAnimation()
            // Toggle record/stop availability
            binding.stopRecordingButton.isEnabled = false
            binding.recordPronunciationButton.isEnabled = true
        }

        // Play audio for current word (either URL or TTS fallback)
        binding.playAudioButton.setOnClickListener {
            val current = words.getOrNull(currentWordIndex)
            if (current != null) {
                val audioUrl = current.audioURL
                if (!audioUrl.isNullOrBlank()) {
                    try {
                        mediaPlayer?.release()
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(if (audioUrl.startsWith("/")) RetrofitInstance.getBaseUrl() + audioUrl else audioUrl)
                            prepareAsync()
                            setOnPreparedListener { it.start() }
                        }
                    } catch (e: Exception) {
                        Log.e("WordActivity", "Error playing remote audio: ${e.message}", e)
                        tts.speak(current.word, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                } else {
                    tts.speak(current.word, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        }

        // Generate sessionId ONCE per session
        sessionId = UUID.randomUUID().toString()
    }

    // Call this when the lesson session is complete (after last word)
    private fun saveSessionScore() {
        // Build ScoreRecordDTO expected by backend
        val attemptsDuration = 0L // If you track start/end time, replace with actual duration
        val correctWords = wordResults.count { it.correct }
        val incorrectWords = wordResults.count { !it.correct }

        val scoreDTO = ScoreRecordDTO(
            lessonId = lessonId,
            score = if (totalWords > 0) score.toDouble() else 0.0,
            attemptsDuration = attemptsDuration,
            correctWords = correctWords,
            incorrectWords = incorrectWords,
            sessionId = sessionId
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = RetrofitInstance.getApi(this@WordActivity)
                val response = api.createScoreRecord(scoreDTO)
                if (response.isSuccessful) {
                    // After successful save, update accumulated points only by the delta (new best)
                    updateAccumulatedPoints()
                    // Also notify backend of lesson completion so streak can be updated
                    try {
                        val prefs = getSharedPreferences("PronounceItPrefs", Context.MODE_PRIVATE)
                        val userId = prefs.getLong("userId", -1L)
                        if (userId != -1L) {
                            val dateStr = java.time.LocalDate.now().toString()
                            val streakResp = RetrofitInstance.getApi(this@WordActivity).markStreakActivity(userId, dateStr)
                            if (streakResp.isSuccessful) {
                                Log.d("WordActivity", "Streak updated: ${streakResp.body()}")
                            } else {
                                Log.e("WordActivity", "Failed to update streak: ${streakResp.code()} ${streakResp.errorBody()?.string()}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("WordActivity", "Error updating streak: ${e.message}", e)
                    }
                } else {
                    Log.e(
                        "WordActivity",
                        "Failed to save score: ${response.code()} ${response.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("WordActivity", "Error saving score: ${e.message}", e)
            }
        }
    }

    private fun updateAccumulatedPoints() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Read user id from same prefs used elsewhere in the app
                val prefs = getSharedPreferences("PronounceItPrefs", Context.MODE_PRIVATE)
                val userId = prefs.getLong("userId", -1L)
                if (userId == -1L) {
                    Log.e("WordActivity", "User ID not found in preferences")
                    return@launch
                }

                val currentLessonPoints = score * 10

                // Fetch previous best for this user & lesson
                val bestResp = RetrofitInstance.getApi(this@WordActivity).getLatestScoreRecord(userId, lessonId)
                if (bestResp.isSuccessful && bestResp.body() != null) {
                    val best = bestResp.body()!!
                    val previousBestPoints = best.correctWords * 10
                    if (currentLessonPoints > previousBestPoints) {
                        val pointsToAdd = currentLessonPoints - previousBestPoints
                        val pointsRequest = mapOf("points" to pointsToAdd)
                        val updateResp = RetrofitInstance.getApi(this@WordActivity).addPointsToUser(userId, pointsRequest)
                        if (updateResp.isSuccessful) {
                            Log.d("WordActivity", "Added $pointsToAdd points for improved lesson score")
                            withContext(Dispatchers.Main) {
                                android.widget.Toast.makeText(this@WordActivity, "New best score! +${pointsToAdd} points", android.widget.Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Log.e("WordActivity", "Failed to add points: ${updateResp.errorBody()?.string()}")
                        }
                    } else {
                        Log.d("WordActivity", "No points added; currentLessonPoints=$currentLessonPoints previousBestPoints=$previousBestPoints")
                    }
                } else {
                    // No previous best — award full points
                    val pointsRequest = mapOf("points" to currentLessonPoints)
                    val updateResp = RetrofitInstance.getApi(this@WordActivity).addPointsToUser(userId, pointsRequest)
                    if (updateResp.isSuccessful) {
                        Log.d("WordActivity", "Awarded first-attempt points: $currentLessonPoints")
                        withContext(Dispatchers.Main) {
                            android.widget.Toast.makeText(this@WordActivity, "Lesson completed! Earned ${currentLessonPoints} points!", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e("WordActivity", "Failed to add first-attempt points: ${updateResp.errorBody()?.string()}")
                    }
                }

            } catch (e: Exception) {
                Log.e("WordActivity", "Error updating accumulated points: ${e.message}", e)
            }
        }
    }

    private fun startRecordingAnimation() {
        // Hide the recording indicator text and its space
        binding.recordingIndicatorText.clearAnimation()
        binding.recordingIndicatorText.visibility = View.GONE

        binding.stopRecordingButton.setImageResource(R.drawable.mic_recording_animation)
        binding.recordPronunciationButton.visibility = View.GONE
        binding.stopRecordingButton.visibility = View.VISIBLE

        micAnimation = binding.stopRecordingButton.drawable as AnimationDrawable
        micAnimation?.start()

        val scaleX = ObjectAnimator.ofFloat(binding.stopRecordingButton, "scaleX", 1f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.stopRecordingButton, "scaleY", 1f, 1.1f, 1f)
        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatCount = ValueAnimator.INFINITE
        scaleX.duration = 1000
        scaleY.duration = 1000
        scaleX.start()
        scaleY.start()
    }

    private fun stopRecordingAnimation() {
        micAnimation?.stop()
        binding.recordingIndicatorText.clearAnimation()
        binding.recordingIndicatorText.visibility = View.GONE
        binding.recordPronunciationButton.visibility = View.VISIBLE
        binding.stopRecordingButton.visibility = View.GONE
        binding.stopRecordingButton.animate().cancel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                binding.recordPronunciationButton.isEnabled = false
            }
        }
    }

    private fun fetchWords(lessonId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response =
                    RetrofitInstance.getApi(this@WordActivity).getWordsByLessonId(lessonId)
                if (response.isSuccessful) {
                    val wordsList = response.body() ?: emptyList()
                    words = wordsList
                    totalWords = words.size
                    withContext(Dispatchers.Main) {
                        updateUI()
                        updateScoreTracker()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        finish()
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "Error fetching words: ${e.message}"
                Log.e("WordActivity", errorMessage, e)
                withContext(Dispatchers.Main) {
                    finish()
                }
            }
        }
    }

    private fun updateUI() {
        if (currentWordIndex < words.size) {
            val currentWord = words[currentWordIndex]
            binding.lessonNameTextView.text = "Lesson: ${currentWord.lesson.name}"
            binding.wordTextView.text = currentWord.word.uppercase()

            // Update the word counter
            binding.wordCounterTextView.text = "Word: ${currentWordIndex + 1}/$totalWords"

            val baseUrl = RetrofitInstance.getBaseUrl()
            val imageUrl = currentWord.imageURL?.let { url ->
                if (url.startsWith("/")) baseUrl + url else url
            }

            if (!imageUrl.isNullOrBlank()) {
                Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .into(binding.wordImageView)
            } else {
                binding.wordImageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Reset per-word UI state: hide next, show play, enable recording, reset attempts
            attemptCount = 0
            wordScored = false
            binding.playAudioButton.visibility = View.VISIBLE
            binding.nextWordButton.visibility = View.GONE
            binding.recordPronunciationButton.isEnabled = true
            binding.stopRecordingButton.isEnabled = false
            // Use updateScoreTracker to keep attempts/score rendering in one place
            updateScoreTracker()
        } else {
            // No words available
            binding.lessonNameTextView.text = "Lesson: -"
            binding.wordTextView.text = ""
            binding.wordCounterTextView.text = "Word: 0/0"
            binding.nextWordButton.visibility = View.GONE
        }
    }

    // Update score tracker and attempts left on the UI
    private fun updateScoreTracker() {
        // Protect against division by zero / empty list
        val total = if (totalWords > 0) totalWords else 0
        binding.scoreTrackerTextView.text = "Score: $score/$total"
        val attemptsLeft = (maxAttempts - attemptCount).coerceAtLeast(0)
        binding.attemptCounterTextView.text = "Attempts left: " + attemptsLeft
    }

    private fun startRecording() {
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
            return
        }

        // Create a unique file name for MP4 (AAC)
        val fileName = UUID.randomUUID().toString() + ".mp4"
        val recordingDir = getExternalFilesDir("recordings") // Using custom directory name
        if (recordingDir == null) {
            Log.e("WordActivity", "Cannot get recording directory")
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
                Log.d("WordActivity", "MediaRecorder prepared and started.")
            } catch (e: IOException) {
                Log.e("WordActivity", "prepare() failed: ${e.message}", e)
                stopRecordingAnimation()
            } catch (e: IllegalStateException) {
                Log.e("WordActivity", "IllegalStateException during recording: ${e.message}", e)
                stopRecordingAnimation()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
                Log.d("WordActivity", "MediaRecorder stopped and released.")
            } catch (e: RuntimeException) {
                Log.e("WordActivity", "Stop/release failed: ${e.message}", e)
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
            audioFile?.delete()
        }
    }

    private fun showCorrectPronunciationCelebration() {
        // Play the correct sound
        playCorrectSound()

        // Display confetti effect
        konfettiView.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.CYAN)
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(1500L)
            .addShapes(Shape.Square, Shape.Circle)
            .addSizes(Size(8), Size(12))
            .setPosition(konfettiView.width / 2f, konfettiView.height / 2f)
            .burst(200)
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
                            }

                            // Show confetti celebration for correct pronunciation
                            showCorrectPronunciationCelebration()

                            // Switch from play button to next button
                            binding.playAudioButton.visibility = View.GONE
                            binding.nextWordButton.visibility = View.VISIBLE

                            binding.recordPronunciationButton.isEnabled = false
                            binding.stopRecordingButton.isEnabled = false
                        } else {
                            // Play the error sound effect for incorrect pronunciation
                            playErrorSound()

                            if (attemptCount >= maxAttempts) {
                                if (!wordScored) {
                                    wordScored = true // Mark as scored to prevent duplicate entries even if not correct
                                }
                                binding.playAudioButton.visibility = View.GONE
                                binding.nextWordButton.visibility = View.VISIBLE

                                binding.recordPronunciationButton.isEnabled = false
                                binding.stopRecordingButton.isEnabled = false
                            }
                        }
                        // Always update the counter after increment
                        val attemptsLeft = (maxAttempts - attemptCount).coerceAtLeast(0)
                        binding.attemptCounterTextView.text = "Attempts left: " + attemptsLeft

                        Log.d("WordActivity", "Transcribed: ${pronunciationCheckResponse.transcribedText}")
                        sendPronunciationAttemptToBackend(pronunciationCheckResponse.correct)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Failed to check pronunciation: ${response.code()}, ${response.message()}, Body: $errorBody"
                    Log.e("WordActivity", errorMessage)
                }
                audioFile.delete()
            }
        } catch (e: Exception) {
            Log.e("WordActivity", "Error sending audio: ${e.message}", e)
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
                // After saving score, update accumulated points
                updateAccumulatedPoints()
            } catch (e: Exception) {
                Log.e("WordActivity", "Error saving score: ${e.message}", e)
            }
        }
    }

    private fun playCorrectSound() {
        try {
            // Prefer app bundled correct.mp3
            val resId = resources.getIdentifier("correct", "raw", packageName)
            if (resId != 0) {
                correctSoundEffect?.release()
                correctSoundEffect = MediaPlayer.create(this, resId)
                correctSoundEffect?.start()
            } else {
                // Fallback to ToneGenerator
                val tg = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
                tg.startTone(android.media.ToneGenerator.TONE_PROP_ACK, 150)
                CoroutineScope(Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(200)
                    try { tg.release() } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.e("WordActivity", "playCorrectSound failed: ${e.message}", e)
        }
    }

    private fun playErrorSound() {
        try {
            val resId = resources.getIdentifier("error", "raw", packageName)
            if (resId != 0) {
                errorSoundEffect?.release()
                errorSoundEffect = MediaPlayer.create(this, resId)
                errorSoundEffect?.start()
            } else {
                val tg = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
                tg.startTone(android.media.ToneGenerator.TONE_PROP_NACK, 200)
                CoroutineScope(Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(250)
                    try { tg.release() } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.e("WordActivity", "playErrorSound failed: ${e.message}", e)
        }
    }


    private fun showSessionEndDialog() {
        sendScoreToBackend()

        val dialogView = layoutInflater.inflate(R.layout.dialog_session_end, null)
        val congratsImage = dialogView.findViewById<android.widget.ImageView>(R.id.congratsImageView)
        val scoreMessage = dialogView.findViewById<android.widget.TextView>(R.id.scoreMessageTextView)
        val nextLessonButton = dialogView.findViewById<android.widget.ImageButton>(R.id.nextLessonButton)
        val tryAgainButton = dialogView.findViewById<android.widget.ImageButton>(R.id.tryAgainButton)
        val viewScoreDetailsButton = dialogView.findViewById<android.widget.ImageButton>(R.id.viewScoreDetailsButton)
        val nextButtonLayout = dialogView.findViewById<android.view.View>(R.id.nextButtonLayout)

        if (score >= totalWords / 2) {
            congratsImage.setImageResource(R.drawable.congratulations)
            congratsImage.visibility = View.VISIBLE
            scoreMessage.text = "You scored $score/$totalWords. Proceed to next lesson?"

            // Mark lesson as completed for this user
            val prefs = getSharedPreferences("PronounceItPrefs", Context.MODE_PRIVATE)
            val userId = prefs.getLong("userId", -1L)
            val lessonPrefs = getSharedPreferences("lesson_prefs", Context.MODE_PRIVATE)
            val key = "completed_lessons_user_$userId"
            val set = lessonPrefs.getStringSet(key, emptySet())!!.toMutableSet()
            set.add(lessonId.toString())
            lessonPrefs.edit().putStringSet(key, set).apply()

            // Mark lesson as completed for streak tracking
            val streakPrefs = getSharedPreferences("PronounceItPrefs", Context.MODE_PRIVATE)
            streakPrefs.edit().putBoolean("lesson_completed", true).apply()

            nextButtonLayout.visibility = View.VISIBLE
        } else {
            congratsImage.setImageResource(R.drawable.gameover)
            congratsImage.visibility = View.VISIBLE
            scoreMessage.text = "You scored $score/$totalWords. You need at least ${totalWords/2} points to proceed."
            nextButtonLayout.visibility = View.GONE
        }

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        nextLessonButton.setOnClickListener {
            // Find the next lesson by sequence
            CoroutineScope(Dispatchers.IO).launch {
                val api = RetrofitInstance.getApi(this@WordActivity)
                val response = api.getLessonsByCategoryId(intent.getLongExtra("categoryId", -1L))
                if (response.isSuccessful) {
                    val lessons = response.body()?.sortedBy { it.sequence } ?: emptyList()
                    val currentIndex = lessons.indexOfFirst { it.lessonId == lessonId }
                    val nextLesson = if (currentIndex != -1 && currentIndex + 1 < lessons.size) lessons[currentIndex + 1] else null
                    withContext(Dispatchers.Main) {
                        if (nextLesson != null) {
                            val intent = Intent(this@WordActivity, WordActivity::class.java)
                            intent.putExtra("lessonId", nextLesson.lessonId)
                            intent.putExtra("categoryId", nextLesson.category.categoryId)
                            startActivity(intent)
                            finish()
                        } else {
                            val intent = Intent(this@WordActivity, LessonActivity::class.java)
                            intent.putExtra("categoryId", getIntent().getLongExtra("categoryId", -1L))
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(intent)
                            finish()
                        }
                        dialog.dismiss()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
                    }
                }
            }
        }

        tryAgainButton.setOnClickListener {
            restartSession()
            dialog.dismiss()
        }

        viewScoreDetailsButton.setOnClickListener {
            val intent = Intent(this, ViewScoreActivity::class.java)
            intent.putExtra("lessonId", lessonId)
            intent.putExtra("sessionId", sessionId)
            intent.putParcelableArrayListExtra("wordResults", ArrayList(wordResults))
            startActivity(intent)
            finish()
            dialog.dismiss()
        }

        tryAgainButton.contentDescription = "Try Again"
        viewScoreDetailsButton.contentDescription = "View Score"

        dialog.show()
    }

    private fun restartSession() {
        currentWordIndex = 0
        score = 0
        // Generate a new sessionId for a new session
        sessionId = UUID.randomUUID().toString()
        updateScoreTracker()
        updateUI()
    }

    // Called by the next button in layout (android:onClick="nextWord")
    fun nextWord(view: View) {
        // Advance to next word
        currentWordIndex++

        // If we've reached the end of the words, show the session end dialog
        if (currentWordIndex >= totalWords) {
            // Ensure UI reflects final score
            updateScoreTracker()

            // Show end-of-session dialog (which will save score)
            showSessionEndDialog()
            return
        }

        // Otherwise update UI for next word
        updateUI()
        updateScoreTracker()
    }
}
