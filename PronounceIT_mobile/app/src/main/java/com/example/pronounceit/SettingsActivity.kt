package com.example.pronounceit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var myProgressButton: Button
    private lateinit var editUserProfileButton: Button
    private lateinit var volumeToggleButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize buttons
        myProgressButton = findViewById(R.id.myProgressButton)
        editUserProfileButton = findViewById(R.id.editUserProfileButton)
        volumeToggleButton = findViewById(R.id.volumeToggleButton)

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        myProgressButton.setOnClickListener {
            val intent = Intent(this, MyProgressActivity::class.java)
            startActivity(intent)
        }

        editUserProfileButton.setOnClickListener {
            val intent = Intent(this, EditUserProfileActivity::class.java)
            startActivity(intent)
        }

        volumeToggleButton.setOnClickListener {
            // Toggle volume logic or open volume settings activity
            // Example: Toast.makeText(this, "Volume toggled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
