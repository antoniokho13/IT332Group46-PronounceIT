package com.example.pronounceit

import android.os.Bundle
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity

class AchievementsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        val listView = findViewById<ListView>(R.id.achievementsListView)
        displayAchievements(getStaticAchievements(), listView)
    }

    private fun getStaticAchievements(): List<Map<String, String>> {
        return listOf(
            mapOf(
                "title" to "Beginner Speaker",
                "description" to "Completed your first lesson"
            ),
            mapOf(
                "title" to "Practice Makes Perfect",
                "description" to "Practiced 5 different words"
            ),
            mapOf(
                "title" to "Rising Star",
                "description" to "Achieved 80% accuracy in any lesson"
            ),
            mapOf(
                "title" to "Dedicated Learner",
                "description" to "Completed 5 lessons"
            ),
            mapOf(
                "title" to "Pronunciation Master",
                "description" to "Got 100% in any lesson"
            )
        )
    }

    private fun displayAchievements(achievements: List<Map<String, String>>, listView: ListView) {
        val adapter = SimpleAdapter(
            this,
            achievements,
            android.R.layout.simple_list_item_2,
            arrayOf("title", "description"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        listView.adapter = adapter
    }
}