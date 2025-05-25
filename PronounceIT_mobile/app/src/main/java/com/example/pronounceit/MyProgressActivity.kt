package com.example.pronounceit

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.LessonEntity
import com.example.pronounceit.network.models.ScoreRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyProgressActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myprogress)

        // Make sure your layout has these IDs
        val listView = findViewById<ListView>(R.id.progressListView)
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)

        val userId = getSharedPreferences("PronounceItPrefs", Context.MODE_PRIVATE)
            .getLong("userId", -1L)
        if (userId == -1L) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val lessonPrefs = getSharedPreferences("lesson_prefs", Context.MODE_PRIVATE)
        val completedLessons = lessonPrefs.getStringSet("completed_lessons_user_$userId", emptySet())!!
            .mapNotNull { it.toLongOrNull() }
            .toSet()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch all lessons (so we can get names)
                val lessonsResponse = RetrofitInstance.getApi(this@MyProgressActivity).getAllLessons()
                if (!lessonsResponse.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MyProgressActivity, "Failed to load lessons", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                val allLessons = lessonsResponse.body() ?: emptyList<LessonEntity>()
                val passedLessons = allLessons.filter { it.lessonId in completedLessons }

                // For each passed lesson, get the latest score record
                val data = mutableListOf<Map<String, String>>()
                for (lesson in passedLessons) {
                    // Fetch latest score for this lesson and user
                    val scoreResponse = RetrofitInstance.getApi(this@MyProgressActivity)
                        .getLatestScoreRecord(userId, lesson.lessonId)
                    val scoreRecord: ScoreRecordEntity? = if (scoreResponse.isSuccessful) scoreResponse.body() else null
                    val scoreText = if (scoreRecord != null)
                        "Score: ${scoreRecord.correctWords}/${scoreRecord.correctWords + scoreRecord.incorrectWords}"
                    else
                        "Score: N/A"
                    data.add(
                        mapOf(
                            "lesson" to lesson.name,
                            "score" to scoreText
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    if (data.isEmpty()) {
                        listView.visibility = View.GONE
                        rootLayout.removeAllViews()
                        val emptyText = TextView(this@MyProgressActivity)
                        emptyText.text = "No completed lessons yet."
                        emptyText.textSize = 18f
                        emptyText.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                        emptyText.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        rootLayout.addView(emptyText)
                    } else {
                        listView.visibility = View.VISIBLE
                        val adapter = SimpleAdapter(
                            this@MyProgressActivity,
                            data,
                            android.R.layout.simple_list_item_2,
                            arrayOf("lesson", "score"),
                            intArrayOf(android.R.id.text1, android.R.id.text2)
                        )
                        listView.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                Log.e("MyProgressActivity", "Error loading progress", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MyProgressActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}