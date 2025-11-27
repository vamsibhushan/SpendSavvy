package com.example.spendsavvy.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.spendsavvy.ui.screens.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _unreadNotifications = MutableStateFlow<List<Notification>>(emptyList())
    val unreadNotifications: StateFlow<List<Notification>> = _unreadNotifications

    init {
        fetchUnreadNotifications()
    }

    private fun fetchUnreadNotifications() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("notifications")
            .whereEqualTo("isRead", false) // Only fetch unread notifications
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("NotificationViewModel", "Error fetching notifications: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Debugging to see what is coming from Firestore
                    Log.d("NotificationViewModel", "Fetched notifications: ${snapshot.documents.size}")

                    // Update unread notifications
                    _unreadNotifications.value = snapshot.documents.mapNotNull { document ->
                        document.toObject(Notification::class.java)?.copy(id = document.id)
                    }
                }
            }
    }

    fun markAsRead(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("notifications")
            .document(notificationId)
            .update("isRead", true)
            .addOnSuccessListener {
                // Remove the marked notification from the UI
                _unreadNotifications.value = _unreadNotifications.value.filterNot { it.id == notificationId }
                Log.d("NotificationViewModel", "Notification $notificationId marked as read")
            }
            .addOnFailureListener { e ->
                Log.e("NotificationViewModel", "Error marking notification as read: ${e.message}")
            }
    }
}