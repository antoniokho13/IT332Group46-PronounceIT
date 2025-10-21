package com.example.pronounceit

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pronounceit.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailedScoresActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var rootLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_scores)

        // Start zoom animation for myscore.png
        val myscoreImage = findViewById<ImageView>(R.id.myscoreImage)
        val zoomAnim = AnimationUtils.loadAnimation(this, R.anim.category_zoom)
        myscoreImage.startAnimation(zoomAnim)

        // Initialize views
        rootLayout = findViewById(R.id.rootLayout)
        listView = findViewById(R.id.scoresListView)

        val userId = intent.getLongExtra("userId", -1L)
        if (userId == -1L) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUserScores(userId)
    }

    private fun loadUserScores(userId: Long) {
        // Get completed lessons from SharedPreferences (for status display)
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

                // Fetch scores for ALL lessons (not just completed ones)
                val data = mutableListOf<Map<String, Any>>()
                var totalScore = 0.0
                var lessonCount = 0

                for (lesson in allLessons) {
                    val scoreResponse = RetrofitInstance.getApi(this@DetailedScoresActivity)
                        .getLatestScoreRecord(userId, lesson.lessonId)

                    val scoreRecord = if (scoreResponse.isSuccessful) scoreResponse.body() else null

                    // Display score if ANY score record exists (passed or failed)
                    if (scoreRecord != null) {
                        val correctWords = scoreRecord.correctWords
                        val totalWords = scoreRecord.correctWords + scoreRecord.incorrectWords
                        val score = (correctWords.toDouble() / totalWords.toDouble()) * 100

                        totalScore += score
                        lessonCount++

                        // Determine if passed
                        val isPassed = lesson.lessonId in completedLessons

                        data.add(mapOf(
                            "lesson" to lesson.name,
                            "score" to score,
                            "correctWords" to correctWords,
                            "incorrectWords" to scoreRecord.incorrectWords,
                            "isPassed" to isPassed
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
        data: List<Map<String, Any>>,
        totalScore: Double,
        lessonCount: Int
    ) {
        withContext(Dispatchers.Main) {
            if (data.isEmpty()) {
                // Add header showing no lessons
                val headerView = layoutInflater.inflate(R.layout.score_header_view, null) as LinearLayout
                val scoreTextView = headerView.findViewById<TextView>(R.id.scoreText)
                scoreTextView.text = "No attempted lessons yet"
                rootLayout.addView(headerView, 1)

                listView.visibility = View.GONE
                return@withContext
            }

            // Add average score header
            val averageScore = totalScore / lessonCount
            val headerView = layoutInflater.inflate(R.layout.score_header_view, null) as LinearLayout
            val scoreTextView = headerView.findViewById<TextView>(R.id.scoreText)
            scoreTextView.text = "Average: ${String.format("%.1f", averageScore)}%"
            rootLayout.addView(headerView, 1)

            listView.visibility = View.VISIBLE

            val adapter = DetailedScoreAdapter(this@DetailedScoresActivity, data)
            listView.adapter = adapter
        }
    }

    class DetailedScoreAdapter(
        private val context: Context,
        private val data: List<Map<String, Any>>
    ) : BaseAdapter() {

        override fun getCount(): Int = data.size
        override fun getItem(position: Int): Any = data[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.detailed_score_item_view, parent, false)

            val item = data[position]
            val lessonNameText = view.findViewById<TextView>(R.id.lessonNameText)
            val scoreText = view.findViewById<TextView>(R.id.scoreText)
            val detailsText = view.findViewById<TextView>(R.id.detailsText)
            val statusIcon = view.findViewById<ImageView>(R.id.statusIcon)

            val lessonName = item["lesson"] as String
            val score = item["score"] as Double
            val correctWords = item["correctWords"] as Int
            val incorrectWords = item["incorrectWords"] as Int
            val isPassed = item["isPassed"] as Boolean

            lessonNameText.text = lessonName
            scoreText.text = "${String.format("%.1f", score)}%"
            detailsText.text = "Correct: $correctWords | Incorrect: $incorrectWords"

            // Set icon and background based on pass/fail status
            if (isPassed) {
                statusIcon.setImageResource(R.drawable.ic_check)
                statusIcon.setColorFilter(Color.GREEN)
                view.setBackgroundResource(R.drawable.correct_item_background)
            } else {
                statusIcon.setImageResource(android.R.drawable.ic_delete)
                statusIcon.setColorFilter(Color.RED)
                view.setBackgroundResource(R.drawable.incorrect_item_background)
            }

            return view
        }
    }
}