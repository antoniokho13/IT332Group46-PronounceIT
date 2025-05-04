package com.example.pronounceit

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LessonActivity : AppCompatActivity() {

    private lateinit var lessonTextView: TextView
    private lateinit var wordsTextView: TextView
    private lateinit var scoreButton: Button
    private lateinit var wordImageView: ImageView
    private lateinit var wordTextView: TextView
    private lateinit var soundButton: ImageView
    private lateinit var microphoneButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson)

        // Initialize views by finding their IDs
        lessonTextView = findViewById(R.id.lessonTextView)
        wordsTextView = findViewById(R.id.wordsTextView)
        scoreButton = findViewById(R.id.scoreButton)
        wordImageView = findViewById(R.id.wordImageView)
        wordTextView = findViewById(R.id.wordTextView)
        soundButton = findViewById(R.id.soundButton)
        microphoneButton = findViewById(R.id.microphoneButton)

        // You can now work with these views to display data
        // For example, setting the initial lesson and word count:
        lessonTextView.text = "Lesson: A" // You might get this from the Intent
        wordsTextView.text = "Words: 1/5" // You might get this from your data

        // For now, let's set some placeholder data for the first word
        wordImageView.setImageResource(R.drawable.ic_launcher_background) // Replace with your actual image resource
        wordTextView.text = "Apple" // Replace with the actual word

        // You can add click listeners to the soundButton and microphoneButton here
        soundButton.setOnClickListener {
            // Play the pronunciation of the word
        }

        microphoneButton.setOnClickListener {
            // Start speech recognition
        }

        // You might also want to retrieve the category from the Intent
        val category = intent.getStringExtra("category")
        if (!category.isNullOrEmpty()) {
            // Use the category to fetch data from your backend (or local data source)
            // and update the UI accordingly.
            // For now, let's just log the category.
            println("Selected Category: $category")
        }
    }
}