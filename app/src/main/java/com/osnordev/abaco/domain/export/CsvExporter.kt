package com.osnordev.abaco.domain.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.osnordev.abaco.domain.model.Transaction
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private val DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/**
 * Exports a list of transactions to a CSV file and returns a share Intent.
 * Requirements: 14.1
 */
@Singleton
class CsvExporter @Inject constructor() {

    fun export(context: Context, transactions: List<Transaction>, fileName: String = "transacciones.csv"): Intent {
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).bufferedWriter().use { writer ->
            // Header
            writer.write("ID,Tipo,Importe,Moneda,Importe CUP,Categoría,Descripción,Fecha,Año,Mes\n")
            // Rows
            transactions.forEach { tx ->
                val row = listOf(
                    tx.id.toString(),
                    tx.type.name,
                    tx.amount.toString(),
                    tx.currency.name,
                    tx.amountCup.toString(),
                    tx.category.escapeCsv(),
                    tx.description.escapeCsv(),
                    tx.date.format(DATE_FMT),
                    tx.year.toString(),
                    tx.month.toString()
                ).joinToString(",")
                writer.write("$row\n")
            }
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun String.escapeCsv(): String {
        return if (contains(",") || contains("\"") || contains("\n")) {
            "\"${replace("\"", "\"\"")}\""
        } else this
    }
}
