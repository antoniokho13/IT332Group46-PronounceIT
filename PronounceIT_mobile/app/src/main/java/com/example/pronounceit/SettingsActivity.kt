package com.example.pronounceit

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {
    private lateinit var soundEffectsSwitch: SwitchCompat
    private lateinit var backgroundMusicSwitch: SwitchCompat
    private lateinit var vibrationSwitch: SwitchCompat
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var volumeLabel: TextView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)

        // Initialize UI components
        soundEffectsSwitch = findViewById(R.id.soundEffectsSwitch)
        backgroundMusicSwitch = findViewById(R.id.backgroundMusicSwitch)
        vibrationSwitch = findViewById(R.id.vibrationSwitch)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        volumeLabel = findViewById(R.id.volumeLabel)

        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        soundEffectsSwitch.isChecked = sharedPreferences.getBoolean("soundEffects", true)
        backgroundMusicSwitch.isChecked = sharedPreferences.getBoolean("backgroundMusic", true)
        vibrationSwitch.isChecked = sharedPreferences.getBoolean("vibration", true)

        val volume = sharedPreferences.getInt("volume", 50)
        volumeSeekBar.progress = volume
        volumeLabel.text = "Volume: $volume%"
    }

    private fun setupListeners() {
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                volumeLabel.text = "Volume: $progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onPause() {
        super.onPause()
        saveSettings()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun saveSettings() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("soundEffects", soundEffectsSwitch.isChecked)
        editor.putBoolean("backgroundMusic", backgroundMusicSwitch.isChecked)
        editor.putBoolean("vibration", vibrationSwitch.isChecked)
        editor.putInt("volume", volumeSeekBar.progress)
        editor.apply()
    }
}


