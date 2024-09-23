package com.example.downloadingfeaturesapplication

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationGraph(
    navHostController: NavHostController,
    snackbarHostState: SnackbarHostState,
    buttonClick: (value: Int) -> Unit
) {
    NavHost(navController = navHostController, startDestination = "Greeting") {

        composable("Greeting") {
            Greeting(snackbarHostState = snackbarHostState){ value ->
                Log.d("#DownloadFeaature","value of onClick $value")
                buttonClick(value)
            }
        }

        composable("PreviewDownload") {
            PDFPreview()
        }
    }
}