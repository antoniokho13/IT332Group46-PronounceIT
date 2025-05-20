package com.example.pronounceit

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pronounceit.adapters.LessonAdapter
import com.example.pronounceit.databinding.ActivityLessonBinding
import com.example.pronounceit.network.AuthApi
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.LessonEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers.Main

class LessonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLessonBinding
    private lateinit var lessonAdapter: LessonAdapter
    private lateinit var api: AuthApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLessonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        api = RetrofitInstance.api

        val categoryId = intent.getLongExtra("categoryId", -1L)
        if (categoryId != -1L) {
            fetchLessons(categoryId)
        } else {
            Toast.makeText(this, "Invalid category ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchLessons(categoryId: Long) {
        CoroutineScope(IO).launch {
            try {
                // Use the new endpoint to get lessons by category ID
                val response = api.getLessonsByCategoryId(categoryId)  // Corrected line
                if (response.isSuccessful) {
                    val lessons = response.body() ?: emptyList()
                    withContext(Main) {
                        if (lessons.isNotEmpty()) {
                            setupRecyclerView(lessons)
                        } else {
                            Toast.makeText(
                                this@LessonActivity,
                                "No lessons found for this category",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    withContext(Main) {
                        Toast.makeText(
                            this@LessonActivity,
                            "Failed to load lessons: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(
                            "LessonActivity",
                            "Failed to load lessons. Response Code: ${response.code()}, Body: ${response.errorBody()?.string()}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("LessonActivity", "Error fetching lessons", e)
                withContext(Main) {
                    Toast.makeText(this@LessonActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun setupRecyclerView(lessons: List<LessonEntity>) {
        lessonAdapter = LessonAdapter(this, lessons)
        binding.lessonRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LessonActivity)
            adapter = lessonAdapter
        }
    }
}