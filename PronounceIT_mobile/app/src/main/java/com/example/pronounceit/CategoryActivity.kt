package com.example.pronounceit

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CategoryActivity : AppCompatActivity() {

    private lateinit var animalsButton: Button
    private lateinit var foodButton: Button
    private lateinit var placesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        animalsButton = findViewById(R.id.animalsButton)
        foodButton = findViewById(R.id.foodButton)
        placesButton = findViewById(R.id.placesButton)

        animalsButton.setOnClickListener {
            Toast.makeText(this, "Animals selected", Toast.LENGTH_SHORT).show()
        }

        foodButton.setOnClickListener {
            Toast.makeText(this, "Food selected", Toast.LENGTH_SHORT).show()
        }

        placesButton.setOnClickListener {
            Toast.makeText(this, "Places selected", Toast.LENGTH_SHORT).show()
        }
    }
}
