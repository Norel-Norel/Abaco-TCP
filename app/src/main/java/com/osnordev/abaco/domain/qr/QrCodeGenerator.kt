package com.osnordev.abaco.domain.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.osnordev.abaco.domain.model.PaymentQrData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates a QR code [Bitmap] from [PaymentQrData] using ZXing.
 * Format: ABACO_QR:cuenta={account};tel={phone};nombre={name}
 * Requirements: 24.2
 */
@Singleton
class QrCodeGenerator @Inject constructor() {

    /**
     * Encodes [data] into a structured text string and generates a QR bitmap.
     */
    fun generate(data: PaymentQrData, sizePx: Int = 512): Bitmap {
        val content = encode(data)
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    /**
     * Encodes [PaymentQrData] to the structured text format.
     * Requirements: 24.2
     */
    fun encode(data: PaymentQrData): String =
        "ABACO_QR:cuenta=${data.accountNumber};tel=${data.phone};nombre=${data.holderName}"

    /**
     * Decodes the structured text back to [PaymentQrData].
     * Returns null if the format is invalid.
     */
    fun decode(text: String): PaymentQrData? {
        if (!text.startsWith("ABACO_QR:")) return null
        return try {
            val params = text.removePrefix("ABACO_QR:").split(";").associate { part ->
                val (k, v) = part.split("=", limit = 2)
                k to v
            }
            PaymentQrData(
                accountNumber = params["cuenta"] ?: return null,
                phone = params["tel"] ?: return null,
                holderName = params["nombre"] ?: return null
            )
        } catch (e: Exception) {
            null
        }
    }
}
