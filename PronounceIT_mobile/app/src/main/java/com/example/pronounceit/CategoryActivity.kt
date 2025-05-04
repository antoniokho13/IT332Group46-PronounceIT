package com.example.pronounceit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
            val intent = Intent(this, LessonActivity::class.java)
            intent.putExtra("category", "Animals")
            startActivity(intent)
        }

        foodButton.setOnClickListener {
            val intent = Intent(this, LessonActivity::class.java)
            intent.putExtra("category", "Food")
            startActivity(intent)
        }

        placesButton.setOnClickListener {
            val intent = Intent(this, LessonActivity::class.java)
            intent.putExtra("category", "Places")
            startActivity(intent)
        }
    }
}