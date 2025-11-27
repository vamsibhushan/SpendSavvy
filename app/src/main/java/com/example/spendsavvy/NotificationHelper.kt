package com.example.spendsavvy


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.spendsavvy.ui.navigation.Destinations
import com.example.spendsavvy.ui.screens.NotificationScreen


class NotificationHelper(private val context: Context) {

    fun sendNotification(title: String, message: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "spending_limit_notifications"

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Spending Limit Notifications", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for spending limits and reminders"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Unique notification ID for each notification
        val notificationId = (0..1000).random()

        // PendingIntent to open MainActivity which handles navigation to NotificationScreen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", Destinations.NOTIFICATIONS)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification =
            NotificationCompat.Builder(context, channelId).setSmallIcon(R.drawable.logo2)
                .setContentTitle(title).setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)  // Launch MainActivity when tapped
                .setAutoCancel(true)  // Automatically remove the notification when clicked
                .build()

        // Show the notification
        notificationManager.notify(notificationId, notification)
    }
}







