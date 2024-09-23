package com.example.downloadingfeaturesapplication

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.downloadingfeaturesapplication.ui.theme.DownloadingFeaturesApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    var recevier : BroadcastReceiver ?= null
    private var downloadService : DownloadService ?= null
    private var isBound = false

    private val service = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            val binder2 = binder as DownloadService.LocalBinder
            downloadService = binder2.getService()
            isBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
        }

    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            var buttonClick by remember { mutableIntStateOf(0) }
            var showDownloadedCompleted by remember { mutableStateOf(false) }
            var pdfUri by remember { mutableStateOf("") }
            var viewModel = viewModel(modelClass = DownloadViewModel::class.java)
            val scope = rememberCoroutineScope()
            val navController = rememberNavController()
            DownloadingFeaturesApplicationTheme {
                val postNotificationPermission =
                    rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
                LaunchedEffect(key1 = true) {
                    if (!postNotificationPermission.status.isGranted) {
                        postNotificationPermission.launchPermissionRequest()
                    }
                }
                val waterNotificationService = DownloadNotificationService(
                    this,
                    "https://www.iitk.ac.in/esc101/share/downloads/javanotes5.pdf"
                )
                NavigationGraph(navHostController = navController, snackbarHostState) { value ->
                    buttonClick = value
                }

                /*recevier = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        if (intent != null) {
                            pdfUri = intent.getStringExtra("pdfFile").toString()
                            showDownloadedCompleted = intent.getBooleanExtra("isDownloaded",false)
                        }
                    }

                }*/

                if (isBound) {
                    val data = downloadService?.getService()
                    if (data != null) {
                        showDownloadedCompleted = data.first
                        pdfUri = data.second
                    }
                }

                if(showDownloadedCompleted){
                    waterNotificationService.showBasicNotification(pdfUri)
                }

                LaunchedEffect(Unit) {
                    scope.launch {
                        Log.d("#PreviewFile", "ViewModel response")
                        viewModel.downloadViewModel.collectLatest {
                            Log.d(
                                "#PreviewFile",
                                "ViewModel response value ${it.first}, ${it.second}"
                            )
                            showDownloadedCompleted = it.first
                            pdfUri = it.second
                            DownloadService().stopSelf()
                        }
                    }
                }

                when (buttonClick) {
                    1 -> {
                        waterNotificationService.showExpandableNotification()
                        buttonClick = 0
                    }

                    2 -> {
                        Log.d("#PreviewFile", "Preview is called")
                        buttonClick = 0
                        navController.navigate("PreviewDownload")
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, DownloadService::class.java).also { intent ->
            bindService(intent, service, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        if (isBound) {
            unbindService(service)
            isBound = false
        }
    }
}


@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    buttonClick: (value: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }) { innerPadding ->
        Text(
            text = "Download button",
            modifier = modifier
                .padding(top = 50.dp, end = 10.dp)
                .clickable {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            "SnackBar", actionLabel = "Action",
                            duration = SnackbarDuration.Indefinite
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {}
                            SnackbarResult.Dismissed -> {}
                        }
                    }
                    buttonClick(1)
                }
        )
        Text(
            text = "Preview",
            modifier = modifier
                .padding(top = 100.dp)
                .clickable {
                    scope.launch {
                        buttonClick(2)
                    }
                }
        )
    }
}




