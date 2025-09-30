package com.example.pronounceit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.pronounceit.network.models.AchievementEntity

object NotificationPoster {
    private const val CHANNEL_ID = "ach_unlocks"

    private fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Achievement Unlocks"
            val descriptionText = "Notifications when achievements unlock"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply { description = descriptionText }
            val notificationManager: NotificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun post(ctx: Context, ach: AchievementEntity) {
        ensureChannel(ctx)
        val intent = Intent(ctx, AchievementsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("achievementToScrollId", ach.id)
        }
        val pendingIntent = PendingIntent.getActivity(ctx, ach.id.hashCode(), intent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)

        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.pronounce_logo)
            .setContentTitle("Achievement unlocked")
            .setContentText("You unlocked \"${ach.title}\"")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(ctx)) { notify(ach.id.hashCode(), builder.build()) }
    }
}
