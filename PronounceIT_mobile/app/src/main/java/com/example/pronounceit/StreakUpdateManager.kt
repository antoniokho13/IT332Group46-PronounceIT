package com.example.pronounceit

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.pronounceit.network.models.StreakDTO

class StreakUpdateManager(private val context: Context) {

    fun showStreakUpdateDialog(streak: StreakDTO) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.streak_details_dialog, null)

        // Get references to views
        val streakIconView = dialogView.findViewById<ImageView>(R.id.streakIconDialog)
                val streakCountView = dialogView.findViewById<TextView>(R.id.streakCountDialog)
                val streakMessageView = dialogView.findViewById<TextView>(R.id.streakMessageDialog)

                // Set initial values
                streakCountView.text = (streak.currentStreak - 1).toString() // Start from previous count
        streakMessageView.text = "Days Streak!"

        // Create dialog
        val dialog = AlertDialog.Builder(context, R.style.TransparentDialog)
                .setView(dialogView)
                .setCancelable(true)
                .create()

        // Make dialog background transparent
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Show dialog
        dialog.show()

        // Start animations
        animateStreakUpdate(streakIconView, streakCountView, streak.currentStreak, dialog)
    }

    private fun animateStreakUpdate(
            iconView: ImageView,
            countView: TextView,
            newStreak: Int,
            dialog: Dialog
    ) {
        // Animate the fire icon with flame pulse animation
        val flameAnimation = AnimationUtils.loadAnimation(context, R.anim.streak_flame_pulse)
        iconView.startAnimation(flameAnimation)

        // After a short delay, animate the count increment
        Handler(Looper.getMainLooper()).postDelayed({
                // Animate count up
                val startValue = newStreak - 1
                val endValue = newStreak

                // Use ValueAnimator to increment number smoothly
                val animator = android.animation.ValueAnimator.ofInt(startValue, endValue)
                animator.duration = 1000 // 1 second duration
                animator.addUpdateListener { animation ->
                countView.text = animation.animatedValue.toString()
        }

                // Apply scale animation to count when it changes
                val incrementAnim = AnimationUtils.loadAnimation(context, R.anim.streak_increment)
                animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                countView.startAnimation(incrementAnim)
            }
        })

        animator.start()

        // Auto-dismiss after 3.5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
        try {
            dialog.dismiss()
        } catch (e: Exception) {
            // Dialog may already be dismissed
        }
            }, 3500)
        }, 500) // Start count animation after 500ms
    }
}