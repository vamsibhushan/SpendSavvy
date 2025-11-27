package com.example.spendsavvy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spendsavvy.viewmodels.NotificationViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController, viewModel: NotificationViewModel = viewModel()
) {
    val notifications by viewModel.unreadNotifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(colors = TopAppBarDefaults.topAppBarColors(Color.White),
                title = { Text("Notifications", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                })
        }, containerColor = Color.White
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(notifications) { notification ->
                NotificationCard(notification = notification,
                    onMarkAsRead = { viewModel.markAsRead(notification.id) })
            }
        }
    }
}


@Composable
fun NotificationCard(notification: Notification, onMarkAsRead: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = notification.type, style = MaterialTheme.typography.bodyLarge)
                Text(text = notification.message, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onMarkAsRead) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Mark as Read")
            }
        }
    }
}


data class Notification(
    val id: String = "",
    val type: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)


