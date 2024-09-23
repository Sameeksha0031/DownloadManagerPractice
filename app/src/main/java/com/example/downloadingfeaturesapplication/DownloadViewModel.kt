package com.example.downloadingfeaturesapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadViewModel : ViewModel() {
    val downloadRepository = DownloadRepository()

    private val _downloadViewModel = MutableSharedFlow<Pair<Boolean,String>>()
    val downloadViewModel : SharedFlow<Pair<Boolean, String>> = _downloadViewModel.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("#PreviewFile", "ViewModel is Called")
            downloadRepository.dataFlow.collectLatest {
                Log.d("#PreviewFile", "ViewModel is Called response = $it")
                _downloadViewModel.emit(it)
            }
        }
    }

}