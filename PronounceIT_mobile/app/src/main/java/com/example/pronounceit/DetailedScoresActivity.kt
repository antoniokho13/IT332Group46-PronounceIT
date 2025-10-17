package com.example.pronounceit

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pronounceit.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailedScoresActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var averageScoreText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_scores)

        // Start zoom animation for myscore.png
        val myscoreImage = findViewById<ImageView>(R.id.myscoreImage)
        val zoomAnim = AnimationUtils.loadAnimation(this, R.anim.category_zoom)
        myscoreImage.startAnimation(zoomAnim)

        // Initialize views
        listView = findViewById(R.id.scoresListView)
        averageScoreText = findViewById(R.id.averageScoreText)

        val userId = intent.getLongExtra("userId", -1L)
        if (userId == -1L) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUserScores(userId)
    }

    private fun loadUserScores(userId: Long) {
        // Get completed lessons from SharedPreferences
        val lessonPrefs = getSharedPreferences("lesson_prefs", Context.MODE_PRIVATE)
        val completedLessons = lessonPrefs.getStringSet("completed_lessons_user_$userId", emptySet())!!
            .mapNotNull { it.toLongOrNull() }
            .toSet()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch all lessons
                val lessonsResponse = RetrofitInstance.getApi(this@DetailedScoresActivity).getAllLessons()
                if (!lessonsResponse.isSuccessful) {
                    showError("Failed to load lessons")
                    return@launch
                }

                val allLessons = lessonsResponse.body() ?: emptyList()
                val passedLessons = allLessons.filter { it.lessonId in completedLessons }

                // Fetch scores for completed lessons
                val data = mutableListOf<Map<String, String>>()
                var totalScore = 0.0
                var lessonCount = 0

                for (lesson in passedLessons) {
                    val scoreResponse = RetrofitInstance.getApi(this@DetailedScoresActivity)
                        .getLatestScoreRecord(userId, lesson.lessonId)

                    val scoreRecord = if (scoreResponse.isSuccessful) scoreResponse.body() else null

                    if (scoreRecord != null) {
                        val correctWords = scoreRecord.correctWords
                        val totalWords = scoreRecord.correctWords + scoreRecord.incorrectWords
                        val score = (correctWords.toDouble() / totalWords.toDouble()) * 100

                        totalScore += score
                        lessonCount++

                        data.add(mapOf(
                            "lesson" to lesson.name,
                            "details" to buildString {
                                append("Score: ${String.format("%.1f", score)}%")
                                append(" - Correct: $correctWords")
                                append(", Incorrect: ${scoreRecord.incorrectWords}")
                            }
                        ))
                    }
                }

                updateUI(data, totalScore, lessonCount)
            } catch (e: Exception) {
                Log.e("DetailedScoresActivity", "Error loading scores", e)
                showError("Error: ${e.message}")
            }
        }
    }

    private suspend fun showError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@DetailedScoresActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun updateUI(
        data: List<Map<String, String>>,
        totalScore: Double,
        lessonCount: Int
    ) {
        withContext(Dispatchers.Main) {
            if (data.isEmpty()) {
                averageScoreText.text = "No completed lessons yet"
                listView.visibility = View.GONE
                return@withContext
            }

            val averageScore = totalScore / lessonCount
            averageScoreText.text = "Average Score: ${String.format("%.1f", averageScore)}%"
            listView.visibility = View.VISIBLE

            val adapter = SimpleAdapter(
                this@DetailedScoresActivity,
                data,
                android.R.layout.simple_list_item_2,
                arrayOf("lesson", "details"),
                intArrayOf(android.R.id.text1, android.R.id.text2)
            )
            listView.adapter = adapter
        }
    }
}