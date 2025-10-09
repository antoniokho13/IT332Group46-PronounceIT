package com.example.pronounceit

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.pronounceit.network.models.StreakDTO
import java.time.LocalDate
import java.time.ZoneId

class StreakUpdateManager(private val context: Context) {

    private val prefs by lazy { context.getSharedPreferences("PronounceItPrefs", Context.MODE_PRIVATE) }
    private val PREF_LAST_STREAK_DATE = "lastStreakPopupDate"
    private val PREF_LAST_STREAK_VALUE = "lastStreakPopupValue"
    private fun dateKey(userId: Long) = "${PREF_LAST_STREAK_DATE}_u_$userId"
    private fun valueKey(userId: Long) = "${PREF_LAST_STREAK_VALUE}_u_$userId"
    // Use Manila timezone to stay consistent with other time logging in the app
    private val zoneId: ZoneId = ZoneId.of("Asia/Manila")

    /**
     * Shows the streak dialog ONLY if:
     *  - The streak value represents a new increment for the current calendar day (Manila time), AND
     *  - A popup has not already been shown today.
     * This prevents multiple lesson completions on the same day from repeatedly showing + animating.
     * Also handles streak reset (e.g., back to 1) as a valid show event if not already shown today.
     *
     * @param streak The streak data to display
     * @param onDismiss Optional callback that runs when the dialog is dismissed
     */
    fun showStreakUpdateDialog(streak: StreakDTO, onDismiss: (() -> Unit)? = null) {
        if (!shouldShowFor(streak)) {
            // If we're not showing the dialog, still invoke the callback immediately
            onDismiss?.invoke()
            return
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.streak_details_dialog, null)

        val streakIconView = dialogView.findViewById<ImageView>(R.id.streakIconDialog)
        val streakCountView = dialogView.findViewById<TextView>(R.id.streakCountDialog)
        val streakMessageView = dialogView.findViewById<TextView>(R.id.streakMessageDialog)

        // Start from previous count for animation. Guard against going below 0.
        val previous = (streak.currentStreak - 1).coerceAtLeast(0)
        streakCountView.text = previous.toString()
        streakMessageView.text = "Days Streak!"

        val dialog = AlertDialog.Builder(context, R.style.TransparentDialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set up dismiss listener to trigger callback
        dialog.setOnDismissListener {
            onDismiss?.invoke()
        }

        dialog.show()

        // Persist that we showed it AFTER successful show
        persistShown(streak)

        animateStreakUpdate(streakIconView, streakCountView, streak.currentStreak, dialog)
    }

    private fun shouldShowFor(streak: StreakDTO): Boolean {
        val today = LocalDate.now(zoneId).toString()
        val userId = streak.userId ?: return false

        // Migration: if per-user keys missing but global keys exist, migrate once.
        val perUserDateKey = dateKey(userId)
        val perUserValueKey = valueKey(userId)
        if (!prefs.contains(perUserDateKey) && prefs.contains(PREF_LAST_STREAK_DATE)) {
            val legacyDate = prefs.getString(PREF_LAST_STREAK_DATE, null)
            val legacyVal = prefs.getInt(PREF_LAST_STREAK_VALUE, -1)
            prefs.edit()
                .remove(PREF_LAST_STREAK_DATE)
                .remove(PREF_LAST_STREAK_VALUE)
                .putString(perUserDateKey, legacyDate)
                .putInt(perUserValueKey, legacyVal)
                .apply()
        }

        val lastDate = prefs.getString(perUserDateKey, null)
        val lastValue = prefs.getInt(perUserValueKey, -1)

        // If already shown today, skip regardless of value.
        if (lastDate == today) return false

        // Determine if this is a new increment or a reset start.
        val isIncrement = streak.currentStreak > lastValue && streak.currentStreak > 0
        val isResetStart = streak.currentStreak == 1 && lastValue > 1 // treat reset as fresh event (>1 -> lapse -> back to 1)

        // Do not show for initial zero baseline
        if (lastValue == -1 && streak.currentStreak == 0) return false

        return isIncrement || isResetStart || (lastValue == -1 && streak.currentStreak == 1)
    }

    private fun persistShown(streak: StreakDTO) {
        val userId = streak.userId ?: return
        val today = LocalDate.now(zoneId).toString()
        prefs.edit()
            .putString(dateKey(userId), today)
            .putInt(valueKey(userId), streak.currentStreak)
            .apply()
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