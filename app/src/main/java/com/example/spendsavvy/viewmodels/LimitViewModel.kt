package com.example.spendsavvy.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.spendsavvy.models.Limit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LimitViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _limits = MutableStateFlow<List<Limit>>(emptyList())
    val limits: StateFlow<List<Limit>> = _limits

    init {
        fetchLimits()
    }

    // Fetch limits from Firestore
    private fun fetchLimits() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("limits")
            .get()
            .addOnSuccessListener { result ->
                val data = result.documents.mapNotNull { document ->
                    document.toObject(Limit::class.java)
                }
                _limits.value = data
            }
            .addOnFailureListener { e ->
                Log.e("LimitViewModel", "Error fetching limits: ${e.message}")
            }
    }

    fun setLimit(category: String, limit: Double, timePeriod: String) {

        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("limits")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Update the existing limit for the category
                    val documentId = querySnapshot.documents.first().id

                    db.collection("users")
                        .document(userId)
                        .collection("limits")
                        .document(documentId)
                        .update(
                            mapOf(
                                "limit" to limit,
                                "timePeriod" to timePeriod
                            )
                        )
                        .addOnSuccessListener {
                            fetchLimits() // Refresh data
                            Log.d("LimitViewModel", "Limit updated successfully for category: $category")
                        }
                        .addOnFailureListener { e ->
                            Log.e("LimitViewModel", "Error updating limit: ${e.message}")
                        }
                } else {
                    // Add a new limit if none exists for the category
                    val documentId = db.collection("users").document(userId)
                        .collection("limits").document().id

                    val limitData = Limit(
                        id = documentId,
                        category = category,
                        limit = limit,
                        timePeriod = timePeriod
                    )

                    db.collection("users")
                        .document(userId)
                        .collection("limits")
                        .document(documentId)
                        .set(limitData)
                        .addOnSuccessListener {
                            fetchLimits() // Refresh data
                            Log.d("LimitViewModel", "New limit added successfully for category: $category")
                        }
                        .addOnFailureListener { e ->
                            Log.e("LimitViewModel", "Error saving new limit: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("LimitViewModel", "Error checking existing limit: ${e.message}")
            }
    }
}