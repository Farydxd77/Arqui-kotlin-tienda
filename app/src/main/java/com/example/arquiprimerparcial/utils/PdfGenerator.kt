package com.example.arquiprimerparcial.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator(private val context: Context) {

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun generarReportePDF(datosReporte: Map<String, Any>): String {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
            color = Color.rgb(29, 53, 87) // Color primary
        }
        val headerPaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
            color = Color.rgb(69, 123, 157) // Color primaryLight
        }
        val normalPaint = Paint().apply {
            textSize = 14f
            color = Color.BLACK
        }
        val smallPaint = Paint().apply {
            textSize = 12f
            color = Color.DKGRAY
        }

        var pageNumber = 1
        var yPosition = 50f

        // Página 1: Portada y Resumen
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create() // A4
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Título principal
        canvas.drawText("📊 REPORTE DE VENTAS", 50f, yPosition, titlePaint)
        yPosition += 40f

        // Información del reporte
        val tipoReporte = datosReporte["tipoReporte"] as String
        val fechaInicio = datosReporte["fechaInicio"] as String
        val fechaFin = datosReporte["fechaFin"] as String
        val fechaGeneracion = datosReporte["fechaGeneracion"] as String

        canvas.drawText(tipoReporte, 50f, yPosition, headerPaint)
        yPosition += 30f
        canvas.drawText("Período: $fechaInicio - $fechaFin", 50f, yPosition, normalPaint)
        yPosition += 25f
        canvas.drawText("Generado: $fechaGeneracion", 50f, yPosition, smallPaint)
        yPosition += 40f

        // Línea separadora
        canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
        yPosition += 30f

        // RESUMEN GENERAL
        @Suppress("UNCHECKED_CAST")
        val resumen = datosReporte["resumen"] as Map<String, Any>

        canvas.drawText("💰 RESUMEN GENERAL", 50f, yPosition, headerPaint)
        yPosition += 30f

        canvas.drawText("Total de Pedidos: ${resumen["totalPedidos"]}", 70f, yPosition, normalPaint)
        yPosition += 25f
        canvas.drawText("Total en Ventas: S/ ${String.format("%.2f", resumen["totalVentas"])}", 70f, yPosition, normalPaint)
        yPosition += 25f
        canvas.drawText("Promedio por Venta: S/ ${String.format("%.2f", resumen["promedioVenta"])}", 70f, yPosition, normalPaint)
        yPosition += 25f
        canvas.drawText("Venta Máxima: S/ ${String.format("%.2f", resumen["ventaMaxima"])}", 70f, yPosition, normalPaint)
        yPosition += 25f
        canvas.drawText("Venta Mínima: S/ ${String.format("%.2f", resumen["ventaMinima"])}", 70f, yPosition, normalPaint)
        yPosition += 40f

        // PRODUCTOS MÁS VENDIDOS
        canvas.drawText("🏆 TOP 10 PRODUCTOS MÁS VENDIDOS", 50f, yPosition, headerPaint)
        yPosition += 30f

        @Suppress("UNCHECKED_CAST")
        val productosMasVendidos = datosReporte["productosMasVendidos"] as List<Array<Any>>

        var posicion = 1
        for (producto in productosMasVendidos.take(10)) {
            val nombre = producto[0] as String
            val categoria = producto[1] as String
            val cantidad = producto[2] as Int
            val ingresos = producto[3] as Double

            if (yPosition > 750) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }

            canvas.drawText("$posicion. $nombre", 70f, yPosition, normalPaint)
            yPosition += 20f
            canvas.drawText("   Categoría: $categoria | Vendido: $cantidad | Ingresos: S/ ${String.format("%.2f", ingresos)}", 70f, yPosition, smallPaint)
            yPosition += 25f
            posicion++
        }

        yPosition += 20f

        // VENTAS POR CATEGORÍA
        if (yPosition > 700) {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 50f
        }

        canvas.drawText("🏷️ VENTAS POR CATEGORÍA", 50f, yPosition, headerPaint)
        yPosition += 30f

        @Suppress("UNCHECKED_CAST")
        val ventasPorCategoria = datosReporte["ventasPorCategoria"] as List<Array<Any>>

        for (categoria in ventasPorCategoria) {
            val nombreCategoria = categoria[0] as String
            val totalPedidos = categoria[1] as Int
            val productosVendidos = categoria[2] as Int
            val totalVentas = categoria[3] as Double

            if (yPosition > 750) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }

            canvas.drawText("• $nombreCategoria", 70f, yPosition, normalPaint)
            yPosition += 20f
            canvas.drawText("   Pedidos: $totalPedidos | Productos: $productosVendidos | Total: S/ ${String.format("%.2f", totalVentas)}", 70f, yPosition, smallPaint)
            yPosition += 25f
        }

        yPosition += 20f

        // PRODUCTOS CON BAJO STOCK (ALERTA)
        if (yPosition > 700) {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 50f
        }

        canvas.drawText("⚠️ ALERTA: PRODUCTOS CON BAJO STOCK", 50f, yPosition, headerPaint)
        yPosition += 30f

        @Suppress("UNCHECKED_CAST")
        val productosBajoStock = datosReporte["productosBajoStock"] as List<Array<Any>>

        if (productosBajoStock.isEmpty()) {
            canvas.drawText("✅ No hay productos con bajo stock", 70f, yPosition, normalPaint)
            yPosition += 30f
        } else {
            for (producto in productosBajoStock) {
                val nombre = producto[0] as String
                val categoria = producto[1] as String
                val stock = producto[2] as Int
                val precio = producto[3] as Double

                if (yPosition > 750) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }

                canvas.drawText("• $nombre", 70f, yPosition, normalPaint)
                yPosition += 20f
                canvas.drawText("   Stock: $stock unidades | Categoría: $categoria | Precio: S/ ${String.format("%.2f", precio)}", 70f, yPosition, smallPaint)
                yPosition += 25f
            }
        }

        pdfDocument.finishPage(page)

        // Guardar el PDF
        val fileName = "Reporte_${dateFormat.format(Date())}.pdf"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            return file.absolutePath
        } catch (e: Exception) {
            throw Exception("Error al guardar el PDF: ${e.message}")
        } finally {
            pdfDocument.close()
        }
    }
}
