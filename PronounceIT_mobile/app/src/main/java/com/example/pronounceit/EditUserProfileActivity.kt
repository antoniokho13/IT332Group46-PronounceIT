package com.example.pronounceit

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.UserResponse
import com.example.pronounceit.network.models.UpdateUserRequest
import kotlinx.coroutines.launch

class EditUserProfileActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var editProfileButton: Button
    private lateinit var saveProfileButton: Button
    private lateinit var backButton: Button
    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var sharedPreferences: SharedPreferences
    private var userId: Long = -1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edituserprofile)

        // Initialize UI components
        firstNameEditText = findViewById(R.id.firstName)
        lastNameEditText = findViewById(R.id.lastName)
        emailEditText = findViewById(R.id.email)
        editProfileButton = findViewById(R.id.editProfile)
        saveProfileButton = findViewById(R.id.saveProfile)
        backButton = findViewById(R.id.backButton)
        oldPasswordEditText = findViewById(R.id.oldPassword)
        newPasswordEditText = findViewById(R.id.newPassword)
        confirmPasswordEditText = findViewById(R.id.confirmPassword)

        // Access SharedPreferences
        sharedPreferences = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)

        // Get user ID from SharedPreferences
        userId = sharedPreferences.getLong("userId", -1)

        if (userId == -1L) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load existing user data from backend
        loadUserProfile()

        // Set up button click listeners
        editProfileButton.setOnClickListener {
            showPasswordFields()
        }

        saveProfileButton.setOnClickListener {
            updateUserProfile()
        }

        backButton.setOnClickListener {
            hidePasswordFields()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                val token = sharedPreferences.getString("token", "") ?: ""
                val response = RetrofitInstance.getApi(this@EditUserProfileActivity)
                    .getUserById(userId, "Bearer $token")

                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        firstNameEditText.setText(user.firstName)
                        lastNameEditText.setText(user.lastName)
                        emailEditText.setText(user.email)
                    } else {
                        Toast.makeText(this@EditUserProfileActivity, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@EditUserProfileActivity, "Failed to load profile: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditUserProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPasswordFields() {
        oldPasswordEditText.visibility = View.VISIBLE
        newPasswordEditText.visibility = View.VISIBLE
        confirmPasswordEditText.visibility = View.VISIBLE
        saveProfileButton.visibility = View.VISIBLE
        backButton.visibility = View.VISIBLE
        editProfileButton.visibility = View.GONE
    }

    private fun hidePasswordFields() {
        oldPasswordEditText.visibility = View.GONE
        newPasswordEditText.visibility = View.GONE
        confirmPasswordEditText.visibility = View.GONE
        saveProfileButton.visibility = View.GONE
        backButton.visibility = View.GONE
        editProfileButton.visibility = View.VISIBLE

        // Clear any entered password data for security
        oldPasswordEditText.setText("")
        newPasswordEditText.setText("")
        confirmPasswordEditText.setText("")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUserProfile() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val oldPassword = oldPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        // Basic validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Only validate password if the user is attempting to change it
        if (oldPasswordEditText.visibility == View.VISIBLE) {
            if (oldPassword.isEmpty()) {
                Toast.makeText(this, "Please enter your current password", Toast.LENGTH_SHORT).show()
                return
            }

            if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
                Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show()
                return
            }
        }

        lifecycleScope.launch {
            try {
                val token = sharedPreferences.getString("token", "") ?: ""

                val updateRequest = UpdateUserRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = if (newPassword.isNotEmpty()) newPassword else null,
                    oldPassword = if (oldPassword.isNotEmpty()) oldPassword else null
                )

                val response = RetrofitInstance.getApi(this@EditUserProfileActivity)
                    .updateUser(userId, updateRequest)

                if (response.isSuccessful) {
                    // Update the SharedPreferences with new user info
                    sharedPreferences.edit().apply {
                        putString("firstName", firstName)
                        putString("lastName", lastName)
                        putString("email", email)
                        apply()
                    }

                    Toast.makeText(this@EditUserProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditUserProfileActivity, "Failed to update profile: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditUserProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        // If in edit mode, go back to view mode instead of closing activity
        if (saveProfileButton.visibility == View.VISIBLE) {
            hidePasswordFields()
        } else {
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}