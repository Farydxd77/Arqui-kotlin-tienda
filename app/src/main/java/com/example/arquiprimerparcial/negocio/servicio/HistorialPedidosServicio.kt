package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.HistorialPedidosDao

object HistorialPedidosServicio {

    // Retorna lista de arrays: [id, nombre_cliente, fecha_pedido, total]
    fun obtenerTodosPedidos(): List<Array<Any>> {
        return HistorialPedidosDao.listarTodos()
    }

    // Retorna lista de arrays de detalles: [id_pedido, id_producto, cantidad, precio_unitario, producto_nombre, producto_url]
    fun obtenerDetallesPedido(idPedido: Int): List<Array<Any>> {
        return HistorialPedidosDao.obtenerDetallesPedido(idPedido)
    }

    // Retorna Pair<ventasDelDia, totalPedidosHoy>
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

    // Retorna lista de arrays de pedidos por rango de fechas
    fun obtenerPedidosPorRangoFechas(fechaInicio: String, fechaFin: String): List<Array<Any>> {
        return HistorialPedidosDao.listarPorRangoFechas(fechaInicio, fechaFin)
    }

    // Retorna Map con estadísticas: ["total_pedidos": Int, "total_ventas": Double, "promedio_por_pedido": Double]
    fun obtenerEstadisticasCompletas(): Map<String, Any> {
        return HistorialPedidosDao.obtenerEstadisticasCompletas()
    }

    // Utilidades primitivas para trabajar con arrays de pedidos
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

            detalles.forEach { detalle ->
                val cantidad = detalle[2] as Int
                val precioUnitario = detalle[3] as Double
                val nombreProducto = detalle[4] as String
                val subtotal = cantidad * precioUnitario

                append("• $nombreProducto\n")
                append("  $cantidad x S/ ${formatearPrecio(precioUnitario)}")
                append(" = S/ ${formatearPrecio(subtotal)}\n")
            }
            append("\n💰 Total: S/ ${formatearPrecio(total)}")
        }
    }

    // Validaciones primitivas
    fun validarFecha(fecha: String): Boolean {
        return try {
            // Formato esperado: yyyy-MM-dd
            val regex = "\\d{4}-\\d{2}-\\d{2}".toRegex()
            regex.matches(fecha)
        } catch (e: Exception) {
            false
        }
    }

    fun validarRangoFechas(fechaInicio: String, fechaFin: String): Boolean {
        return validarFecha(fechaInicio) && validarFecha(fechaFin) && fechaInicio <= fechaFin
    }

    // Utilidades de formateo
    fun formatearPrecio(precio: Double): String {
        return String.format("%.2f", precio)
    }

    fun formatearTotal(total: Double): String {
        return String.format("%.2f", total)
    }

    // Cálculos sobre arrays de pedidos
    fun calcularTotalVentas(pedidos: List<Array<Any>>): Double {
        return pedidos.sumOf { pedido ->
            pedido[3] as Double // total
        }
    }

    fun calcularPromedioVentas(pedidos: List<Array<Any>>): Double {
        if (pedidos.isEmpty()) return 0.0
        return calcularTotalVentas(pedidos) / pedidos.size
    }

    fun contarPedidosPorCliente(pedidos: List<Array<Any>>): Map<String, Int> {
        val conteo = mutableMapOf<String, Int>()

        pedidos.forEach { pedido ->
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

    // Filtros sobre arrays de pedidos
    fun filtrarPedidosPorCliente(pedidos: List<Array<Any>>, nombreCliente: String): List<Array<Any>> {
        return pedidos.filter { pedido ->
            val cliente = pedido[1] as String
            cliente.contains(nombreCliente, ignoreCase = true)
        }
    }

    fun filtrarPedidosPorMontoMinimo(pedidos: List<Array<Any>>, montoMinimo: Double): List<Array<Any>> {
        return pedidos.filter { pedido ->
            val total = pedido[3] as Double
            total >= montoMinimo
        }
    }

    fun ordenarPedidosPorFecha(pedidos: List<Array<Any>>, ascendente: Boolean = false): List<Array<Any>> {
        return if (ascendente) {
            pedidos.sortedBy { it[2] } // fecha_pedido
        } else {
            pedidos.sortedByDescending { it[2] } // fecha_pedido
        }
    }

    fun ordenarPedidosPorTotal(pedidos: List<Array<Any>>, ascendente: Boolean = false): List<Array<Any>> {
        return if (ascendente) {
            pedidos.sortedBy { it[3] as Double } // total
        } else {
            pedidos.sortedByDescending { it[3] as Double } // total
        }
    }
}