package com.example.spendsavvy.models

data class Limit(
    val id: String = "", // Unique ID for each limit
    val category: String = "",
    val limit: Double = 0.0,
    val timePeriod: String = ""
)