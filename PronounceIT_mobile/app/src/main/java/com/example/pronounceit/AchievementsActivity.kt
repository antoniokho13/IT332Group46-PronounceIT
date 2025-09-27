package com.example.pronounceit

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.AchievementEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AchievementsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private var achievements: List<AchievementEntity> = emptyList()
    private lateinit var backgroundGradient: GradientDrawable
    private lateinit var colorAnimator: ValueAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        // Set up gradient background
        setupGradientBackground()

        recyclerView = findViewById(R.id.achievementsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 items per row for bigger badges

        // Add scroll listener to restart animations when recycling occurs
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // When scrolling stops, restart animations for visible items
                    restartAllVisibleAnimations()
                }
            }
        })

        loadAchievements()
    }

    private fun setupGradientBackground() {
        // Create gradient drawable with dark blue to light sky blue
        backgroundGradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                ContextCompat.getColor(this, android.R.color.holo_blue_dark),
                ContextCompat.getColor(this, android.R.color.holo_blue_light)
            )
        )

        // Set the background of the root layout
        findViewById<View>(android.R.id.content).rootView.background = backgroundGradient

        // Create color animation from dark blue to black and back
        val darkBlue = ContextCompat.getColor(this, android.R.color.holo_blue_dark)
        val skyBlue = ContextCompat.getColor(this, android.R.color.holo_blue_light)
        val black = ContextCompat.getColor(this, android.R.color.black)

        colorAnimator = ValueAnimator.ofObject(
            ArgbEvaluator(),
            darkBlue, skyBlue, black, skyBlue, darkBlue
        )

        colorAnimator.duration = 4000 // 4 seconds for complete cycle (1 second per transition)
        colorAnimator.repeatCount = ValueAnimator.INFINITE
        colorAnimator.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            backgroundGradient.colors = intArrayOf(color, skyBlue)
            findViewById<View>(android.R.id.content).rootView.invalidate()
        }

        colorAnimator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::colorAnimator.isInitialized) {
            colorAnimator.cancel()
        }
    }

    private fun restartAllVisibleAnimations() {
        for (i in 0 until recyclerView.childCount) {
            val viewHolder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i))
            if (viewHolder is AchievementAdapter.ViewHolder) {
                startBounceAnimation(viewHolder.iconView)
            }
        }
    }

    private fun startBounceAnimation(view: View) {
        view.clearAnimation()
        val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_bounce)
        bounceAnimation.fillAfter = true
        bounceAnimation.isFillEnabled = true
        view.startAnimation(bounceAnimation)
    }

    private fun showAchievementDetails(achievement: AchievementEntity) {
        try {
            // Create a custom dialog
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_achievement_details, null)

            // Find views in the dialog layout
            val titleTextView = dialogView.findViewById<TextView>(R.id.achievementTitleTextView)
            val imageView = dialogView.findViewById<ImageView>(R.id.achievementImageView)
            val descriptionTextView = dialogView.findViewById<TextView>(R.id.achievementDescriptionTextView)
            val pointsTextView = dialogView.findViewById<TextView>(R.id.achievementPointsTextView)
            val pointsRequiredTextView = dialogView.findViewById<TextView>(R.id.achievementPointsRequiredTextView)

            // Set achievement data to views with null safety
            titleTextView?.text = achievement.title
            descriptionTextView?.text = achievement.description

            // Set points reward information if available
            if (achievement.pointsReward != null && achievement.pointsReward > 0) {
                pointsTextView?.visibility = View.VISIBLE
                pointsTextView?.text = "Reward: ${achievement.pointsReward} points"
            } else {
                pointsTextView?.visibility = View.GONE
            }

            // Set points required information if available
            if (achievement.triggerValue != null && achievement.triggerValue > 0) {
                pointsRequiredTextView?.visibility = View.VISIBLE
                pointsRequiredTextView?.text = "Required: ${achievement.triggerValue} ${achievement.triggerType?.toLowerCase() ?: "points"}"
            } else {
                pointsRequiredTextView?.visibility = View.GONE
            }

            // Make image view LARGER - now 40% of screen height
            imageView?.layoutParams?.height = (resources.displayMetrics.heightPixels * 0.4).toInt()
            imageView?.layoutParams?.width = (resources.displayMetrics.widthPixels * 0.6).toInt()
            imageView?.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView?.requestLayout()

            // Load the achievement image with error handling
            if (!achievement.badgeImagePath.isNullOrEmpty()) {
                val baseUrl = RetrofitInstance.getBaseUrl()
                val imageUrl = if (achievement.badgeImagePath.startsWith("/")) {
                    baseUrl + achievement.badgeImagePath
                } else {
                    baseUrl + "/" + achievement.badgeImagePath
                }

                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_achievement_default)
                    .error(R.drawable.ic_achievement_default)
                    .into(imageView)
            } else {
                imageView?.setImageResource(R.drawable.ic_achievement_default)
            }

            // Create dialog
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            // Set transparent background for the dialog window
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            // Get the container and apply the rainbow gradient background
            val container = dialogView.findViewById<LinearLayout>(R.id.achievementDetailsContainer)
            container?.setBackgroundResource(R.drawable.rainbow_gradient_background)

            // Add a semi-transparent overlay for better text readability
            container?.background?.alpha = 180 // Make background a bit more transparent (0-255)

            // Set dialog size - 90% of screen width and wrap content for height
            val displayMetrics = resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.95).toInt() // Increased from 0.9 to 0.95
            val height = ViewGroup.LayoutParams.WRAP_CONTENT

            // Apply the size
            dialog.window?.setLayout(width, height)

            // Add animation for appearance
            dialog.window?.attributes?.windowAnimations = android.R.style.Animation_Dialog

            // Make title MUCH BIGGER and more aesthetic
            titleTextView?.apply {
                textSize = 32f  // Increased from 28f to 32f
                setTextColor(Color.WHITE)
                typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
                setShadowLayer(8f, 3f, 3f, Color.BLACK)  // Increased shadow
                letterSpacing = 0.05f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 24, 0, 24)  // More padding
                setLineSpacing(0f, 1.2f) // Add some line spacing if text wraps
            }

            // Make description CENTERED and BIGGER
            descriptionTextView?.apply {
                textSize = 22f  // Increased from 18f to 22f
                setTextColor(Color.WHITE)
                gravity = android.view.Gravity.CENTER  // Center align text
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                setPadding(12, 20, 12, 20)  // Add padding around text
                setShadowLayer(3f, 1f, 1f, Color.BLACK)  // Slight shadow for readability
            }

            pointsTextView?.apply {
                textSize = 20f  // Increased from 18f to 20f
                setTextColor(Color.WHITE)
                gravity = android.view.Gravity.CENTER  // Center these too
                typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                setShadowLayer(2f, 1f, 1f, Color.BLACK)
            }

            pointsRequiredTextView?.apply {
                textSize = 20f  // Increased from 18f to 20f
                setTextColor(Color.WHITE)
                gravity = android.view.Gravity.CENTER  // Center these too
                typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                setShadowLayer(2f, 1f, 1f, Color.BLACK)
            }

            // Add more padding around the container
            container?.setPadding(32, 36, 32, 36)

            // Custom positive button
            val buttonLayout = dialogView.findViewById<LinearLayout>(R.id.buttonLayout)
                ?: LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    gravity = android.view.Gravity.CENTER
                    dialogView.findViewById<LinearLayout>(R.id.achievementDetailsContainer)?.addView(this)
                }

            val okButton = Button(this).apply {
                text = "OK"
                setBackgroundResource(R.drawable.dialog_button_background)
                setTextColor(Color.WHITE)
                textSize = 20f  // Increased button text size
                setPadding(48, 24, 48, 24)  // Larger button
                setOnClickListener { dialog.dismiss() }
            }
            buttonLayout.addView(okButton)

            dialog.show()

        } catch (e: Exception) {
            Log.e("AchievementsActivity", "Error showing achievement details: ${e.message}", e)
            Toast.makeText(this, "Error showing achievement details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAchievements() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("AchievementsActivity", "Starting to load achievements")
                val response = RetrofitInstance.getApi(this@AchievementsActivity)
                    .getAllAchievements()

                if (response.isSuccessful) {
                    val achievementsList = response.body() ?: emptyList()
                    Log.d("AchievementsActivity", "Loaded ${achievementsList.size} achievements")
                    displayAchievements(achievementsList)
                } else {
                    Log.e("AchievementsActivity", "Failed to load achievements: ${response.code()}")
                    showError("Server error: ${response.code()}")
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
            val adapter = AchievementAdapter(achievementsList) { achievement ->
                showAchievementDetails(achievement)
            }
            recyclerView.adapter = adapter

            // Delay to ensure all views are properly laid out before starting animations
            recyclerView.post {
                restartAllVisibleAnimations()
            }
        }
    }

    private suspend fun showError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@AchievementsActivity, message, Toast.LENGTH_LONG).show()
            Log.e("AchievementsActivity", "Error: $message")
        }
    }

    private inner class AchievementAdapter(
        private val achievements: List<AchievementEntity>,
        private val onItemClick: (AchievementEntity) -> Unit
    ) : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val iconView: ImageView = view.findViewById(R.id.achievementIcon)

            // Add a function to this ViewHolder to handle animation
            fun startAnimation() {
                startBounceAnimation(iconView)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_achievement_grid, parent, false)
            // Ensure the view is completely transparent
            view.setBackgroundColor(Color.TRANSPARENT)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val achievement = achievements[position]

            // Make badge wider horizontally by setting its layout params
            val layoutParams = holder.iconView.layoutParams
            // Calculate width for 2 items per row with padding
            layoutParams.width = resources.displayMetrics.widthPixels / 2 - resources.displayMetrics.density.toInt() * 24
            // Also increase height for better proportions
            layoutParams.height = (layoutParams.width * 1.2).toInt()
            holder.iconView.layoutParams = layoutParams

            // Ensure background is transparent
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            holder.iconView.setBackgroundColor(Color.TRANSPARENT)

            // Load badge image from backend
            if (!achievement.badgeImagePath.isNullOrEmpty()) {
                val baseUrl = RetrofitInstance.getBaseUrl()
                val imageUrl = if (achievement.badgeImagePath.startsWith("/")) {
                    baseUrl + achievement.badgeImagePath
                } else {
                    baseUrl + "/" + achievement.badgeImagePath
                }

                Log.d("AchievementsActivity", "Loading image: $imageUrl")

                Glide.with(this@AchievementsActivity)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_achievement_default)
                    .error(R.drawable.ic_achievement_default)
                    .into(holder.iconView)
            } else {
                holder.iconView.setImageResource(R.drawable.ic_achievement_default)
            }

            // Start animation when view becomes visible
            holder.startAnimation()

            // Set click listener for showing achievement details
            holder.itemView.setOnClickListener {
                onItemClick(achievement)
            }
        }

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            super.onViewAttachedToWindow(holder)
            // Restart animation whenever a view is attached to window
            holder.startAnimation()
        }

        override fun getItemCount() = achievements.size
    }
}