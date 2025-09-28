package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.HistorialPedidosDao
import java.text.SimpleDateFormat
import java.util.*

object HistorialPedidosServicio {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun obtenerTodosPedidosPrimitivos(): List<Map<String, Any>> {
        return try {
            val pedidosArray = HistorialPedidosDao.listarTodos()

            val resultado = mutableListOf<Map<String, Any>>()

            for (pedido in pedidosArray) {
                val id = pedido[0] as Int
                val nombreCliente = pedido[1] as String
                val fechaPedido = pedido[2] as java.sql.Timestamp
                val total = pedido[3] as Double

                val detallesArray = HistorialPedidosDao.obtenerDetallesPedido(id)
                val detallesPrimitivos = mutableListOf<Map<String, Any>>()
                var cantidadTotal = 0

                for (detalle in detallesArray) {
                    val cantidad = detalle[2] as Int
                    val precioUnitario = detalle[3] as Double
                    cantidadTotal += cantidad

                    detallesPrimitivos.add(mapOf(
                        "idProducto" to (detalle[1] as Int),
                        "nombreProducto" to (detalle[4] as String),
                        "cantidad" to cantidad,
                        "precioUnitario" to precioUnitario,
                        "subtotal" to (cantidad.toDouble() * precioUnitario)
                    ))
                }

                resultado.add(mapOf(
                    "id" to id,
                    "nombreCliente" to nombreCliente,
                    "fecha" to dateFormat.format(Date(fechaPedido.time)),
                    "total" to total,
                    "cantidadProductos" to cantidadTotal,
                    "detalles" to detallesPrimitivos
                ))
            }

            resultado
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun obtenerTodosPedidos(): List<Array<Any>> {
        return HistorialPedidosDao.listarTodos()
    }

    fun obtenerDetallesPedido(idPedido: Int): List<Array<Any>> {
        return HistorialPedidosDao.obtenerDetallesPedido(idPedido)
    }

    fun obtenerEstadisticasDia(): Pair<Double, Int> {
        val ventas = HistorialPedidosDao.calcularVentasDia()
        val totalPedidos = HistorialPedidosDao.contarPedidosHoy()
        return Pair(ventas, totalPedidos)
    }

    fun eliminarPedidoCompleto(id: Int): Result<Boolean> {
        return try {
            if (id <= 0) {
                return Result.failure(Exception("ID inválido"))
            }

            val resultado = HistorialPedidosDao.eliminarPedido(id)
            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar el pedido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun calcularVentasPorPeriodo(fechaInicio: String, fechaFin: String): Double {
        return HistorialPedidosDao.calcularVentasPorPeriodo(fechaInicio, fechaFin)
    }

    fun obtenerPedidosPorRangoFechas(fechaInicio: String, fechaFin: String): List<Array<Any>> {
        return HistorialPedidosDao.listarPorRangoFechas(fechaInicio, fechaFin)
    }

    fun obtenerEstadisticasCompletas(): Map<String, Any> {
        return HistorialPedidosDao.obtenerEstadisticasCompletas()
    }

    fun construirMensajeDetallePrimitivo(
        id: Int,
        nombreCliente: String,
        fecha: String,
        total: Double,
        detalles: List<Map<String, Any>>
    ): String {
        return buildString {
            append("📦 Pedido #$id\n\n")
            append("👤 Cliente: $nombreCliente\n")
            append("📅 Fecha: $fecha\n\n")
            append("🛒 Productos:\n")
            for (detalle in detalles) {
                val nombreProducto = detalle["nombreProducto"] as String
                val cantidad = detalle["cantidad"] as Int
                val precioUnitario = detalle["precioUnitario"] as Double
                val subtotal = detalle["subtotal"] as Double

                append("• $nombreProducto\n")
                append("  $cantidad x S/ ${formatearPrecio(precioUnitario)}")
                append(" = S/ ${formatearPrecio(subtotal)}\n")
            }
            append("\n💰 Total: S/ ${formatearPrecio(total)}")
        }
    }

    fun construirMensajeDetalle(pedidoArray: Array<Any>, detalles: List<Array<Any>>): String {
        val id = pedidoArray[0] as Int
        val nombreCliente = pedidoArray[1] as String
        val fechaPedido = pedidoArray[2]
        val total = pedidoArray[3] as Double

        return buildString {
            append("📦 Pedido #$id\n\n")
            append("👤 Cliente: $nombreCliente\n")
            append("📅 Fecha: $fechaPedido\n\n")
            append("🛒 Productos:\n")

            for (detalle in detalles) {
                val cantidad = detalle[2] as Int
                val precioUnitario = detalle[3] as Double
                val nombreProducto = detalle[4] as String
                val subtotal = cantidad.toDouble() * precioUnitario

                append("• $nombreProducto\n")
                append("  $cantidad x S/ ${formatearPrecio(precioUnitario)}")
                append(" = S/ ${formatearPrecio(subtotal)}\n")
            }
            append("\n💰 Total: S/ ${formatearPrecio(total)}")
        }
    }

    fun validarFecha(fecha: String): Boolean {
        return try {
            val regex = "\\d{4}-\\d{2}-\\d{2}".toRegex()
            regex.matches(fecha)
        } catch (e: Exception) {
            false
        }
    }

    fun validarRangoFechas(fechaInicio: String, fechaFin: String): Boolean {
        return validarFecha(fechaInicio) && validarFecha(fechaFin) && fechaInicio <= fechaFin
    }

    fun formatearPrecio(precio: Double): String {
        return String.format("%.2f", precio)
    }

    fun formatearTotal(total: Double): String {
        return String.format("%.2f", total)
    }

    fun calcularTotalVentas(pedidos: List<Array<Any>>): Double {
        var totalVentas = 0.0
        for (pedido in pedidos) {
            totalVentas += (pedido[3] as Double)
        }
        return totalVentas
    }

    fun calcularPromedioVentas(pedidos: List<Array<Any>>): Double {
        if (pedidos.isEmpty()) return 0.0
        return calcularTotalVentas(pedidos) / pedidos.size
    }

    fun contarPedidosPorCliente(pedidos: List<Array<Any>>): Map<String, Int> {
        val conteo = mutableMapOf<String, Int>()

        for (pedido in pedidos) {
            val nombreCliente = pedido[1] as String
            conteo[nombreCliente] = (conteo[nombreCliente] ?: 0) + 1
        }

        return conteo
    }

    fun obtenerClientesMasFrecuentes(pedidos: List<Array<Any>>, limite: Int = 5): List<Pair<String, Int>> {
        return contarPedidosPorCliente(pedidos)
            .toList()
            .sortedByDescending { it.second }
            .take(limite)
    }

    fun filtrarPedidosPorCliente(pedidos: List<Array<Any>>, nombreCliente: String): List<Array<Any>> {
        val resultado = mutableListOf<Array<Any>>()
        for (pedido in pedidos) {
            val cliente = pedido[1] as String
            if (cliente.contains(nombreCliente, ignoreCase = true)) {
                resultado.add(pedido)
            }
        }
        return resultado
    }

    fun filtrarPedidosPorMontoMinimo(pedidos: List<Array<Any>>, montoMinimo: Double): List<Array<Any>> {
        val resultado = mutableListOf<Array<Any>>()
        for (pedido in pedidos) {
            val total = pedido[3] as Double
            if (total >= montoMinimo) {
                resultado.add(pedido)
            }
        }
        return resultado
    }

    fun ordenarPedidosPorFecha(pedidos: List<Array<Any>>, ascendente: Boolean = false): List<Array<Any>> {
        return if (ascendente) {
            pedidos.sortedBy { it[2] }
        } else {
            pedidos.sortedByDescending { it[2] }
        }
    }

    fun ordenarPedidosPorTotal(pedidos: List<Array<Any>>, ascendente: Boolean = false): List<Array<Any>> {
        return if (ascendente) {
            pedidos.sortedBy { it[3] as Double }
        } else {
            pedidos.sortedByDescending { it[3] as Double }
        }
    }
}