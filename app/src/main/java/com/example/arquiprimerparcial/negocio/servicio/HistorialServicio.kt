package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.DetallePedidoDao
import com.example.arquiprimerparcial.data.dao.PedidoDao
import com.example.arquiprimerparcial.negocio.modelo.DetallePedidoModelo
import com.example.arquiprimerparcial.negocio.modelo.PedidoModelo

class HistorialServicio(
    private val pedidoDao: PedidoDao,
    private val detallePedidoDao: DetallePedidoDao
) {

    suspend fun obtenerHistorialCompleto(): List<PedidoModelo> {
        val pedidos = pedidoDao.obtenerTodos()
        return pedidos.map { pedidoEntidad ->
            val detallesEntidad = detallePedidoDao.obtenerPorPedidoId(pedidoEntidad.id)

            PedidoModelo(
                id = pedidoEntidad.id,
                nombreCliente = pedidoEntidad.nombreCliente,
                fecha = pedidoEntidad.fecha,
                total = pedidoEntidad.total,
                detalles = detallesEntidad.map { detalleEntidad ->
                    DetallePedidoModelo(
                        id = detalleEntidad.id,
                        nombreProducto = detalleEntidad.nombreProducto,
                        cantidad = detalleEntidad.cantidad,
                        precio = detalleEntidad.precio,
                        pedidoId = detalleEntidad.pedidoId
                    )
                }
            )
        }
    }

    suspend fun obtenerResumenDelDia(): Pair<Double, Int> {
        val ventasDelDia = pedidoDao.obtenerVentasDelDia()
        val totalPedidos = pedidoDao.obtenerTotalPedidosHoy()
        return Pair(ventasDelDia, totalPedidos)
    }

    suspend fun eliminarPedidoDelHistorial(id: Int): Boolean {
        return try {
            // Eliminar detalles primero (por las constraints FK)
            detallePedidoDao.eliminarPorPedidoId(id)
            // Luego eliminar pedido
            pedidoDao.eliminar(id)
            true
        } catch (e: Exception) {
            false
        }
    }
}