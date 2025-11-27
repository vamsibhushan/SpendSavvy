package com.example.spendsavvy.viewmodels

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spendsavvy.SpendingLimitChecker
import com.example.spendsavvy.models.Transaction

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


data class AddTransactionState(
    val isSaving: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class AddTransactionViewModel(application: Application) : AndroidViewModel(application) {


    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    private val _addTransactionState = MutableStateFlow(AddTransactionState())
    val addTransactionState: StateFlow<AddTransactionState> = _addTransactionState

    // Callback to notify HomeScreenViewModel to refresh data
    var onTransactionAdded: (() -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun addTransaction(transaction: Transaction) {
        val userId = auth.currentUser?.uid ?: return
        // Use existing transaction ID or generate a new one if null
        val transactionId =
            transaction.id ?: db.collection("users").document(userId).collection("transactions")
                .document().id

        val expenseData = mapOf(
            "id" to transactionId,
            "title" to transaction.title,
            "amount" to transaction.amount,
            "date" to transaction.date,
            "type" to transaction.type
        )

        viewModelScope.launch {
            _addTransactionState.value = AddTransactionState(isSaving = true)
            try {
                val userDoc = db.collection("users").document(userId)

                // Ensure the user document exists
                val userSnapshot = userDoc.get().await()
                if (!userSnapshot.exists()) {
                    userDoc.set(mapOf("isNewUser" to false)).await() // Initialize user document
                } else if (userSnapshot.getBoolean("isNewUser") != false) {
                    userDoc.update("isNewUser", false)
                        .await() // Update if the user is marked as new
                }

                // Add transaction to Firestore
                db.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .document(transactionId)
                    .set(expenseData, SetOptions.merge()) // Merge data to avoid overwriting
                    .await()

                // Log the success
                Log.d(
                    "AddTransactionViewModel",
                    "Transaction added successfully with ID: $transactionId"
                )

                // Update state
                _addTransactionState.value = AddTransactionState(isSaving = false, success = true)

                // Notify that the transaction was added and data should be refreshed
                onTransactionAdded?.invoke()

                // Check spending against limits for the specific category if it's an expense
                if (transaction.type == "Expense") {
                    Log.d(
                        "AddTransactionViewModel",
                        "Checking spending for category: ${transaction.title}"
                    )
                    SpendingLimitChecker(getApplication()).checkSpendingAgainstLimits(transaction.title)
                }
            } catch (e: Exception) {
                Log.e("AddTransactionViewModel", "Error adding transaction: ${e.message}")
                _addTransactionState.value =
                    AddTransactionState(isSaving = false, error = e.message)
            }
        }
    }

}