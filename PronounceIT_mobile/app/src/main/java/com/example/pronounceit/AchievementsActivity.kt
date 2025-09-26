package com.example.pronounceit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.AchievementEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AchievementsActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private var achievements: List<AchievementEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        listView = findViewById(R.id.achievementsListView)
        listView.setOnItemClickListener { _, _, position, _ ->
            showAchievementDetails(achievements[position])
        }
        
        loadAchievements()
    }

    private fun showAchievementDetails(achievement: AchievementEntity) {
        AlertDialog.Builder(this)
            .setTitle(achievement.title)
            .setMessage(buildString {
                append(achievement.description)
                if (achievement.pointsReward != null && achievement.pointsReward > 0) {
                    append("\n\nReward: ${achievement.pointsReward} points")
                }
            })
            .setPositiveButton("OK", null)
            .show()
    }

    private fun loadAchievements() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("AchievementsActivity", "Starting to load achievements")
                val response = RetrofitInstance.getApi(this@AchievementsActivity)
                    .getActiveAchievements()
                
                if (response.isSuccessful) {
                    val achievements = response.body() ?: emptyList()
                    Log.d("AchievementsActivity", "Received ${achievements.size} achievements")
                    displayAchievements(achievements)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AchievementsActivity", "Error response: $errorBody")
                    showError("Failed to load achievements: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("AchievementsActivity", "Error loading achievements", e)
                showError("Network error: ${e.message}")
            }
        }
    }

    private suspend fun displayAchievements(achievementsList: List<AchievementEntity>) {
        withContext(Dispatchers.Main) {
            if (achievementsList.isEmpty()) {
                Toast.makeText(this@AchievementsActivity, 
                    "No achievements available", 
                    Toast.LENGTH_SHORT).show()
                return@withContext
            }

            achievements = achievementsList
            val adapter = AchievementAdapter(achievementsList)
            listView.adapter = adapter
        }
    }

    private suspend fun showError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@AchievementsActivity, message, Toast.LENGTH_LONG).show()
            Log.e("AchievementsActivity", "Error: $message")
        }
    }

    private inner class AchievementAdapter(private val achievements: List<AchievementEntity>) : BaseAdapter() {
        override fun getCount(): Int = achievements.size
        override fun getItem(position: Int): Any = achievements[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@AchievementsActivity)
                .inflate(R.layout.item_achievement, parent, false)
            
            val achievement = achievements[position]
            val iconView = view.findViewById<ImageView>(R.id.achievementIcon)
            val titleView = view.findViewById<TextView>(R.id.achievementTitle)

            titleView.text = achievement.title

            // Load badge image from backend using public method
            if (!achievement.badgeImagePath.isNullOrEmpty()) {
                val imageUrl = "${RetrofitInstance.getBaseUrl()}/api/achievements/${achievement.id}/badge"
                Glide.with(this@AchievementsActivity)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_achievement_default)
                    .error(R.drawable.ic_achievement_default)
                    .into(iconView)
            } else {
                iconView.setImageResource(R.drawable.ic_achievement_default)
            }

            return view
        }
    }
}