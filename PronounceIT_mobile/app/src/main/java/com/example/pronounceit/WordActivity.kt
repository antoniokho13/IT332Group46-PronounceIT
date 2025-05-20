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
                val response = RetrofitInstance.api.getWordsByLessonId(lessonId)
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

            // Load image using Glide
            Glide.with(this)
                .load(currentWord.imageURL)
                .into(binding.wordImageView)

            binding.playAudioButton.setOnClickListener {
                currentWord.audioURL?.let { url ->  // Use let to handle nullability
                    playSound(url)
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
