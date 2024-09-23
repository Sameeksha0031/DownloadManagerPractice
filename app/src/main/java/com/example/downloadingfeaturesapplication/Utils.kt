package com.example.downloadingfeaturesapplication

import android.app.Application.MODE_PRIVATE
import android.content.SharedPreferences
import android.preference.PreferenceManager

object Utils {
   val preferences: SharedPreferences = DownloadApplication().getSharedPreferences( "_preference", MODE_PRIVATE)
}