package com.example.pronounceit

import android.app.Activity
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.example.pronounceit.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.graphics.Color

object InAppPopupPoster {
    fun postPopupForAchievement(achTitle: String, achId: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val activity: Activity? = ActivityTracker.getCurrentActivity()
                if (activity == null) {
                    Log.d("InAppPopupPoster", "No current activity to attach popup")
                    return@launch
                }

                val root = activity.findViewById<ViewGroup>(android.R.id.content) ?: run {
                    Log.w("InAppPopupPoster", "Activity root is null for popup")
                    return@launch
                }

                // Get current user ID to make tag user-specific
                val prefs = activity.getSharedPreferences("PronounceItPrefs", Activity.MODE_PRIVATE)
                val userId = prefs.getLong("userId", -1L)
                val tag = "user_${userId}_ach_popup_$achId"

                // Prevent duplicates for this specific user
                if (root.findViewWithTag<ViewGroup>(tag) != null) return@launch

                // Create a custom modal popup with rainbow gradient background
                val container = LinearLayout(activity)
                container.orientation = LinearLayout.VERTICAL
                container.gravity = Gravity.CENTER
                container.tag = tag

                // Make the container bigger - use more screen real estate
                val screenWidth = activity.resources.displayMetrics.widthPixels
                val screenHeight = activity.resources.displayMetrics.heightPixels

                container.layoutParams = ViewGroup.LayoutParams(
                    (screenWidth * 0.85).toInt(),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                container.setPadding(56, 64, 56, 64)
                container.setBackgroundResource(R.drawable.rainbow_gradient_background)

                // Make background semi-transparent for better readability
                container.background.alpha = 230 // 0-255, where 255 is fully opaque

                // Add a title text "Achievement Unlocked!" - bigger and bolder
                val titleText = TextView(activity)
                titleText.text = "Achievement Unlocked!"
                titleText.textSize = 32f
                titleText.setTextColor(Color.WHITE)
                titleText.typeface = android.graphics.Typeface.DEFAULT_BOLD
                titleText.gravity = Gravity.CENTER
                titleText.setPadding(16, 16, 16, 32)
                titleText.setShadowLayer(5f, 2f, 2f, Color.BLACK)
                container.addView(titleText)

                // Achievement name text - bigger and with shadow
                val nameText = TextView(activity)
                nameText.text = achTitle
                nameText.textSize = 26f
                nameText.setTextColor(Color.WHITE)
                nameText.gravity = Gravity.CENTER
                nameText.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                nameText.setPadding(16, 0, 16, 48)
                nameText.setShadowLayer(4f, 2f, 2f, Color.BLACK)
                container.addView(nameText)

                // Create a much bigger image view - 60% of screen width
                val popupImage = ImageView(activity)
                val imageSize = (screenWidth * 0.6).toInt()
                popupImage.layoutParams = LinearLayout.LayoutParams(
                    imageSize,
                    imageSize
                )
                popupImage.scaleType = ImageView.ScaleType.FIT_CENTER
                popupImage.adjustViewBounds = true

                // Load the achievement image
                try {
                    // Fetch the achievement data first to get the badge image path
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val achievementsResponse = RetrofitInstance.getApi(activity)
                                .getAllAchievements()

                            if (achievementsResponse.isSuccessful) {
                                val achievements = achievementsResponse.body() ?: emptyList()
                                val achievement = achievements.find { it.id == achId }

                                if (achievement != null && !achievement.badgeImagePath.isNullOrEmpty()) {
                                    val baseUrl = RetrofitInstance.getBaseUrl()
                                    val imageUrl = if (achievement.badgeImagePath.startsWith("/")) {
                                        baseUrl + achievement.badgeImagePath
                                    } else {
                                        baseUrl + "/" + achievement.badgeImagePath
                                    }

                                    // Load image on the main thread
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Glide.with(activity)
                                            .load(imageUrl)
                                            .placeholder(R.drawable.ic_achievement_default)
                                            .error(R.drawable.ic_achievement_default)
                                            .into(popupImage)

                                        // Add bounce animation after image is loaded
                                        val bounceAnimation = AnimationUtils.loadAnimation(activity, R.anim.logo_bounce)
                                        popupImage.startAnimation(bounceAnimation)
                                    }
                                } else {
                                    // Use default image if achievement not found or no badge path
                                    CoroutineScope(Dispatchers.Main).launch {
                                        popupImage.setImageResource(R.drawable.ic_achievement_default)
                                        val bounceAnimation = AnimationUtils.loadAnimation(activity, R.anim.logo_bounce)
                                        popupImage.startAnimation(bounceAnimation)
                                    }
                                }
                            } else {
                                // Error loading achievements - use default
                                CoroutineScope(Dispatchers.Main).launch {
                                    popupImage.setImageResource(R.drawable.ic_achievement_default)
                                    val bounceAnimation = AnimationUtils.loadAnimation(activity, R.anim.logo_bounce)
                                    popupImage.startAnimation(bounceAnimation)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("InAppPopupPoster", "Error loading achievement image", e)
                            // Set default image on error
                            CoroutineScope(Dispatchers.Main).launch {
                                popupImage.setImageResource(R.drawable.ic_achievement_default)
                                val bounceAnimation = AnimationUtils.loadAnimation(activity, R.anim.logo_bounce)
                                popupImage.startAnimation(bounceAnimation)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("InAppPopupPoster", "Error setting up achievement image", e)
                    popupImage.setImageResource(R.drawable.ic_achievement_default)
                }

                container.addView(popupImage)

                // "View Badge" button - bigger and more prominent
                val viewBadgeButton = Button(activity)
                viewBadgeButton.text = "VIEW BADGE"
                val buttonParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                buttonParams.setMargins(0, 48, 0, 0)
                buttonParams.gravity = Gravity.CENTER
                viewBadgeButton.layoutParams = buttonParams
                viewBadgeButton.setPadding(64, 24, 64, 24)
                viewBadgeButton.setBackgroundResource(R.drawable.dialog_button_background)
                viewBadgeButton.setTextColor(Color.WHITE)
                viewBadgeButton.textSize = 18f

                viewBadgeButton.setOnClickListener {
                    // Navigate directly to Achievements screen
                    val intent = Intent(activity, AchievementsActivity::class.java).apply {
                        putExtra("achievementToScrollId", achId)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    activity.startActivity(intent)

                    // Animate and remove popup
                    container.animate()
                        .alpha(0f)
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(300)
                        .withEndAction {
                            try { root.removeView(container) }
                            catch (e: Exception) {
                                Log.e("InAppPopupPoster", "Error removing popup", e)
                            }
                        }
                        .start()
                }
                container.addView(viewBadgeButton)

                // Add a semi-transparent overlay behind the popup for focus
                val overlay = View(activity)
                overlay.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                overlay.setBackgroundColor(Color.BLACK)
                overlay.alpha = 0.7f

                // Add both overlay and container to the root view
                root.addView(overlay)

                // Add popup to center of screen
                val params = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.gravity = Gravity.CENTER
                root.addView(container, params)

                // Entrance animation - make it more dramatic
                container.alpha = 0f
                container.scaleX = 0.2f
                container.scaleY = 0.2f
                container.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(700)
                    .start()

                // Make sure it's on top
                container.bringToFront()
                container.elevation = 20f

                // Click on overlay to dismiss
                overlay.setOnClickListener {
                    container.animate()
                        .alpha(0f)
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(300)
                        .withEndAction {
                            try {
                                root.removeView(container)
                                root.removeView(overlay)
                            }
                            catch (e: Exception) {
                                Log.e("InAppPopupPoster", "Error removing popup", e)
                            }
                        }
                        .start()
                }

                // Auto-dismiss after a few seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        if (container.parent != null) {
                            container.animate()
                                .alpha(0f)
                                .scaleX(1.2f)
                                .scaleY(1.2f)
                                .setDuration(300)
                                .withEndAction {
                                    try {
                                        root.removeView(container)
                                        root.removeView(overlay)
                                    }
                                    catch (e: Exception) {
                                        Log.e("InAppPopupPoster", "Error removing popup", e)
                                    }
                                }
                                .start()
                        }
                    } catch (e: Exception) {
                        Log.e("InAppPopupPoster", "Error dismissing popup", e)
                    }
                }, 8000)  // Show for 8 seconds to give more time to read
            } catch (e: Exception) {
                Log.e("InAppPopupPoster", "Failed posting in-app popup", e)
            }
        }
    }
}