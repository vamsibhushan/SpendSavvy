package com.example.spendsavvy.models

data class Notification(
    val id: String = "",
    val type: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)