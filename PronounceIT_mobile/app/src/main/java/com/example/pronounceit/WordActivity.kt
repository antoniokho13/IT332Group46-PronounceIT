package com.example.pronounceit

import android.content.Context
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
import android.media.AudioFormat
import android.media.AudioRecord
import com.example.pronounceit.utils.WavUtil
import java.io.FileOutputStream
import java.io.RandomAccessFile

class WordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWordBinding
    private var mediaPlayer: MediaPlayer? = null
    private var currentWordIndex = 0
    private var words: List<WordEntity> = emptyList()
    private var lessonId: Long = -1
    private lateinit var tts: TextToSpeech
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var wavFile: File? = null

    private val RECORD_AUDIO_PERMISSION_CODE = 101

    private var recordingStartTime: Long = 0

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
                    withContext(Dispatchers.Main) {
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
        } else {
            Toast.makeText(this, "End of words", Toast.LENGTH_SHORT).show()
            finish()
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

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        val recordingDir = getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)
        if (recordingDir == null) {
            Log.e("WordActivity", "Cannot get recording directory")
            Toast.makeText(this, "Cannot access storage for recording.", Toast.LENGTH_SHORT).show()
            return
        }
        wavFile = File(recordingDir, "recorded_${System.currentTimeMillis()}.wav")
        val outputStream = FileOutputStream(wavFile)
        // Write placeholder header
        WavUtil.writeWavHeader(outputStream, 0)

        isRecording = true
        audioRecord?.startRecording()
        Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show()
        recordingStartTime = System.currentTimeMillis()

        Thread {
            val data = ByteArray(bufferSize)
            var totalAudioLen = 0L
            while (isRecording) {
                val read = audioRecord?.read(data, 0, data.size) ?: 0
                if (read > 0) {
                    outputStream.write(data, 0, read)
                    totalAudioLen += read
                }
            }
            outputStream.close()
            // Re-write header with correct length
            val raf = RandomAccessFile(wavFile, "rw")
            WavUtil.writeWavHeader(raf, totalAudioLen)
            raf.close()
        }.start()
    }

    private fun stopRecording() {
        val duration = System.currentTimeMillis() - recordingStartTime
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        if (duration < 1000) {
            Toast.makeText(this, "Please record at least 1 second.", Toast.LENGTH_SHORT).show()
            wavFile?.delete()
            binding.recordPronunciationButton.visibility = View.VISIBLE
            binding.stopRecordingButton.visibility = View.GONE
            return
        }

        if (wavFile != null && wavFile!!.exists() && wavFile!!.length() > 1000) {
            Log.d("WordActivity", "WAV file size: ${wavFile!!.length()} bytes")
            CoroutineScope(Dispatchers.IO).launch {
                sendAudioForValidation(wavFile!!)
            }
        } else {
            Log.e("WordActivity", "WAV file invalid: exists=${wavFile?.exists()}, size=${wavFile?.length()}")
            Toast.makeText(this, "No audio recorded or file is too small.", Toast.LENGTH_SHORT).show()
            binding.recordPronunciationButton.visibility = View.VISIBLE
            binding.stopRecordingButton.visibility = View.GONE
            wavFile?.delete()
        }
    }

    private suspend fun sendAudioForValidation(audioFile: File) {
        try {
            val currentWord = words[currentWordIndex]
            val wordId = currentWord.wordId
            Log.d("WordActivity", "Sending pronunciation check for wordId: $wordId, word: ${currentWord.word}")

            val requestFile = audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)
            Log.d("WordActivity", "Sending audio file: ${audioFile.name}, size: ${audioFile.length()} bytes, type: audio/wav")

            val response = RetrofitInstance.getApi(this@WordActivity).checkPronunciation(wordId, audioPart)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val pronunciationCheckResponse = response.body()
                    if (pronunciationCheckResponse != null) {
                        if (pronunciationCheckResponse.correct) {
                            Toast.makeText(this@WordActivity, "Correct Pronunciation!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@WordActivity, pronunciationCheckResponse.feedbackMessage, Toast.LENGTH_LONG).show()
                        }
                        Log.d("WordActivity", "Transcribed: ${pronunciationCheckResponse.transcribedText}")
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
}