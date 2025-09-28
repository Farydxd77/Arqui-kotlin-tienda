package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.HistorialPedidosDao
import com.example.arquiprimerparcial.negocio.modelo.PedidoModelo

object HistorialPedidosServicio {

    fun obtenerTodosPedidos(): List<PedidoModelo> {
        return HistorialPedidosDao.listarTodos().map { entidad ->
            val detalles = HistorialPedidosDao.obtenerDetallesPedido(entidad.id)
            PedidoModelo(
                id = entidad.id,
                nombreCliente = entidad.nombre_cliente,
                fechaPedido = entidad.fecha_pedido,
                total = entidad.total,
                detalles = detalles.toMutableList()
            )
        }
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
                Result.failure(Exception("Error al eliminar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}