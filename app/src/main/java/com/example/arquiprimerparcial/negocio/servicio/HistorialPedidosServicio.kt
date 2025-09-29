package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.CategoriaDao
import com.example.arquiprimerparcial.data.dao.HistorialPedidosDao
import java.text.SimpleDateFormat
import java.util.*

class HistorialPedidosServicio {


    private val historialPedidosDao: HistorialPedidosDao = HistorialPedidosDao()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun obtenerTodosPedidosPrimitivos(): List<Map<String, Any>> {
        return try {
            // TRANSFORMACIÓN COMPLEJA + AGREGACIÓN DE DATOS (lógica de negocio)
            val pedidosArray = historialPedidosDao.listarTodos()

            val resultado = mutableListOf<Map<String, Any>>()

            for (pedido in pedidosArray) {
                val id = pedido[0] as Int
                val nombreCliente = pedido[1] as String
                val fechaPedido = pedido[2] as java.sql.Timestamp
                val total = pedido[3] as Double

                val detallesArray = historialPedidosDao.obtenerDetallesPedido(id)
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
                    "fecha" to dateFormat.format(Date(fechaPedido.time)), // Formateo de fecha (lógica de negocio)
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

    fun obtenerEstadisticasDia(): Pair<Double, Int> {
        // AGREGACIÓN DE DATOS (lógica de negocio para reportes)
        val ventas = historialPedidosDao.calcularVentasDia()
        val totalPedidos = historialPedidosDao.contarPedidosHoy()
        return Pair(ventas, totalPedidos)
    }

    fun eliminarPedidoCompleto(id: Int): Result<Boolean> {
        return try {
            // VALIDACIÓN DE NEGOCIO
            if (id <= 0) {
                return Result.failure(Exception("ID inválido"))
            }

            // Operación de datos - Delegar a DAO
            val resultado = historialPedidosDao.eliminarPedido(id)
            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar el pedido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // LÓGICA DE NEGOCIO PURA (construcción de mensajes y formateo)
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
    fun formatearPrecio(precio: Double): String {
        return String.format("%.2f", precio)
    }
}