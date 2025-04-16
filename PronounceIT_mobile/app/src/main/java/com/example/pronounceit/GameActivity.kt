package com.example.pronounceit

import android.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)


        // Your game implementation code will go here
        // This could include loading pronunciation challenges,
        // managing audio playback, recording user attempts, etc.
    }
}