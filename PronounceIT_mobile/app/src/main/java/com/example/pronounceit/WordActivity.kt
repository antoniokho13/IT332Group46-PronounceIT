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
            Toast.makeText(this, "Invalid lesson ID", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show()
            }
        }

        fetchWords(lessonId)

        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
        }

        binding.recordPronunciationButton.setOnClickListener {
            startRecording()
            startRecordingAnimation()
        }

        binding.stopRecordingButton.setOnClickListener {
            stopRecording()
            stopRecordingAnimation()
        }

        // Generate sessionId ONCE per session
        sessionId = UUID.randomUUID().toString()
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
                    val wordsList = response.body() ?: emptyList()
                    words = wordsList
                    totalWords = words.size
                    withContext(Dispatchers.Main) {
                        updateUI()
                        updateScoreTracker()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WordActivity, "Failed to fetch words: ${response.code()}", Toast.LENGTH_SHORT).show()
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
            binding.wordTextView.text = currentWord.word.uppercase()

            // Add this line to update the word counter
            binding.wordCounterTextView.text = "Word: ${currentWordIndex + 1}/$totalWords"

            val baseUrl = "http://192.168.113.197:8080"
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
                speakWord(currentWord.word)
            }

            // Reset attempts for new word
            attemptCount = 0
            wordScored = false

            // Reset button visibility - show play audio, hide next word
            binding.playAudioButton.visibility = View.VISIBLE
            binding.nextWordButton.visibility = View.GONE

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

    // Method to play the correct sound effect and show confetti
    private fun playCorrectSound() {
        try {
            // Release any previous instance
            correctSoundEffect?.release()

            // Create and play the sound effect
            correctSoundEffect = MediaPlayer.create(this, R.raw.correct)
            correctSoundEffect?.setOnCompletionListener { it.release() }
            correctSoundEffect?.start()

            // Show confetti animation
            showConfettiAnimation()
        } catch (e: Exception) {
            Log.e("WordActivity", "Error playing correct sound: ${e.message}", e)
        }
    }

    // Show confetti animation for correct pronunciation
    private fun showConfettiAnimation() {
        konfettiView.build()
            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.RED)
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(2000L)
            .addShapes(Shape.Square, Shape.Circle)
            .addSizes(Size(8), Size(12), Size(16))
            .setPosition(
                konfettiView.width / 2f,
                konfettiView.height / 2f
            )
            .burst(300)
    }

    // Method to play the error sound effect
    private fun playErrorSound() {
        try {
            // Release any previous instance
            errorSoundEffect?.release()

            // Create and play the sound effect
            errorSoundEffect = MediaPlayer.create(this, R.raw.error)
            errorSoundEffect?.setOnCompletionListener { it.release() }
            errorSoundEffect?.start()
        } catch (e: Exception) {
            Log.e("WordActivity", "Error playing error sound: ${e.message}", e)
        }
    }

    private fun speakWord(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        correctSoundEffect?.release()
        correctSoundEffect = null
        errorSoundEffect?.release()
        errorSoundEffect = null
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        mediaRecorder?.release()
        mediaRecorder = null
        micAnimation?.stop()
    }

    fun nextWord(view: View) {
        currentWordIndex++
        // Reset button visibility when moving to next word
        binding.playAudioButton.visibility = View.VISIBLE
        binding.nextWordButton.visibility = View.GONE
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
        val recordingDir = getExternalFilesDir("recordings") // Using custom directory name
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
                // Toast now shows recording is in progress
                Toast.makeText(this@WordActivity, "Recording in progress...", Toast.LENGTH_SHORT).show()
                Log.d("WordActivity", "MediaRecorder prepared and started.")
            } catch (e: IOException) {
                Log.e("WordActivity", "prepare() failed: ${e.message}", e)
                Toast.makeText(this@WordActivity, "Recording preparation failed", Toast.LENGTH_SHORT).show()
                stopRecordingAnimation()
            } catch (e: IllegalStateException) {
                Log.e("WordActivity", "IllegalStateException during recording: ${e.message}", e)
                Toast.makeText(this@WordActivity, "Recording failed: ${e.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@WordActivity, "Recording stopped. Sending for validation...", Toast.LENGTH_SHORT).show()
            } catch (e: RuntimeException) {
                Log.e("WordActivity", "Stop/release failed: ${e.message}", e)
                Toast.makeText(this@WordActivity, "Recording stop failed. Audio might be corrupt.", Toast.LENGTH_SHORT).show()
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
                            }

                            // Play the correct sound effect
                            playCorrectSound()

                            Toast.makeText(this@WordActivity, "Correct Pronunciation!", Toast.LENGTH_SHORT).show()

                            // Switch from play button to next button
                            binding.playAudioButton.visibility = View.GONE
                            binding.nextWordButton.visibility = View.VISIBLE

                            binding.recordPronunciationButton.isEnabled = false
                            binding.stopRecordingButton.isEnabled = false
                        } else {
                            // Play the error sound effect for incorrect pronunciation
                            playErrorSound()

                            if (attemptCount < maxAttempts) {
                                // Fixed: Remove direct feedback reference
                                Toast.makeText(this@WordActivity, "Try again. Attempts left: ${maxAttempts - attemptCount}", Toast.LENGTH_SHORT).show()
                            } else {
                                if (!wordScored) {
                                    wordScored = true // Mark as scored to prevent duplicate entries even if not correct
                                }
                                Toast.makeText(this@WordActivity, "Maximum attempts reached. Moving to next word.", Toast.LENGTH_SHORT).show()
                                binding.playAudioButton.visibility = View.GONE
                                binding.nextWordButton.visibility = View.VISIBLE

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
        binding.scoreTrackerTextView.text = "Score: $score/$totalWords"
    }

    private fun showSessionEndDialog() {
        // Save or update score at the end of the session
        sendScoreToBackend()

        // Create a custom view for the dialog with an ImageView
        val dialogView = layoutInflater.inflate(R.layout.dialog_session_end, null)
        val congratsImage = dialogView.findViewById<android.widget.ImageView>(R.id.congratsImageView)
        val scoreMessage = dialogView.findViewById<android.widget.TextView>(R.id.scoreMessageTextView)
        val returnToLessonsButton = dialogView.findViewById<android.widget.ImageButton>(R.id.returnToLessonsButton)
        val tryAgainButton = dialogView.findViewById<android.widget.ImageButton>(R.id.tryAgainButton)
        val viewScoreDetailsButton = dialogView.findViewById<android.widget.ImageButton>(R.id.viewScoreDetailsButton)

        // Find the return button layout by ID to properly hide/show both button and text
        val returnButtonLayout = dialogView.findViewById<android.view.View>(R.id.returnButtonLayout)

        // Set image based on score (half or more = congratulations, less than half = game over)
        if (score >= totalWords / 2) {
            congratsImage.setImageResource(R.drawable.congratulations)
            congratsImage.visibility = View.VISIBLE
            scoreMessage.text = "You scored $score/$totalWords. Return to lessons?"

            // Mark lesson as completed for this user
            val prefs = getSharedPreferences("PronounceItPrefs", Context.MODE_PRIVATE)
            val userId = prefs.getLong("userId", -1L)
            val lessonPrefs = getSharedPreferences("lesson_prefs", Context.MODE_PRIVATE)
            val key = "completed_lessons_user_$userId"
            val set = lessonPrefs.getStringSet(key, emptySet())!!.toMutableSet()
            set.add(lessonId.toString())
            lessonPrefs.edit().putStringSet(key, set).apply()

            // Show return button and its text
            returnButtonLayout.visibility = View.VISIBLE
        } else {
            congratsImage.setImageResource(R.drawable.gameover)
            congratsImage.visibility = View.VISIBLE
            scoreMessage.text = "You scored $score/$totalWords. You need at least ${totalWords/2} points to proceed."

            // Hide the entire return button layout (both button and text)
            returnButtonLayout.visibility = View.GONE
        }

        // Create and configure the dialog
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Set button click listeners
        returnToLessonsButton.setOnClickListener {
            // Return to LessonActivity and refresh
            val intent = Intent(this, LessonActivity::class.java)
            intent.putExtra("categoryId", getIntent().getLongExtra("categoryId", -1L))
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
            dialog.dismiss()
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

        // Update the button content descriptions for accessibility
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
}