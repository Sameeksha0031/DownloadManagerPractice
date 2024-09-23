package com.example.downloadingfeaturesapplication

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class DownloadRepository {
    private var _dataFlow = MutableSharedFlow<Pair<Boolean,String>>()
    val dataFlow: SharedFlow<Pair<Boolean, String>> = _dataFlow.asSharedFlow()

    suspend fun updateData(isDownloaded: Boolean, newData: String) {
        Log.d("#PreviewFile", "Repository is called")
        _dataFlow.emit(Pair(isDownloaded,newData))
    }
}