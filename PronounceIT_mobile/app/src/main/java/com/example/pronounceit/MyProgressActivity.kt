package com.example.pronounceit

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class MyProgressActivity : AppCompatActivity() {
    private lateinit var rootLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myprogress)

        // Initialize rootLayout (now a ConstraintLayout)
        rootLayout = findViewById(R.id.rootLayout)

        // Apply zoom animation to the progress image (consistent with CategoryActivity)
        val progressImage = findViewById<ImageView>(R.id.progressImage)
        val zoomAnimation = AnimationUtils.loadAnimation(this, R.anim.category_zoom)
        progressImage.startAnimation(zoomAnimation)

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
        val clickSound = MediaPlayer.create(this, R.raw.button_click)

        fun playClickSound() {
            if (clickSound.isPlaying) {
                clickSound.seekTo(0)
            }
            clickSound.start()
        }

        val achievementsButton = findViewById<Button>(R.id.achievementsButton)
        val viewScoresButton = findViewById<Button>(R.id.viewScoresButton)

        achievementsButton.setOnClickListener {
            playClickSound()
            val intent = Intent(this, AchievementsActivity::class.java)
            intent.putExtra("userId", getUserId())
            startActivity(intent)
        }

        viewScoresButton.setOnClickListener {
            playClickSound()
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