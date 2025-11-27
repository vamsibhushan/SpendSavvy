package com.example.spendsavvy.models

data class Transaction(
    val id: String? ="",
    val title: String ="",
    val amount: Double =0.0,
    val date: String ="",
    val type: String =""
)