package com.osnordev.abaco.domain.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.osnordev.abaco.domain.calculator.BalanceSheet
import java.io.File
import java.io.FileOutputStream

object TrialBalancePdfExporter {

    private const val PAGE_WIDTH = 595   // A4 points
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val LINE_HEIGHT = 20f

    /**
     * Generates a Trial Balance PDF from [sheet] and returns a shareable [Uri].
     */
    fun export(context: Context, sheet: BalanceSheet): Uri {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        draw(page.canvas, sheet)
        document.finishPage(page)

        val file = File(context.cacheDir, "balance_comprobacion_${sheet.cutoffDate}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    private fun draw(canvas: Canvas, sheet: BalanceSheet) {
        val titlePaint = Paint().apply { textSize = 16f; isFakeBoldText = true }
        val headerPaint = Paint().apply { textSize = 11f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 10f }
        val linePaint = Paint().apply { strokeWidth = 0.5f }

        var y = MARGIN + 20f

        // Title
        canvas.drawText("Balance de Comprobación", MARGIN, y, titlePaint)
        y += LINE_HEIGHT
        canvas.drawText("Fecha de corte: ${sheet.cutoffDate}", MARGIN, y, bodyPaint)
        y += LINE_HEIGHT * 1.5f

        // Header row
        canvas.drawText("Cuenta", MARGIN, y, headerPaint)
        canvas.drawText("Tipo", 220f, y, headerPaint)
        canvas.drawText("Débito", 340f, y, headerPaint)
        canvas.drawText("Crédito", 440f, y, headerPaint)
        y += 4f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += LINE_HEIGHT

        // All accounts
        val allAccounts = sheet.assets + sheet.liabilities + sheet.equity
        var totalDebit = 0.0
        var totalCredit = 0.0

        for (account in allAccounts) {
            canvas.drawText(account.accountName.take(28), MARGIN, y, bodyPaint)
            canvas.drawText(account.accountType.name, 220f, y, bodyPaint)
            canvas.drawText("%.2f".format(account.debitTotal), 340f, y, bodyPaint)
            canvas.drawText("%.2f".format(account.creditTotal), 440f, y, bodyPaint)
            totalDebit += account.debitTotal
            totalCredit += account.creditTotal
            y += LINE_HEIGHT
            if (y > PAGE_HEIGHT - MARGIN) break  // simple overflow guard
        }

        // Totals
        y += 4f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += LINE_HEIGHT
        canvas.drawText("TOTALES", MARGIN, y, headerPaint)
        canvas.drawText("%.2f".format(totalDebit), 340f, y, headerPaint)
        canvas.drawText("%.2f".format(totalCredit), 440f, y, headerPaint)
    }
}
