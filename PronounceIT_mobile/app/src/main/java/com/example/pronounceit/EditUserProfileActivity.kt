package com.example.pronounceit

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditUserProfileActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var editProfileButton: Button
    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edituserprofile)

        // Initialize UI components
        firstNameEditText = findViewById(R.id.firstName)
        lastNameEditText = findViewById(R.id.lastName)
        emailEditText = findViewById(R.id.email)
        editProfileButton = findViewById(R.id.editProfile)
        oldPasswordEditText = findViewById(R.id.oldPassword)
        newPasswordEditText = findViewById(R.id.newPassword)
        confirmPasswordEditText = findViewById(R.id.confirmPassword)

        // Access SharedPreferences
        sharedPreferences = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)

        // Load existing user data
        loadUserProfile()

        // Set up button click listener
        editProfileButton.setOnClickListener {
            showPasswordFields()
        }
    }

    private fun loadUserProfile() {
        val firstName = sharedPreferences.getString("firstName", "")
        val lastName = sharedPreferences.getString("lastName", "")
        val email = sharedPreferences.getString("email", "")

        firstNameEditText.setText(firstName)
        lastNameEditText.setText(lastName)
        emailEditText.setText(email)
    }

    private fun showPasswordFields() {
        oldPasswordEditText.visibility = View.VISIBLE
        newPasswordEditText.visibility = View.VISIBLE
        confirmPasswordEditText.visibility = View.VISIBLE
        Toast.makeText(this, "Enter your password details", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}