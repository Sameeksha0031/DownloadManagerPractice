package com.example.downloadingfeaturesapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.example.downloadingfeaturesapplication.ui.theme.Pink40
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class PdfBitmapConverter(private val context: Context) {
    var render: PdfRenderer? = null

    suspend fun pdfToBitmaps(contentUri: Uri): List<Bitmap> {
        return withContext(Dispatchers.IO) {
            render?.close()

            context
                .contentResolver
                .openFileDescriptor(contentUri, "r")
                ?.use { descriptor ->
                    with(PdfRenderer(descriptor)) {
                        render = this

                        return@withContext (0 until pageCount).map { index ->
                            async {
                                openPage(index).use { page ->
                                    val bitmap = Bitmap.createBitmap(
                                        page.width,
                                        page.height,
                                        Bitmap.Config.ARGB_8888
                                    )

                                    val canvas = Canvas(bitmap).apply {
                                        drawBitmap(bitmap, 0f, 0f, null)
                                    }

                                    page.render(
                                        bitmap,
                                        null,
                                        null,
                                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                    )

                                    bitmap
                                }
                            }
                        }.awaitAll()
                    }
                }
            return@withContext emptyList()
        }
    }
}