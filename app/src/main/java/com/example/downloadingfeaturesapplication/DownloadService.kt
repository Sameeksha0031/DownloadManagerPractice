package com.example.downloadingfeaturesapplication

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.EnvironmentCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : Service() {

    private val binder = LocalBinder()
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var downloadManager : DownloadManager ?= null
    private var downloadId:Long = -1
    private val downloadRepository = DownloadRepository()
    var pdfUrlLocal = ""
    var isDownloaded = false

    override fun onBind(p0: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): DownloadService = this@DownloadService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            "START_DOWNLOAD" -> {
                Log.d("#DownloadFeaature","onStartDownload and START_DOWNLOAD")
//                downloadId = startDownload(intent.getStringExtra("url"))
//                getDownloadProgress(this@DownloadService,downloadId)
                downloadFileUsingHttpUrlConnection(this@DownloadService, "https://www.iitk.ac.in/esc101/share/downloads/javanotes5.pdf")
            }
            "STOP_DOWNLOAD" -> {
                Log.d("#DownloadFeaature","onStartDownload and STOP_DOWNLOAD")
                stopDownload()
            }
        }
        return START_NOT_STICKY
    }

    private fun getDownloadProgress(
        downloadService: DownloadService,
        downloadId: Long
    ) : Int{
        var downloadManager = applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor: Cursor? = downloadManager?.query(query)
        var bytesDownloaded = 0
        var progress = 0
        if(cursor?.moveToFirst() == true) {
            bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val totalBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

            if(totalBytes > 0) {
                progress = ((bytesDownloaded * 100L) / totalBytes).toInt()
            }
        }
        Log.d("#DownloadFeaature","progress value = $progress")
        cursor?.close()
        if(progress != 100) {
            getDownloadProgress(
                downloadService, downloadId
            )
        }
        stopDownload()
        return progress
    }

    private fun startDownload(stringExtra: String?): Long {
        Log.d("#DownloadFeaature", "StartDownload is call of Service")
        var request: DownloadManager.Request? = null
        val uri = Uri.parse(stringExtra)
        val packageName = applicationContext.packageName
        val mediaDir = File(Environment.getExternalStorageDirectory(),"Android/media/$packageName")
        if(!mediaDir.exists()){
            mediaDir.mkdirs()
        }
        request = DownloadManager.Request(uri).apply {
            setMimeType("application/pdf")
            setTitle("DumyFile")
            setDescription("Downloading file....")
            setDestinationInExternalFilesDir(applicationContext,"Android/media/$packageName", "DumyFile.pdf")
            Log.d("ExternalFile","getDestinationFilesDir = ${getExternalFilesDir("DumyFile.pdf")}")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        }
        var downloadManager =
            applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(request)
    }


    private fun downloadFileUsingHttpUrlConnection(context: Context, pdfUrl: String) {
        Thread {
            try {
                val url = URL(pdfUrl)
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.connect()

                // Create media directory
                val packageName = context.packageName
                val mediaDir = File(Environment.getExternalStorageDirectory(), "Android/media/$packageName")

                if (!mediaDir.exists()) {
                    mediaDir.mkdirs()
                }

                // Create file
                val pdfFile = File(mediaDir, "downloaded_file.pdf")
                val inputStream = BufferedInputStream(urlConnection.inputStream)
                val fileOutputStream = FileOutputStream(pdfFile)
                VALUE_IS_ADDED = Pair(true,pdfFile.toString())
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } != -1) {
                    fileOutputStream.write(buffer, 0, length)
                }

                pdfUrlLocal = pdfFile.toString()
                isDownloaded = true
                // Close streams
                fileOutputStream.flush()
                fileOutputStream.close()
                inputStream.close()
                urlConnection.disconnect()

//                serviceScope.launch {
//                    Log.d("#DownloadFeaature","Inside Scope function")
//                    downloadRepository.updateData(true,pdfFile.toString())
//                }

                //BroadCAstReceiver
//                val intent = Intent("PENDING_INTENT_SAM")
//                intent.putExtra("pdfFile",pdfFile)
//                intent.putExtra("isDownloaded",true)
//                sendBroadcast(intent)

                NotificationManagerCompat.from(this).cancel(1)
                Log.d("#DownloadFeaature","StopDownload is call of Service")


            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions
                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun stopDownload() {
        Log.d("#DownloadFeaature","StopDownload is call of Service")
        serviceJob.cancel()
        stopSelf()
        NotificationManagerCompat.from(this).cancel(1)
    }

    fun getService() : Pair<Boolean,String> {
        return Pair(isDownloaded,pdfUrlLocal)
    }
}