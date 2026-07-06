package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportExporter {
    fun exportToPdf(
        context: Context,
        reportTitle: String,
        headers: List<String>,
        rows: List<List<String>>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Paints
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 18f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }

            val subtitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 11f
                textAlign = Paint.Align.RIGHT
            }

            val headerPaint = Paint().apply {
                color = Color.WHITE
                textSize = 10f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }

            val bodyPaint = Paint().apply {
                color = Color.BLACK
                textSize = 9f
                textAlign = Paint.Align.RIGHT
            }

            val gridPaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 0.5f
                style = Paint.Style.STROKE
            }

            val headerBgPaint = Paint().apply {
                color = Color.parseColor("#1565C0") // Primary Blue
                style = Paint.Style.FILL
            }

            val alternatingBgPaint = Paint().apply {
                color = Color.parseColor("#F4F6F9")
                style = Paint.Style.FILL
            }

            val margin = 30f
            val width = 595f
            var y = 50f

            // 1. Draw Title & Brand Info
            canvas.drawText("نظام الأثير لإدارة الدروس الخصوصية", width - margin, y, subtitlePaint)
            y += 22f
            canvas.drawText(reportTitle, width - margin, y, titlePaint)
            y += 18f

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            canvas.drawText("تاريخ إصدار التقرير: ${sdf.format(Date())}", width - margin, y, subtitlePaint)
            y += 25f

            // Draw line below brand
            canvas.drawLine(margin, y, width - margin, y, gridPaint)
            y += 20f

            // 2. Draw Table
            val tableWidth = width - (margin * 2)
            val columnWidth = tableWidth / headers.size

            // Draw Header Row Background
            canvas.drawRect(margin, y - 14f, width - margin, y + 6f, headerBgPaint)

            // Draw Header Column text (RTL ordering)
            for (i in headers.indices) {
                val header = headers[i]
                // Distribute columns from right to left
                val x = width - margin - (i * columnWidth) - 10f
                canvas.drawText(header, x, y, headerPaint)
            }
            y += 20f

            // Draw Body Rows
            for (rowIndex in rows.indices) {
                val row = rows[rowIndex]
                
                // Height guard for single page
                if (y > 800f) {
                    break
                }

                // Alternating row styling
                if (rowIndex % 2 == 1) {
                    canvas.drawRect(margin, y - 12f, width - margin, y + 4f, alternatingBgPaint)
                }

                // Draw cell contents
                for (colIndex in row.indices) {
                    val text = row.getOrNull(colIndex) ?: ""
                    val x = width - margin - (colIndex * columnWidth) - 10f
                    canvas.drawText(text, x, y, bodyPaint)
                }

                // Table grid line
                canvas.drawLine(margin, y + 4f, width - margin, y + 4f, gridPaint)
                y += 18f
            }

            pdfDocument.finishPage(page)

            // Save PDF to documents directory
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(directory, "al_atheer_report_$timestamp.pdf")
            
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            // Build Share Intent
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "تصدير ومشاركة التقرير عبر:"))
            Toast.makeText(context, "تم حفظ التقرير في المستندات", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "فشل في تصدير التقرير: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
