package com.example.pronounceit

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.LoginRequest
import com.example.pronounceit.network.models.LoginResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var buttonClickSound: MediaPlayer
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize sound effect
        buttonClickSound = MediaPlayer.create(this, R.raw.button_click)

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)

        // Add bouncing animation to logo
        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_bounce)
        logoImageView.startAnimation(bounceAnimation)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {
            // Play sound when button is clicked
            buttonClickSound.start()

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            // Play sound when button is clicked
            buttonClickSound.start()

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun login(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.login(LoginRequest(email, password))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null) {
                            handleSuccessfulLogin(loginResponse)
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleSuccessfulLogin(loginResponse: LoginResponse) {
        Log.d("LoginActivity", "Login successful, userId: ${loginResponse.userId}")

        // Save user info to shared preferences
        val editor = sharedPreferences.edit()
        editor.putString("token", loginResponse.token)
        editor.putLong("userId", loginResponse.userId)
        editor.putString("email", loginResponse.email)
        // Remove references to username if it doesn't exist in LoginResponse
        // editor.putString("username", loginResponse.username ?: "User")
        editor.putString("role", loginResponse.role ?: "STUDENT")
        editor.apply()

        // Create initial streak for user
        createInitialStreak(loginResponse.userId)
    }

    private fun createInitialStreak(userId: Long) {
        Log.d("LoginActivity", "Attempting to create initial streak for userId: $userId")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First create the streak without updating it
                val createStreakResponse = RetrofitInstance.getApi(this@LoginActivity)
                    .createStreak(userId)

                Log.d("LoginActivity", "Create streak response code: ${createStreakResponse.code()}")

                if (createStreakResponse.isSuccessful) {
                    val streakDTO = createStreakResponse.body()
                    Log.d("LoginActivity", "Streak created successfully: $streakDTO")
                    // No immediate update - let streak start at 0

                    // Save initial streak value to shared preferences
                    streakDTO?.let {
                        sharedPreferences.edit().putInt("local_streak", it.currentStreak).apply()
                    }
                } else if (createStreakResponse.code() == 409) {
                    // 409 Conflict - streak already exists, which is fine
                    Log.d("LoginActivity", "Streak already exists for user")

                    // Get the current streak value
                    try {
                        val getStreakResponse = RetrofitInstance.getApi(this@LoginActivity)
                            .getStreak(userId)

                        if (getStreakResponse.isSuccessful) {
                            val existingStreak = getStreakResponse.body()
                            Log.d("LoginActivity", "Existing streak value: $existingStreak")

                            // Save current streak value to shared preferences
                            existingStreak?.let {
                                sharedPreferences.edit().putInt("local_streak", it.currentStreak).apply()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "Error getting existing streak", e)
                    }
                } else {
                    Log.e("LoginActivity", "Failed to create streak: ${createStreakResponse.code()}")
                    if (createStreakResponse.errorBody() != null) {
                        val errorBody = createStreakResponse.errorBody()?.string()
                        Log.e("LoginActivity", "Error body: $errorBody")
                    }
                }

                // Always navigate to HomeActivity, even if streak creation fails
                withContext(Dispatchers.Main) {
                    navigateToHome()
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error creating initial streak", e)
                // Not critical, still proceed to HomeActivity
                withContext(Dispatchers.Main) {
                    navigateToHome()
                }
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release MediaPlayer resources
        if (::buttonClickSound.isInitialized) {
            buttonClickSound.release()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}