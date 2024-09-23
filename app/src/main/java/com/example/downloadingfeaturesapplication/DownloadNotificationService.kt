package com.example.downloadingfeaturesapplication

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri

class DownloadNotificationService(
    private val context: Context,
    stringUrl: String,
    service: ServiceConnection
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun showBasicNotification(pdfUri: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri.toUri(), "application/pdf") // Set URI and MIME type for PDF
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context,"downloadNotification")
            .setContentTitle("Download Complete")
            .setContentText("File is downloaded")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(2,notification)
        //notificationManager.notify(2,notification)
    }

    val startIntent = Intent(context,DownloadService::class.java).apply {
        action = "START_DOWNLOAD"
        putExtra("url",stringUrl)
    }

    val i = Intent(context, DownloadService::class.java).also { intent ->
        context.bindService(intent, service, Context.BIND_AUTO_CREATE)
    }.apply {
        action = "START_DOWNLOAD"
        putExtra("url",stringUrl)
    }

    val stopIntent = Intent(context,DownloadService::class.java).apply {
        action = "STOP_DOWNLOAD"
    }

    val startPendingIntent : PendingIntent = PendingIntent.getService(context,0,startIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val stopPendingIntent : PendingIntent = PendingIntent.getService(context,1,stopIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


    fun showExpandableNotification() {
        val notification = NotificationCompat.Builder(context,"downloadNotification")
            .setContentTitle("Download Reminder")
            .setContentText("time to download a file")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setProgress(0,0,true)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground,"Cancel",stopPendingIntent)
            .setStyle(
                NotificationCompat
                    .BigPictureStyle()
                    .bigPicture(
                        context.bitmapFromResource(
                            R.drawable.ic_launcher_background
                        )
                    )
            )
            .build()

        context.startService(i)

        notificationManager.notify(1,notification)
    }

    private fun Context.bitmapFromResource(
        @DrawableRes resId:Int
    )= BitmapFactory.decodeResource(
        resources,
        resId
    )
}