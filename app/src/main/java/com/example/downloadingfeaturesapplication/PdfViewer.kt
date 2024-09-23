package com.example.downloadingfeaturesapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

@Composable
fun PDFPreview(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pdfBitmapConverter = remember {
        PdfBitmapConverter(context)
    }
    val openPdfChooser = remember {
        mutableStateOf(false)
    }

    var pdfUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var renderPages by remember {
        mutableStateOf<List<Bitmap>>(emptyList())
    }

    LaunchedEffect(key1 = pdfUri) {
        pdfUri?.let { uri ->
            renderPages = pdfBitmapConverter.pdfToBitmaps(uri)
        }
    }

    val choosePdfLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        pdfUri = it
    }

    if(openPdfChooser.value){
        PdfFileSelector(context = context) { selectUri ->
            Log.d("PdfViewer","PdfFileSelector on Uri selected = $selectUri")
            openPdf(context,selectUri)
            openPdfChooser.value = false
            //pdfUri = selectUri
        }
//        openPdfChooser.value = false
//        val file = File(context.filesDir,"downloaded_file.pdf")
//        Log.d("PdfViewer","File(context.filesDir,downloaded_file.pdf = ${file}")
//        pdfUri = file.toUri()
    }


    if(pdfUri == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                openPdfChooser.value = true
               //choosePdfLauncher.launch("application/pdf")
            }) {
                Text(text = "Choose Pdf")
            }
        }
    }else {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                items(renderPages) { page->
                    PdfPage(page = page)
                }
            }
        }
    }
}

@Composable
fun PdfPage(
    page : Bitmap,
    modifier : Modifier = Modifier
) {
    AsyncImage(model = page, contentDescription = null, modifier = modifier
        .fillMaxWidth()
        .aspectRatio(page.width.toFloat() / page.height.toFloat()))
}

@Composable
fun PdfFileSelector(context: Context, onPdfSelected: (Uri) -> Unit) {
    val file = File("/storage/emulated/0/Android/media/com.example.downloadingfeaturesapplication/", "downloaded_file.pdf")
    val uri = getFileUri(context,file)
    if (uri != null) {
        onPdfSelected(uri)
    }
}

fun getFileUri(context: Context, file: File): Uri? {
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

fun openPdf(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Open PDF"))
}
