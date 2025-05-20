package com.example.pronounceit

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pronounceit.databinding.ActivityWordBinding
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.WordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

class WordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWordBinding
    private var mediaPlayer: MediaPlayer? = null
    private var currentWordIndex = 0
    private var words: List<WordEntity> = emptyList()
    private var lessonId: Long = -1

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

        fetchWords(lessonId)
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
                            finish() // Consider finishing the activity if no words are found.
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
            // Ensure correct URL for image
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
                    // Ensure correct URL for audio
                    val audioUrl = if (url.startsWith("/")) {
                        baseUrl + url
                    } else {
                        baseUrl + "/" + url
                    }
                    Log.d("WordActivity", "Playing audio: $audioUrl")
                    playSound(audioUrl)
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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // Function to go to the next word
    fun nextWord(view: android.view.View) {
        currentWordIndex++
        updateUI()
    }
}

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        // Don't add Authorization header for static resources
        if (url.contains("/images/") || url.contains("/audio/")) {
            return chain.proceed(request)
        }
        val sharedPreferences = context.getSharedPreferences("PronounceItPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        val requestBuilder = request.newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}
