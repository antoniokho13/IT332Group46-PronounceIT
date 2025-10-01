package com.example.pronounceit

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.pronounceit.network.RetrofitInstance
import com.example.pronounceit.network.models.AchievementEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AchievementsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private var achievements: List<AchievementEntity> = emptyList()
    private var userAccumulatedPoints: Int = 0

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
        // Also fetch current user accumulated points if logged in
        fetchCurrentUserPoints()
    }

    private fun fetchCurrentUserPoints() {
        val prefs = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)
        val userId = prefs.getLong("userId", -1L)
        val token = prefs.getString("token", "") ?: ""
        if (userId == -1L) return
        val lastKnown = getLastKnownPoints()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.getApi(this@AchievementsActivity)
                    .getUserById(userId, "Bearer $token")
                if (response.isSuccessful) {
                    val body = response.body()
                    val acc = body?.accumulatedPoints ?: 0
                    withContext(Dispatchers.Main) {
                        userAccumulatedPoints = acc

                        // Hide the total points text view completely
                        val totalPointsLabel = findViewById<TextView>(R.id.totalPointsTextView)
                        totalPointsLabel.visibility = View.GONE

                        // refresh UI if achievements already loaded
                        (recyclerView.adapter as? AchievementAdapter)?.let { adapter ->
                            adapter.notifyDataSetChanged()
                        }

                        // If we already have achievements loaded, check for new unlocks
                        if (achievements.isNotEmpty()) {
                            checkForNewUnlocks(lastKnown, userAccumulatedPoints)
                            setLastKnownPoints(userAccumulatedPoints)
                            // Also process any unlocked achievements that haven't been seen yet
                            try {
                                processUnseenUnlockedAchievements()
                            } catch (e: Exception) {
                                Log.w("AchievementsActivity", "Error processing unseen unlocked achievements", e)
                            }
                        }
                    }
                } else {
                    Log.e("AchievementsActivity", "Failed to load user: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("AchievementsActivity", "Error fetching user points", e)
            }
        }
    }

    // Show popups/notifications for unlocked achievements that haven't been marked as seen yet
    private fun processUnseenUnlockedAchievements() {
        val seenSet = getSeenAchievementIds().toMutableSet()
        val toShow = achievements.filter { ach ->
            val req = ach.pointsRequired ?: 0
            userAccumulatedPoints >= req && !seenSet.contains(ach.id.toString())
        }
        if (toShow.isEmpty()) {
            Log.d("AchievementsActivity", "No unseen unlocked achievements to process (userPoints=$userAccumulatedPoints)")
            return
        }
        Log.d("AchievementsActivity", "processUnseenUnlockedAchievements will show ${toShow.size} popups: ${toShow.map { it.id }}")
        val newSeen = seenSet
        toShow.forEach { ach ->
            try {
                showUnlockPopup(ach)
            } catch (e: Exception) {
                Log.w("AchievementsActivity", "Failed to show popup for ach=${ach.id}", e)
            }
            try {
                showUnlockNotification(ach)
            } catch (e: Exception) {
                Log.w("AchievementsActivity", "Failed to post notification for ach=${ach.id}", e)
            }
            newSeen.add(ach.id.toString())
        }
        setSeenAchievementIds(newSeen)
    }

    private fun setupGradientBackground() {
        // Use the rainbow gradient background drawable instead of creating a new one
        findViewById<View>(android.R.id.content).rootView.setBackgroundResource(R.drawable.rainbow_gradient_background)
    }

    override fun onDestroy() {
        super.onDestroy()
        // No need to cancel colorAnimator since we're not using it anymore
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

            // Set points required information
            pointsTextView?.visibility = View.VISIBLE
            pointsTextView?.text = "Required: ${achievement.pointsRequired} points"

            // Add user's current points below the required points
            pointsRequiredTextView?.visibility = View.VISIBLE
            pointsRequiredTextView?.text = "My Points: $userAccumulatedPoints"
            pointsRequiredTextView?.setTextColor(Color.WHITE)
            pointsRequiredTextView?.textSize = 20f
            pointsRequiredTextView?.gravity = android.view.Gravity.CENTER
            pointsRequiredTextView?.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
            pointsRequiredTextView?.setShadowLayer(2f, 1f, 1f, Color.BLACK)

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

            // Status label inside dialog
            val statusDialogLabel = dialogView.findViewById<TextView?>(R.id.achievementStatusDialogLabel)

            // Gray-out dialog content if locked
            val prefs = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)
            val userId = prefs.getLong("userId", -1L)
            val isLocked = if (userId != -1L) {
                userAccumulatedPoints < (achievement.pointsRequired ?: 0)
            } else {
                true
            }

            if (isLocked) {
                // Show locked status inside the dialog but use the same background as unlocked
                statusDialogLabel?.visibility = View.VISIBLE
                statusDialogLabel?.text = "Locked"
                statusDialogLabel?.textSize = 28f // Much larger text size
                statusDialogLabel?.setTextColor(Color.WHITE) // Brighter color for visibility
                statusDialogLabel?.typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
                statusDialogLabel?.setShadowLayer(4f, 2f, 2f, Color.BLACK) // Add shadow for better contrast
                statusDialogLabel?.gravity = android.view.Gravity.CENTER

                // Use the rainbow gradient background even when locked
                container?.setBackgroundResource(R.drawable.rainbow_gradient_background)

                // Show the image even when locked, but apply a dark filter
                imageView?.visibility = View.VISIBLE
                imageView?.setColorFilter(Color.argb(170, 0, 0, 0))
            } else {
                statusDialogLabel?.visibility = View.GONE
                container?.setBackgroundResource(R.drawable.rainbow_gradient_background)
                imageView?.clearColorFilter()
            }

            // For both states add a semi-transparent overlay for better text readability
            container?.background?.alpha = 180 // Make background a bit more transparent (0-255)

            // Rest of the method continues as before...

            // Set dialog size - 95% of screen width and wrap content for height
            val displayMetrics = resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.95).toInt()
            val height = ViewGroup.LayoutParams.WRAP_CONTENT

            // Apply the size
            dialog.window?.setLayout(width, height)

            // Add animation for appearance
            dialog.window?.attributes?.windowAnimations = android.R.style.Animation_Dialog

            // Apply the bounce animation to the image instead of shine effect
            val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_bounce)
            imageView?.startAnimation(bounceAnimation)

            // Make title MUCH BIGGER and more aesthetic
            titleTextView?.apply {
                textSize = 32f
                setTextColor(Color.WHITE)
                typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
                setShadowLayer(8f, 3f, 3f, Color.BLACK)
                letterSpacing = 0.05f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 24, 0, 24)
                setLineSpacing(0f, 1.2f)
            }

            // Make description CENTERED and BIGGER
            descriptionTextView?.apply {
                textSize = 22f
                setTextColor(Color.WHITE)
                gravity = android.view.Gravity.CENTER
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                setPadding(12, 20, 12, 20)
                setShadowLayer(3f, 1f, 1f, Color.BLACK)
            }

            pointsTextView?.apply {
                textSize = 20f
                setTextColor(Color.WHITE)
                gravity = android.view.Gravity.CENTER
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
                textSize = 20f
                setPadding(48, 24, 48, 24)
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

            // After setting achievements, check for any unlocks since last known points
            val lastKnown = getLastKnownPoints()
            checkForNewUnlocks(lastKnown, userAccumulatedPoints)
            // persist the latest seen points
            setLastKnownPoints(userAccumulatedPoints)

            // Also, handle any achievements that are unlocked by points but haven't been "seen" yet.
            // Relying solely on lastKnownPoints can miss cases (for example if lastKnown was higher due to
            // earlier runs or data reset). Track seen achievement IDs in prefs and show popups for any
            // unlocked achievements that are not yet in the seen set.
            try {
                val prefs = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)
                val seenSet = getSeenAchievementIds().toMutableSet()
                val newlyUnlockedBySeen = achievements.filter { ach ->
                    val req = ach.pointsRequired ?: 0
                    userAccumulatedPoints >= req && !seenSet.contains(ach.id.toString())
                }
                if (newlyUnlockedBySeen.isNotEmpty()) {
                    Log.d("AchievementsActivity", "Found ${newlyUnlockedBySeen.size} unlocked achievements not yet seen: ${newlyUnlockedBySeen.map { it.id }}")
                    newlyUnlockedBySeen.forEach { ach ->
                        try {
                            showUnlockPopup(ach)
                        } catch (e: Exception) {
                            Log.w("AchievementsActivity", "Failed to show in-app popup for seen-detection", e)
                        }
                        try {
                            showUnlockNotification(ach)
                        } catch (e: Exception) {
                            Log.w("AchievementsActivity", "Failed to post system notification for seen-detection", e)
                        }
                        seenSet.add(ach.id.toString())
                    }
                    setSeenAchievementIds(seenSet)
                }
            } catch (e: Exception) {
                Log.w("AchievementsActivity", "Error while checking unseen unlocked achievements", e)
            }

            // If this activity was opened via notification with a target achievement id,
            // scroll to it now that the adapter is ready.
            val targetId = intent?.getLongExtra("achievementToScrollId", -1L) ?: -1L
            if (targetId != -1L) {
                val idx = achievements.indexOfFirst { it.id == targetId }
                if (idx >= 0) {
                    recyclerView.post { recyclerView.smoothScrollToPosition(idx) }
                }
            }

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

            // Animation function
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

            // Title is hidden in the grid; keep it set for accessibility but not visible
            holder.itemView.findViewById<TextView?>(R.id.achievementTitle)?.text = achievement.title

            // Make badge wider horizontally by setting its layout params
            val layoutParams = holder.iconView.layoutParams
            // Calculate width for 2 items per row with padding
            layoutParams.width = resources.displayMetrics.widthPixels / 2 - resources.displayMetrics.density.toInt() * 24
            // Also increase height for better proportions
            layoutParams.height = (layoutParams.width * 1.2).toInt()
            holder.iconView.layoutParams = layoutParams

            // Completely remove any styling that might create borders
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            holder.iconView.setBackgroundResource(0)  // Remove any background resource
            holder.iconView.setPadding(0, 0, 0, 0)    // Remove all padding
            holder.iconView.elevation = 0f            // Remove any elevation
            holder.iconView.clipToOutline = false     // Disable outline clipping

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
                    .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cache images
                    .into(holder.iconView)
            } else {
                holder.iconView.setImageResource(R.drawable.ic_achievement_default)
            }

            // Start animation when view becomes visible
            holder.startAnimation()

            // Determine locked state based on user's accumulated points and achievement requirement
            val pointsRequired = achievement.pointsRequired ?: 0
            val isUnlocked = userAccumulatedPoints >= pointsRequired

            // Instead of graying out completely, use a dark semi-transparent overlay for locked achievements
            if (isUnlocked) {
                holder.itemView.alpha = 1.0f
                // Clear any color filter when unlocked
                holder.iconView.clearColorFilter()
            } else {
                // Still show the image but with darkened overlay - make it 70% opacity
                holder.itemView.alpha = 0.7f

                // Apply a dark semi-transparent color filter instead of solid gray
                // This lets the image still show through but darkened
                holder.iconView.setColorFilter(Color.argb(170, 0, 0, 0))

                // Remove lock icon visibility - hide it completely
                val lockIcon = holder.itemView.findViewById<ImageView?>(R.id.lockIcon)
                if (lockIcon != null) {
                    lockIcon.visibility = View.GONE
                }
            }

            // Click behavior: simply open the details dialog (dialog shows Locked/Unlocked internally)
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

    // Persist the last known accumulated points to prefs so we can detect new unlocks
    private fun getLastKnownPoints(): Int {
        val prefs = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)
        return prefs.getInt("lastKnownPoints", 0)
    }

    private fun setLastKnownPoints(points: Int) {
        val prefs = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)
        prefs.edit().putInt("lastKnownPoints", points).apply()
    }

    // Persist a set of seen achievement IDs so we don't repeatedly show the same popup
    private fun getSeenAchievementIds(): Set<String> {
        val prefs = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)
        return prefs.getStringSet("seenAchievementIds", emptySet()) ?: emptySet()
    }

    private fun setSeenAchievementIds(ids: Set<String>) {
        val prefs = getSharedPreferences("PronounceItPrefs", MODE_PRIVATE)
        prefs.edit().putStringSet("seenAchievementIds", ids).apply()
    }

    // Check if any achievement thresholds were crossed between lastKnown and currentPoints
    private fun checkForNewUnlocks(lastKnown: Int, currentPoints: Int) {
        Log.d("AchievementsActivity", "checkForNewUnlocks lastKnown=$lastKnown currentPoints=$currentPoints achievements=${achievements.size}")
        if (currentPoints <= lastKnown) {
            Log.d("AchievementsActivity", "No new points gained (or already handled)")
            return
        }

        // find first achievement that was locked before and is now unlocked
        val newlyUnlocked = achievements.firstOrNull { ach ->
            val req = ach.pointsRequired ?: 0
            req in (lastKnown + 1)..currentPoints
        }

        if (newlyUnlocked != null) {
            Log.d("AchievementsActivity", "Newly unlocked achievement found: id=${newlyUnlocked.id} title=${newlyUnlocked.title}")
            // show in-app popup if user is on this screen
            try {
                showUnlockPopup(newlyUnlocked)
            } catch (e: Exception) {
                Log.e("AchievementsActivity", "Error showing in-app popup", e)
            }

            // also post a system notification so it appears regardless of the current screen
            try {
                showUnlockNotification(newlyUnlocked)
            } catch (e: Exception) {
                Log.e("AchievementsActivity", "Error posting system notification", e)
            }
        } else {
            Log.d("AchievementsActivity", "No newly unlocked achievement found in range")
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Achievement Unlocks"
            val descriptionText = "Notifications when achievements unlock"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("ach_unlocks", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showUnlockNotification(achievement: AchievementEntity) {
        ensureNotificationChannel()
        val intent = Intent(this, AchievementsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("achievementToScrollId", achievement.id)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            achievement.id.hashCode(),
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val builder = NotificationCompat.Builder(this, "ach_unlocks")
            .setSmallIcon(R.drawable.pronounce_logo)
            .setContentTitle("Achievement unlocked")
            .setContentText("You unlocked \"${achievement.title}\"")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(achievement.id.hashCode(), builder.build())
        }
    }

    private fun showUnlockPopup(achievement: AchievementEntity) {
        // Ensure UI work runs on main thread
        runOnUiThread {
            Log.d("AchievementsActivity", "showUnlockPopup for id=${achievement.id} title=${achievement.title}")

            // Inflate popup layout and add it to the activity root
            val root = findViewById<ViewGroup>(android.R.id.content)
            if (root == null) {
                Log.e("AchievementsActivity", "Cannot show popup: root view (android.R.id.content) is null")
                return@runOnUiThread
            }

            val popupView = LayoutInflater.from(this).inflate(R.layout.popup_achievement_unlocked, root, false)

            // Tag the popup so we don't add duplicates
            val popupTag = "achievement_popup_${achievement.id}"
            popupView.tag = popupTag

            // If a popup for this achievement already exists, don't add another
            val existing = root.findViewWithTag<View>(popupTag)
            if (existing != null) {
                Log.d("AchievementsActivity", "Popup for achievement id=${achievement.id} already shown; skipping duplicate")
                return@runOnUiThread
            }

            // Set up the achievement image in the popup
            val popupImage = popupView.findViewById<ImageView>(R.id.popupImage)

            // Load the achievement image
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
                    .into(popupImage)
            } else {
                popupImage.setImageResource(R.drawable.ic_achievement_default)
            }

            // Add a bounce animation to the image
            val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_bounce)
            popupImage.startAnimation(bounceAnimation)

            // Click opens AchievementsActivity (we are already in it), but ensure UI scrolls to the unlocked badge
            popupView.setOnClickListener {
                val index = achievements.indexOfFirst { it.id == achievement.id }
                if (index >= 0) {
                    recyclerView.smoothScrollToPosition(index)
                }
                // Animate fade out and remove
                popupView.animate()
                    .alpha(0f)
                    .scaleX(1.5f)
                    .scaleY(1.5f)
                    .setDuration(300)
                    .withEndAction { try { root.removeView(popupView) } catch (_: Exception){} }
                    .start()
            }

            // Add popup to center of screen with fade-in and scale animation
            try {
                val params = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.gravity = android.view.Gravity.CENTER
                popupView.elevation = 20f
                root.addView(popupView, params)
            } catch (e: Exception) {
                Log.e("AchievementsActivity", "Failed to add popup view to root", e)
                return@runOnUiThread
            }

            // Start entrance animation
            popupView.alpha = 0f
            popupView.scaleX = 0.5f
            popupView.scaleY = 0.5f
            popupView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(500)
                .start()

            // Ensure it's on top
            popupView.bringToFront()

            // Auto-dismiss after a few seconds with fade-out and scale animation
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    // Find the view by tag in case the root was recreated
                    val tag = popupView.tag
                    val toRemove = if (tag is String) root.findViewWithTag<View>(tag) else popupView
                    toRemove?.animate()
                        ?.alpha(0f)
                        ?.scaleX(1.5f)
                        ?.scaleY(1.5f)
                        ?.setDuration(300)
                        ?.withEndAction {
                            try {
                                if (toRemove != null && toRemove.parent != null) {
                                    (toRemove.parent as? ViewGroup)?.removeView(toRemove)
                                    Log.d("AchievementsActivity", "Popup removed for tag=$tag")
                                }
                            } catch (ex: Exception) {
                                Log.w("AchievementsActivity", "Failed to remove popup view cleanly", ex)
                            }
                        }
                        ?.start()
                } catch (_: Exception) {
                }
            }, 5000)  // Show for 5 seconds
        }
    }
}