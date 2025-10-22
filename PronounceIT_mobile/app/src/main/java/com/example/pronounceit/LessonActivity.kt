package com.example.pronounceit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pronounceit.databinding.ActivityLessonBinding
import com.example.pronounceit.network.AuthApi
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.LessonEntity
import com.example.pronounceit.adapters.LessonAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers.Main

class LessonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLessonBinding
    private lateinit var lessonAdapter: LessonAdapter
    private lateinit var api: AuthApi
    private var userId: Long = -1
    private var categoryId: Long = -1L
    private var lessonsLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLessonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply zoom in/out animation to the lesson title image
        val zoomAnimation = AnimationUtils.loadAnimation(this, R.anim.lesson_zoom)
        binding.lessonTitle.startAnimation(zoomAnimation)

        // Use the context-aware API instance so AuthInterceptor adds the token
        api = RetrofitInstance.getApi(this)

        // Always get userId from SharedPreferences for the current logged-in user
        val prefs = getSharedPreferences("PronounceItPrefs", Context.MODE_PRIVATE)
        userId = prefs.getLong("userId", -1L)

        categoryId = intent.getLongExtra("categoryId", -1L)

        if (categoryId != -1L) {
            fetchLessons(categoryId)
        } else {
            Toast.makeText(this, "Invalid category ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.resetProgressButton.setOnClickListener {
            clearAllProgressForUser(this@LessonActivity, userId)
            fetchLessons(categoryId, forceRefresh = true)
            Toast.makeText(this@LessonActivity, "All progress cleared!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchLessons(categoryId: Long, forceRefresh: Boolean = false) {
        // Skip if lessons already loaded and we're not forcing a refresh
        if (lessonsLoaded && !forceRefresh) {
            return
        }

        CoroutineScope(IO).launch {
            try {
                Log.d("LessonActivity", "Attempting to fetch lessons for category $categoryId from: ${RetrofitInstance.getBaseUrl()}/api/categories/$categoryId/lessons")
                val response = api.getLessonsByCategoryId(categoryId)
                
                if (response.isSuccessful) {
                    val lessons = response.body() ?: emptyList()
                    Log.d("LessonActivity", "Successfully loaded ${lessons.size} lessons for category $categoryId")
                    val completedLessons = getCompletedLessons(this@LessonActivity, userId)
                    val sortedLessons = lessons.sortedBy { it.sequence } // <-- changed here
                    var foundFirstLocked = false
                    for (lesson in sortedLessons) {
                        if (lesson.lessonId in completedLessons) {
                            lesson.locked = false
                        } else if (!foundFirstLocked) {
                            lesson.locked = false
                            foundFirstLocked = true
                        } else {
                            lesson.locked = true
                        }
                    }
                    Log.d("LessonActivity", "Completed lessons: $completedLessons")
                    for (lesson in sortedLessons) {
                        Log.d("LessonActivity", "Lesson: ${lesson.lessonId}, sequence: ${lesson.sequence}, locked: ${lesson.locked}")
                    }
                    withContext(Main) {
                        if (lessons.isNotEmpty()) {
                            setupRecyclerView(sortedLessons)
                        } else {
                            Toast.makeText(
                                this@LessonActivity,
                                "No lessons found for this category",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        // Mark lessons as loaded to prevent unnecessary API calls
                        lessonsLoaded = true
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

        lessonAdapter.onItemClick = { lesson: LessonEntity ->
            val intent = Intent(this@LessonActivity, WordActivity::class.java).apply {
                putExtra("lessonId", lesson.lessonId)
                putExtra("categoryId", categoryId)
            }
            startActivity(intent)
        }
    }

    private fun getCompletedLessons(context: Context, userId: Long): Set<Long> {
        val prefs = context.getSharedPreferences("lesson_prefs", Context.MODE_PRIVATE)
        val key = "completed_lessons_user_$userId"
        return prefs.getStringSet(key, emptySet())!!.map { it.toLong() }.toSet()
    }

    private fun markLessonCompleted(context: Context, lessonId: Long, userId: Long) {
        val prefs = context.getSharedPreferences("lesson_prefs", Context.MODE_PRIVATE)
        val key = "completed_lessons_user_$userId"
        val set = prefs.getStringSet(key, emptySet())!!.toMutableSet()
        set.add(lessonId.toString())
        prefs.edit().putStringSet(key, set).apply()
    }

    private suspend fun resetProgressForUser(context: Context, userId: Long): Boolean {
        val prefs = context.getSharedPreferences("lesson_prefs", Context.MODE_PRIVATE)
        val key = "completed_lessons_user_$userId"
        val categoryId = intent.getLongExtra("categoryId", -1L)
        val response = api.getLessonsByCategoryId(categoryId)
        return if (response.isSuccessful) {
            val lessons = response.body() ?: emptyList()
            val sorted = lessons.sortedBy { it.sequence }
            val firstLessonId = sorted.firstOrNull()?.lessonId
            val set = mutableSetOf<String>()
            if (firstLessonId != null) set.add(firstLessonId.toString())
            prefs.edit().putStringSet(key, set).apply()
            Log.d("LessonActivity", "After reset: " + prefs.getStringSet(key, emptySet()))
            true
        } else {
            false
        }
    }

    private suspend fun resetAllLessonsForUser(context: Context, userId: Long): Boolean {
        val prefs = context.getSharedPreferences("lesson_prefs", Context.MODE_PRIVATE)
        val key = "completed_lessons_user_$userId"
        // Unlock only the lesson with the lowest sequence across ALL categories
        val response = api.getAllLessons() // You need to implement this endpoint if not present
        return if (response.isSuccessful) {
            val lessons = response.body() ?: emptyList()
            val sorted = lessons.sortedBy { it.sequence }
            val firstLessonId = sorted.firstOrNull()?.lessonId
            val set = mutableSetOf<String>()
            if (firstLessonId != null) set.add(firstLessonId.toString())
            prefs.edit().putStringSet(key, set).apply()
            true
        } else {
            false
        }
    }

    private fun clearAllProgressForUser(context: Context, userId: Long) {
        val prefs = context.getSharedPreferences("lesson_prefs", Context.MODE_PRIVATE)
        val key = "completed_lessons_user_$userId"
        prefs.edit().remove(key).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::lessonAdapter.isInitialized) {
            lessonAdapter.releaseResources()
        }
    }

    override fun onResume() {
        super.onResume()
        // Only fetch lessons if they haven't been loaded yet
        // This prevents unnecessary API calls when returning to this activity
        if (!lessonsLoaded) {
            val categoryId = intent.getLongExtra("categoryId", -1L)
            if (categoryId != -1L) {
                fetchLessons(categoryId)
            }
        }
    }
}
