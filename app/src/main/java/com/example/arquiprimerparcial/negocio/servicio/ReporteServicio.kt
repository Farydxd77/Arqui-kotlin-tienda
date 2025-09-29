package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.ReporteDao
import java.text.SimpleDateFormat
import java.util.*

class ReporteServicio {

    private val reporteDao: ReporteDao = ReporteDao()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // OBTENER DATOS PARA REPORTE COMPLETO
    fun generarDatosReporte(tipoReporte: String): Map<String, Any> {
        val fechas = calcularRangoFechas(tipoReporte)
        val fechaInicio = fechas.first
        val fechaFin = fechas.second

        val datos = mutableMapOf<String, Any>()
        datos["tipoReporte"] = obtenerNombreReporte(tipoReporte)
        datos["fechaInicio"] = displayDateFormat.format(fechaInicio.time)
        datos["fechaFin"] = displayDateFormat.format(fechaFin.time)
        datos["fechaGeneracion"] = displayDateFormat.format(Date())

        // Resumen general
        val resumen = reporteDao.obtenerResumenGeneral(
            dateFormat.format(fechaInicio.time),
            dateFormat.format(fechaFin.time)
        )
        datos["resumen"] = resumen

        // Productos más vendidos
        val productosMasVendidos = reporteDao.obtenerProductosMasVendidos(
            dateFormat.format(fechaInicio.time),
            dateFormat.format(fechaFin.time),
            10
        )
        datos["productosMasVendidos"] = productosMasVendidos

        // Ventas por categoría
        val ventasPorCategoria = reporteDao.obtenerVentasPorCategoria(
            dateFormat.format(fechaInicio.time),
            dateFormat.format(fechaFin.time)
        )
        datos["ventasPorCategoria"] = ventasPorCategoria

        // Productos con bajo stock
        val productosBajoStock = reporteDao.obtenerProductosBajoStock(5)
        datos["productosBajoStock"] = productosBajoStock

        // Clientes frecuentes
        val clientesFrecuentes = reporteDao.obtenerClientesFrecuentes(
            dateFormat.format(fechaInicio.time),
            dateFormat.format(fechaFin.time),
            10
        )
        datos["clientesFrecuentes"] = clientesFrecuentes

        // Ventas diarias
        val ventasDiarias = reporteDao.obtenerVentasPorPeriodo(
            dateFormat.format(fechaInicio.time),
            dateFormat.format(fechaFin.time)
        )
        datos["ventasDiarias"] = ventasDiarias

        return datos
    }

    // CALCULAR RANGOS DE FECHAS
    private fun calcularRangoFechas(tipoReporte: String): Pair<Calendar, Calendar> {
        val fechaInicio = Calendar.getInstance()
        val fechaFin = Calendar.getInstance()

        when (tipoReporte) {
            "HOY" -> {
                fechaInicio.set(Calendar.HOUR_OF_DAY, 0)
                fechaInicio.set(Calendar.MINUTE, 0)
                fechaInicio.set(Calendar.SECOND, 0)
                fechaInicio.set(Calendar.MILLISECOND, 0)

                fechaFin.set(Calendar.HOUR_OF_DAY, 23)
                fechaFin.set(Calendar.MINUTE, 59)
                fechaFin.set(Calendar.SECOND, 59)
            }
            "SEMANA_ACTUAL" -> {
                fechaInicio.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                fechaInicio.set(Calendar.HOUR_OF_DAY, 0)
                fechaInicio.set(Calendar.MINUTE, 0)
                fechaInicio.set(Calendar.SECOND, 0)

                fechaFin.set(Calendar.HOUR_OF_DAY, 23)
                fechaFin.set(Calendar.MINUTE, 59)
                fechaFin.set(Calendar.SECOND, 59)
            }
            "MES_ACTUAL" -> {
                fechaInicio.set(Calendar.DAY_OF_MONTH, 1)
                fechaInicio.set(Calendar.HOUR_OF_DAY, 0)
                fechaInicio.set(Calendar.MINUTE, 0)
                fechaInicio.set(Calendar.SECOND, 0)

                fechaFin.set(Calendar.HOUR_OF_DAY, 23)
                fechaFin.set(Calendar.MINUTE, 59)
                fechaFin.set(Calendar.SECOND, 59)
            }
            "ULTIMOS_7_DIAS" -> {
                fechaInicio.add(Calendar.DAY_OF_YEAR, -7)
                fechaInicio.set(Calendar.HOUR_OF_DAY, 0)
                fechaInicio.set(Calendar.MINUTE, 0)
                fechaInicio.set(Calendar.SECOND, 0)

                fechaFin.set(Calendar.HOUR_OF_DAY, 23)
                fechaFin.set(Calendar.MINUTE, 59)
                fechaFin.set(Calendar.SECOND, 59)
            }
            "ULTIMOS_30_DIAS" -> {
                fechaInicio.add(Calendar.DAY_OF_YEAR, -30)
                fechaInicio.set(Calendar.HOUR_OF_DAY, 0)
                fechaInicio.set(Calendar.MINUTE, 0)
                fechaInicio.set(Calendar.SECOND, 0)

                fechaFin.set(Calendar.HOUR_OF_DAY, 23)
                fechaFin.set(Calendar.MINUTE, 59)
                fechaFin.set(Calendar.SECOND, 59)
            }
            "MES_PASADO" -> {
                fechaFin.add(Calendar.MONTH, -1)
                fechaFin.set(Calendar.DAY_OF_MONTH, fechaFin.getActualMaximum(Calendar.DAY_OF_MONTH))
                fechaFin.set(Calendar.HOUR_OF_DAY, 23)
                fechaFin.set(Calendar.MINUTE, 59)
                fechaFin.set(Calendar.SECOND, 59)

                fechaInicio.add(Calendar.MONTH, -1)
                fechaInicio.set(Calendar.DAY_OF_MONTH, 1)
                fechaInicio.set(Calendar.HOUR_OF_DAY, 0)
                fechaInicio.set(Calendar.MINUTE, 0)
                fechaInicio.set(Calendar.SECOND, 0)
            }
            else -> {
                // Por defecto, últimos 30 días
                fechaInicio.add(Calendar.DAY_OF_YEAR, -30)
                fechaInicio.set(Calendar.HOUR_OF_DAY, 0)
                fechaInicio.set(Calendar.MINUTE, 0)
                fechaInicio.set(Calendar.SECOND, 0)

                fechaFin.set(Calendar.HOUR_OF_DAY, 23)
                fechaFin.set(Calendar.MINUTE, 59)
                fechaFin.set(Calendar.SECOND, 59)
            }
        }

        return Pair(fechaInicio, fechaFin)
    }

    private fun obtenerNombreReporte(tipoReporte: String): String {
        return when (tipoReporte) {
            "HOY" -> "Reporte del Día"
            "SEMANA_ACTUAL" -> "Reporte de la Semana Actual"
            "MES_ACTUAL" -> "Reporte del Mes Actual"
            "ULTIMOS_7_DIAS" -> "Reporte de los Últimos 7 Días"
            "ULTIMOS_30_DIAS" -> "Reporte de los Últimos 30 Días"
            "MES_PASADO" -> "Reporte del Mes Pasado"
            else -> "Reporte General"
        }
    }

    // FORMATEO DE NÚMEROS
    fun formatearPrecio(precio: Double): String {
        return String.format("%.2f", precio)
    }

    fun formatearPorcentaje(valor: Double): String {
        return String.format("%.1f%%", valor)
    }
}