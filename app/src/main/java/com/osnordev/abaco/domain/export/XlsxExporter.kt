package com.osnordev.abaco.domain.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.osnordev.abaco.domain.model.Transaction
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private val DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/**
 * Exports transactions to a minimal XLSX file (OpenXML format) without external libraries.
 * The XLSX format is a ZIP archive containing XML files.
 * Requirements: 14.2
 */
@Singleton
class XlsxExporter @Inject constructor() {

    fun export(context: Context, transactions: List<Transaction>, fileName: String = "transacciones.xlsx"): Intent {
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { fos ->
            ZipOutputStream(fos).use { zip ->
                writeContentTypes(zip)
                writeRels(zip)
                writeWorkbookRels(zip)
                writeWorkbook(zip)
                writeSharedStrings(zip, transactions)
                writeSheet(zip, transactions)
            }
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun writeContentTypes(zip: ZipOutputStream) {
        zip.putNextEntry(ZipEntry("[Content_Types].xml"))
        zip.write(
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
</Types>""".trimIndent().toByteArray()
        )
        zip.closeEntry()
    }

    private fun writeRels(zip: ZipOutputStream) {
        zip.putNextEntry(ZipEntry("_rels/.rels"))
        zip.write(
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""".trimIndent().toByteArray()
        )
        zip.closeEntry()
    }

    private fun writeWorkbookRels(zip: ZipOutputStream) {
        zip.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
        zip.write(
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
</Relationships>""".trimIndent().toByteArray()
        )
        zip.closeEntry()
    }

    private fun writeWorkbook(zip: ZipOutputStream) {
        zip.putNextEntry(ZipEntry("xl/workbook.xml"))
        zip.write(
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
          xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Transacciones" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>""".trimIndent().toByteArray()
        )
        zip.closeEntry()
    }

    private fun writeSharedStrings(zip: ZipOutputStream, transactions: List<Transaction>) {
        // Build shared strings table from headers + all string cell values
        val headers = listOf("ID", "Tipo", "Importe", "Moneda", "Importe CUP", "Categoría", "Descripción", "Fecha", "Año", "Mes")
        val strings = mutableListOf<String>()
        strings.addAll(headers)
        transactions.forEach { tx ->
            strings.add(tx.type.name)
            strings.add(tx.currency.name)
            strings.add(tx.category)
            strings.add(tx.description)
            strings.add(tx.date.format(DATE_FMT))
        }

        zip.putNextEntry(ZipEntry("xl/sharedStrings.xml"))
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="${strings.size}" uniqueCount="${strings.size}">""")
        strings.forEach { s ->
            sb.append("<si><t>${s.xmlEscape()}</t></si>")
        }
        sb.append("</sst>")
        zip.write(sb.toString().toByteArray())
        zip.closeEntry()
    }

    private fun writeSheet(zip: ZipOutputStream, transactions: List<Transaction>) {
        val headers = listOf("ID", "Tipo", "Importe", "Moneda", "Importe CUP", "Categoría", "Descripción", "Fecha", "Año", "Mes")
        // Shared string indices: headers first, then per-row strings
        val headerIndices = headers.indices.toList()

        zip.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")

        // Header row
        sb.append("""<row r="1">""")
        headers.forEachIndexed { col, _ ->
            val cellRef = colLetter(col) + "1"
            sb.append("""<c r="$cellRef" t="s"><v>${headerIndices[col]}</v></c>""")
        }
        sb.append("</row>")

        // Data rows — string offset starts after headers
        var ssIdx = headers.size
        transactions.forEachIndexed { rowIdx, tx ->
            val rowNum = rowIdx + 2
            sb.append("""<row r="$rowNum">""")
            // ID (number)
            sb.append("""<c r="${colLetter(0)}$rowNum"><v>${tx.id}</v></c>""")
            // Type (shared string)
            sb.append("""<c r="${colLetter(1)}$rowNum" t="s"><v>${ssIdx++}</v></c>""")
            // Amount (number)
            sb.append("""<c r="${colLetter(2)}$rowNum"><v>${tx.amount}</v></c>""")
            // Currency (shared string)
            sb.append("""<c r="${colLetter(3)}$rowNum" t="s"><v>${ssIdx++}</v></c>""")
            // AmountCup (number)
            sb.append("""<c r="${colLetter(4)}$rowNum"><v>${tx.amountCup}</v></c>""")
            // Category (shared string)
            sb.append("""<c r="${colLetter(5)}$rowNum" t="s"><v>${ssIdx++}</v></c>""")
            // Description (shared string)
            sb.append("""<c r="${colLetter(6)}$rowNum" t="s"><v>${ssIdx++}</v></c>""")
            // Date (shared string)
            sb.append("""<c r="${colLetter(7)}$rowNum" t="s"><v>${ssIdx++}</v></c>""")
            // Year (number)
            sb.append("""<c r="${colLetter(8)}$rowNum"><v>${tx.year}</v></c>""")
            // Month (number)
            sb.append("""<c r="${colLetter(9)}$rowNum"><v>${tx.month}</v></c>""")
            sb.append("</row>")
        }

        sb.append("</sheetData></worksheet>")
        zip.write(sb.toString().toByteArray())
        zip.closeEntry()
    }

    private fun colLetter(index: Int): String {
        var n = index
        val sb = StringBuilder()
        do {
            sb.insert(0, ('A' + n % 26))
            n = n / 26 - 1
        } while (n >= 0)
        return sb.toString()
    }

    private fun String.xmlEscape(): String = this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
