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
import com.example.pronounceit.network.models.WordResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewScoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewscore)

        // Apply bounce animation to the score details title image
        val scoreDetailsTitle = findViewById<ImageView>(R.id.scoreDetailsTitle)
        val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_bounce)
        scoreDetailsTitle.startAnimation(bounceAnimation)

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
            val totalScoreView = layoutInflater.inflate(R.layout.score_header_view, null) as LinearLayout
            val scoreTextView = totalScoreView.findViewById<TextView>(R.id.scoreText)
            val correct = wordResults.count { it.correct }
            val total = wordResults.size
            scoreTextView.text = "$correct/$total"
            rootLayout.addView(totalScoreView, 1)

            val data = wordResults.map {
                mapOf(
                    "word" to it.word,
                    "result" to if (it.correct) "Correct" else "Incorrect",
                    "attempts" to "Attempts: ${it.attempts}"
                )
            }

            val adapter = ScoreAdapter(this, data)
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

                        val totalScoreView = layoutInflater.inflate(R.layout.score_header_view, null) as LinearLayout
                        val scoreTextView = totalScoreView.findViewById<TextView>(R.id.scoreText)

                        if (scoreRecord != null) {
                            scoreTextView.text = "${scoreRecord.correctWords}/${scoreRecord.correctWords + scoreRecord.incorrectWords}"
                        } else {
                            scoreTextView.text = "N/A"
                        }
                        rootLayout.addView(totalScoreView, 1)

                        val data = attempts.map {
                            mapOf(
                                "word" to it.word.word,
                                "result" to if (it.isCorrect) "Correct" else "Incorrect",
                                "attempts" to "Attempts: ${it.attemptNumber}"
                            )
                        }

                        val adapter = ScoreAdapter(this@ViewScoreActivity, data)
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
        
        // Setup back button
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }
    }

    class ScoreAdapter(
        private val context: Context,
        private val data: List<Map<String, String>>
    ) : BaseAdapter() {

        override fun getCount(): Int = data.size
        override fun getItem(position: Int): Any = data[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.score_item_view, parent, false)

            val item = data[position]
            val wordText = view.findViewById<TextView>(R.id.wordText)
            val resultText = view.findViewById<TextView>(R.id.resultText)
            val attemptsText = view.findViewById<TextView>(R.id.attemptsText)
            val resultIcon = view.findViewById<ImageView>(R.id.resultIcon)

            wordText.text = item["word"]
            resultText.text = item["result"]
            attemptsText.text = item["attempts"]

            // Set icon based on result
            if (item["result"] == "Correct") {
                resultIcon.setImageResource(R.drawable.ic_check)
                resultIcon.setColorFilter(Color.GREEN)
                view.setBackgroundResource(R.drawable.correct_item_background)
            } else {
                resultIcon.setImageResource(android.R.drawable.ic_delete)
                resultIcon.setColorFilter(Color.RED)
                view.setBackgroundResource(R.drawable.incorrect_item_background)
            }

            return view
        }
    }
}