package com.example.spendsavvy.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.spendsavvy.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StatsScreenViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions: StateFlow<List<Transaction>> get() = _allTransactions

    private val _topExpenses = MutableStateFlow<List<Transaction>>(emptyList())
    val topExpenses: StateFlow<List<Transaction>> get() = _topExpenses

    private val _chartData = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val chartData: StateFlow<List<Pair<String, Double>>> get() = _chartData

    init {
        fetchTransactions()
    }

    private fun fetchTransactions() {
        _isLoading.value = true // Ensure loading state is reset
        db.collection("users")
            .document(userId)
            .collection("transactions")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("StatsScreenViewModel", "Error fetching transactions: ${exception.message}", exception)
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val transactions = snapshot.documents.mapNotNull { document ->
                        document.toObject(Transaction::class.java)?.copy(id = document.id)
                    }
                    _allTransactions.value = transactions
                    updateChartData(transactions)
                    updateTopExpenses(transactions)
                }
                _isLoading.value = false
            }
    }

    private fun updateChartData(transactions: List<Transaction>) {
        val groupedByDate = transactions
            .filter { it.type.lowercase() == "expense" }
            .groupBy { it.date }
            .map { (date, expenses) ->
                val total = expenses.sumOf { it.amount }
                date to total
            }
            .sortedBy { it.first }

        _chartData.value = groupedByDate
    }

    private fun updateTopExpenses(transactions: List<Transaction>) {
        val top = transactions
            .filter { it.type.lowercase() == "expense" }
            .sortedByDescending { it.amount }
            .take(5)

        _topExpenses.value = top
    }
}