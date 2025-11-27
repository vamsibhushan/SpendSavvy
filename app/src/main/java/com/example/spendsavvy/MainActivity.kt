package com.example.spendsavvy


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.spendsavvy.ui.screens.MainScreen

class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()


        // Request notification permission for Android 13+ (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            handleNotificationPermission()
        } else {
            Log.d("MainActivity", "Notification permission not required for this Android version.")
        }

        handleIntent(intent)

        // Set the main UI content
        setContent {
            MainScreen(intent = intent)
        }
    }


    private fun handleIntent(intent: Intent) {
        // Check if the activity was launched from a notification
        val navigateTo = intent.getStringExtra("navigate_to")
        navigateTo?.let {

            Log.d("MainActivity", "Notification clicked: $it")
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun handleNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("MainActivity", "Notification permission already granted.")

            }

            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                Log.d("MainActivity", "Showing rationale for notification permission.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

            else -> {
                Log.d("MainActivity", "Requesting notification permission.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Activity result launcher for requesting permissions.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted.")
            showToast("Notification permission granted.")
        } else {
            Log.e("MainActivity", "Notification permission denied.")
            showToast("Notification permission denied.")
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val orientation = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            "landscape"
        } else {
            "portrait"
        }
        Log.d("MainActivity", "Orientation changed to $orientation")
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "Activity started.")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "Activity stopped.")
    }
}







