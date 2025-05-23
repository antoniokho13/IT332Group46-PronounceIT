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

        firstNameEditText = findViewById(R.id.firstName)
        lastNameEditText = findViewById(R.id.lastName)
        emailEditText = findViewById(R.id.email)
        editProfileButton = findViewById(R.id.editProfile)
        saveProfileButton = findViewById(R.id.saveProfile)
        backButton = findViewById(R.id.backButton)
        oldPasswordEditText = findViewById(R.id.oldPassword)
        newPasswordEditText = findViewById(R.id.newPassword)
        confirmPasswordEditText = findViewById(R.id.confirmPassword)

        sharedPreferences = getSharedPreferences("PronounceItPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getLong("userId", -1)

        if (userId == -1L) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadUserProfile()

        editProfileButton.setOnClickListener {
            showEditMode()
        }

        saveProfileButton.setOnClickListener {
            updateUserProfile()
        }

        backButton.setOnClickListener {
            cancelEditMode()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadUserProfile() {
        val firstName = sharedPreferences.getString("firstName", "")
        val lastName = sharedPreferences.getString("lastName", "")
        val email = sharedPreferences.getString("email", "")

        firstNameEditText.setText(firstName)
        lastNameEditText.setText(lastName)
        emailEditText.setText(email)

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

    private fun showEditMode() {
        // Make password field visible
        oldPasswordEditText.visibility = View.VISIBLE
        newPasswordEditText.visibility = View.VISIBLE
        confirmPasswordEditText.visibility = View.VISIBLE

        // Enable edit fields
        firstNameEditText.isEnabled = true
        lastNameEditText.isEnabled = true
        emailEditText.isEnabled = true

        // Show password message
        Toast.makeText(this, "Please enter your current password to make changes", Toast.LENGTH_SHORT).show()

        // Switch buttons
        saveProfileButton.visibility = View.VISIBLE
        backButton.visibility = View.VISIBLE
        editProfileButton.visibility = View.GONE
    }

    private fun cancelEditMode() {
        // Hide password fields
        oldPasswordEditText.visibility = View.GONE
        newPasswordEditText.visibility = View.GONE
        confirmPasswordEditText.visibility = View.GONE

        // Disable edit fields
        firstNameEditText.isEnabled = false
        lastNameEditText.isEnabled = false
        emailEditText.isEnabled = false

        // Reload original values
        loadUserProfile()

        // Switch buttons back
        saveProfileButton.visibility = View.GONE
        backButton.visibility = View.GONE
        editProfileButton.visibility = View.VISIBLE

        // Clear password fields
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

        // Validate required fields
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate current password is provided (required for any changes)
        if (oldPassword.isEmpty()) {
            Toast.makeText(this, "Current password is required to make changes", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate new password if provided
        if (newPassword.isNotEmpty() && newPassword != confirmPassword) {
            Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val token = sharedPreferences.getString("token", "") ?: ""
                val updateRequest = UpdateUserRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = if (newPassword.isNotEmpty()) newPassword else null,
                    oldPassword = oldPassword // Always required for any changes
                )

                val response = RetrofitInstance.getApi(this@EditUserProfileActivity)
                    .updateUser(userId, updateRequest)

                if (response.isSuccessful) {
                    // Update local preferences with new user data
                    sharedPreferences.edit().apply {
                        putString("firstName", firstName)
                        putString("lastName", lastName)
                        putString("email", email)
                        apply()
                    }

                    Toast.makeText(this@EditUserProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    when (response.code()) {
                        401 -> Toast.makeText(this@EditUserProfileActivity, "Incorrect current password", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this@EditUserProfileActivity, "Failed to update profile: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditUserProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        if (saveProfileButton.visibility == View.VISIBLE) {
            cancelEditMode()
        } else {
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}