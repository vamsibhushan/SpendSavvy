package com.example.spendsavvy.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.spendsavvy.models.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeScreenViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    private val _isNewUser = MutableStateFlow(false)
    val isNewUser: StateFlow<Boolean> get() = _isNewUser

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> get() = _transactions

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> get() = _totalIncome

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> get() = _totalExpense

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> get() = _balance

    init {
        checkUserStatus()

    }

    fun checkUserStatus() {
        _isLoading.value = true // Ensure loading state is reset
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val isNewUser = document.getBoolean("isNewUser") ?: true
                _isNewUser.value = isNewUser
                if (!isNewUser) {
                    fetchTransactions(userId)
                } else {
                    _isLoading.value = false
                }
            }
            .addOnFailureListener {
                _isNewUser.value = true
                _isLoading.value = false
            }
    }


    fun fetchTransactions(userId: String) {
        db.collection("users")
            .document(userId)
            .collection("transactions")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("HomeScreenViewModel", "Error fetching transactions: ${exception.message}", exception)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val expenses = snapshot.documents.mapNotNull { document ->
                        document.toObject(Transaction::class.java)?.copy(id = document.id)
                    }
                    _transactions.value = expenses
                    calculateTotals(expenses)
                }
                _isLoading.value = false
            }
    }

    private fun calculateTotals(transactions: List<Transaction>)  {
        var incomeTotal = 0.0
        var expenseTotal = 0.0

        for (transaction in transactions) {
            when (transaction.type.lowercase()) {
                "income" -> incomeTotal += transaction.amount
                "expense" -> expenseTotal += transaction.amount
            }
        }

        _totalIncome.value = incomeTotal
        _totalExpense.value = expenseTotal
        _balance.value = incomeTotal - expenseTotal
    }
}