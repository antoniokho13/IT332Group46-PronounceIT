package com.example.pronounceit

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.PronounciationAttemptEntity
import com.example.pronounceit.network.models.WordResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewScoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewscore)

        val lessonId = intent.getLongExtra("lessonId", -1L)
        val sessionId = intent.getStringExtra("sessionId") ?: ""

        if (lessonId == -1L || sessionId.isBlank()) {
            Toast.makeText(this, "Invalid lesson or session. Cannot load score details.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val rootLayout = findViewById<android.widget.LinearLayout>(R.id.rootLayout)
        val listView = findViewById<ListView>(R.id.scoreListView)

        // Try to get wordResults from intent
        val wordResults = intent.getParcelableArrayListExtra<WordResult>("wordResults")

        if (wordResults != null && wordResults.isNotEmpty()) {
            // Use local results for display
            val totalScoreTextView = TextView(this)
            val correct = wordResults.count { it.correct }
            val total = wordResults.size
            totalScoreTextView.text = "Score: $correct/$total"
            totalScoreTextView.textSize = 20f
            totalScoreTextView.setPadding(32, 32, 32, 32)
            rootLayout.addView(totalScoreTextView, 1)

            val data = wordResults.map {
                mapOf(
                    "word" to it.word,
                    "result" to if (it.correct) "Correct" else "Incorrect",
                    "attempts" to "Attempts: ${it.attempts}"
                )
            }
            val adapter = SimpleAdapter(
                this,
                data,
                android.R.layout.simple_list_item_2,
                arrayOf("word", "result"),
                intArrayOf(android.R.id.text1, android.R.id.text2)
            )
            listView.adapter = adapter
        } else {
            // Fallback to backend if wordResults is not available
            CoroutineScope(Dispatchers.IO).launch {
                val api = RetrofitInstance.getApi(this@ViewScoreActivity)
                val attemptsResponse = api.getAttemptsBySession(lessonId, sessionId)
                val scoreResponse = api.getScoreRecordBySession(lessonId, sessionId)
                withContext(Dispatchers.Main) {
                    if (attemptsResponse.isSuccessful && scoreResponse.isSuccessful) {
                        val attempts = attemptsResponse.body() ?: emptyList()
                        val scoreRecord = scoreResponse.body()

                        val totalScoreTextView = TextView(this@ViewScoreActivity)
                        if (scoreRecord != null) {
                            totalScoreTextView.text = "Score: ${scoreRecord.correctWords}/${scoreRecord.correctWords + scoreRecord.incorrectWords}"
                        } else {
                            totalScoreTextView.text = "Score: N/A"
                        }
                        totalScoreTextView.textSize = 20f
                        totalScoreTextView.setPadding(32, 32, 32, 32)
                        rootLayout.addView(totalScoreTextView, 1)

                        val data = attempts.map {
                            mapOf(
                                "word" to it.word.word,
                                "result" to if (it.isCorrect) "Correct" else "Incorrect",
                                "attempts" to "Attempts: ${it.attemptNumber}"
                            )
                        }
                        val adapter = SimpleAdapter(
                            this@ViewScoreActivity,
                            data,
                            android.R.layout.simple_list_item_2,
                            arrayOf("word", "result"),
                            intArrayOf(android.R.id.text1, android.R.id.text2)
                        )
                        listView.adapter = adapter
                    } else {
                        val errorMsg = "Failed to load score details: " +
                                "Attempts code=${attemptsResponse.code()}, Score code=${scoreResponse.code()}"
                        val errorBody = try { attemptsResponse.errorBody()?.string() } catch (e: Exception) { null }
                        Log.e("ViewScoreActivity", "Error body: $errorBody")
                        Toast.makeText(this@ViewScoreActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}