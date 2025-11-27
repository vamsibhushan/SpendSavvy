package com.example.spendsavvy

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.spendsavvy.models.Limit

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.time.DayOfWeek

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SpendingLimitChecker(private val application: Application) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Function to check spending against limits
    @RequiresApi(Build.VERSION_CODES.O)
    fun checkSpendingAgainstLimits(category: String? = null) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("limits").get()
            .addOnSuccessListener { limitsSnapshot ->
                val limits = limitsSnapshot.documents.mapNotNull { it.toObject(Limit::class.java) }
                val filteredLimits = if (category != null) {
                    limits.filter { it.category == category }
                } else {
                    limits
                }

                for (limit in filteredLimits) {
                    val startDate = getStartDateForTimePeriod(limit.timePeriod)

                    Log.d(
                        "SpendingCheck",
                        "Checking limit for category: ${limit.category}, Start Date: $startDate"
                    )

                    db.collection("users").document(userId).collection("transactions")
                        .whereEqualTo("title", limit.category) // Filter by category
                        .whereGreaterThanOrEqualTo("date", startDate) // Filter by start date
                        .get().addOnSuccessListener { transactionsSnapshot ->
                            val totalSpending = transactionsSnapshot.documents.sumOf { document ->
                                val amount = document.getDouble("amount") ?: 0.0
                                Log.d(
                                    "SpendingCheck",
                                    "Transaction ID: ${document.id}, Amount: $amount"
                                )
                                amount
                            }

                            Log.d(
                                "SpendingCheck",
                                "Total spending for category ${limit.category}: $totalSpending"
                            )

                            // Check if the total spending exceeds the limit
                            if (totalSpending > limit.limit) {
                                // Send a local notification
                                sendNotification(
                                    "Limit Exceeded: ${limit.category}",
                                    "You have exceeded your ${limit.timePeriod} limit of ${limit.limit} for ${limit.category}."
                                )

                                // Save the notification to Firestore
                                saveNotificationToFirestore(
                                    userId,
                                    "Limit Exceeded: ${limit.category}",
                                    "You have exceeded your ${limit.timePeriod} limit of ${limit.limit} for ${limit.category}."
                                )
                            }
                        }.addOnFailureListener { e ->
                            Log.e(
                                "SpendingCheck",
                                "Error fetching transactions for category ${limit.category}: ${e.message}"
                            )
                        }
                }
            }.addOnFailureListener { e ->
                Log.e("SpendingCheck", "Error fetching limits: ${e.message}")
            }
    }

    // Helper function to determine the start date based on the time period
    @RequiresApi(Build.VERSION_CODES.O)
    fun getStartDateForTimePeriod(timePeriod: String): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val currentDate = LocalDate.now()

        return when (timePeriod) {
            "Monthly" -> currentDate.withDayOfMonth(1).format(formatter) // Start of current month
            "Weekly" -> currentDate.with(DayOfWeek.MONDAY)
                .format(formatter) // Start of current week (Monday)
            "Daily" -> currentDate.format(formatter) // Current day
            "Yearly" -> currentDate.withDayOfYear(1).format(formatter) // Start of the year
            else -> currentDate.format(formatter) // Default to today's date
        }
    }

    // Function to send a notification locally
    private fun sendNotification(title: String, message: String) {
        val notificationHelper = NotificationHelper(application)
        notificationHelper.sendNotification(title, message)
    }

    // Function to save the notification to Firestore for future viewing
    private fun saveNotificationToFirestore(userId: String, title: String, message: String) {
        val notificationData = mapOf(
            "type" to "Limit Exceeded",
            "message" to message,
            "isRead" to false,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(userId).collection("notifications").add(notificationData)
            .addOnSuccessListener {
                Log.d("SpendingCheck", "Notification added to Firestore successfully")
            }.addOnFailureListener { e ->
                Log.e("SpendingCheck", "Error adding notification to Firestore: ${e.message}")
            }
    }
}





