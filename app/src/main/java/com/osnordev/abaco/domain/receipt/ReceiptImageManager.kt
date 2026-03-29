package com.osnordev.abaco.domain.receipt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptImageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MAX_SIZE_BYTES = 500 * 1024  // 500 KB
        private const val RECEIPTS_DIR = "receipts"
    }

    private val receiptsDir: File
        get() = File(context.filesDir, RECEIPTS_DIR).also { it.mkdirs() }

    /**
     * Saves an image from [uri] to internal storage, compressing it to ≤500 KB.
     * Returns the absolute path of the saved file, or null on failure.
     */
    fun saveFromUri(uri: Uri, transactionId: Long): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            val file = File(receiptsDir, "receipt_${transactionId}_${System.currentTimeMillis()}.jpg")
            compressToFile(original, file)
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /** Deletes the receipt file at [path]. */
    fun delete(path: String) {
        File(path).takeIf { it.exists() }?.delete()
    }

    /** Creates a temporary file for camera capture. */
    fun createTempImageFile(): File =
        File(receiptsDir, "temp_receipt_${System.currentTimeMillis()}.jpg")

    private fun compressToFile(bitmap: Bitmap, target: File) {
        var quality = 90
        do {
            FileOutputStream(target).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            quality -= 10
        } while (target.length() > MAX_SIZE_BYTES && quality > 10)
    }
}
