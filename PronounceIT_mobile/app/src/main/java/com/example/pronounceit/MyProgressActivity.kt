package com.example.pronounceit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MyProgressActivity : AppCompatActivity() {
    private lateinit var rootLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myprogress)

        // Initialize rootLayout
        rootLayout = findViewById(R.id.rootLayout)

        // Apply bounce animation to the progress image
        val progressImage = findViewById<ImageView>(R.id.progressImage)
        val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_bounce)
        progressImage.startAnimation(bounceAnimation)

        // Check if user is logged in
        val userId = getSharedPreferences("PronounceItPrefs", Context.MODE_PRIVATE)
            .getLong("userId", -1L)
        if (userId == -1L) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup navigation buttons
        setupButtons()
    }

    private fun setupButtons() {
        val achievementsButton = findViewById<Button>(R.id.achievementsButton)
        val viewScoresButton = findViewById<Button>(R.id.viewScoresButton)

        achievementsButton.setOnClickListener {
            val intent = Intent(this, AchievementsActivity::class.java)
            intent.putExtra("userId", getUserId())
            startActivity(intent)
        }

        viewScoresButton.setOnClickListener {
            val intent = Intent(this, DetailedScoresActivity::class.java)
            intent.putExtra("userId", getUserId())
            startActivity(intent)
        }
    }

    private fun getUserId(): Long {
        val userId = getSharedPreferences("PronounceItPrefs", Context.MODE_PRIVATE)
            .getLong("userId", -1L)
        if (userId == -1L) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
        }
        return userId
    }
}