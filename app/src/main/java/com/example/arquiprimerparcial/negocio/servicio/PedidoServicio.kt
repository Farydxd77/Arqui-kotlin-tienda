package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.DetallePedidoDao
import com.example.arquiprimerparcial.data.dao.PedidoDao
import com.example.arquiprimerparcial.data.dao.ProductoDao
import com.example.arquiprimerparcial.data.entidad.DetallePedidoEntidad
import com.example.arquiprimerparcial.data.entidad.PedidoEntidad
import com.example.arquiprimerparcial.negocio.modelo.DetallePedidoModelo
import com.example.arquiprimerparcial.negocio.modelo.PedidoModelo
import java.sql.Timestamp

object PedidoServicio {

    fun crearPedido(pedido: PedidoModelo): Result<Int> {
        return try {
            // Validaciones
            if (!pedido.esValido()) {
                return Result.failure(Exception("Datos del pedido inválidos"))
            }

            if (pedido.nombreCliente.isBlank()) {
                return Result.failure(Exception("El nombre del cliente es requerido"))
            }

            if (pedido.detalles.isEmpty()) {
                return Result.failure(Exception("El pedido debe tener al menos un producto"))
            }

            // Verificar stock de todos los productos
            for (detalle in pedido.detalles) {
                val producto = ProductoDao.obtenerPorId(detalle.idProducto)
                if (producto == null) {
                    return Result.failure(Exception("Producto no encontrado: ${detalle.nombreProducto}"))
                }
                if (producto.stock < detalle.cantidad) {
                    return Result.failure(Exception("Stock insuficiente para ${producto.nombre}. Stock disponible: ${producto.stock}"))
                }
            }

            // Crear pedido en BD
            val pedidoEntidad = PedidoEntidad(
                nombre_cliente = pedido.nombreCliente,
                fecha_pedido = Timestamp(System.currentTimeMillis()),
                total = pedido.calcularTotal()
            )

            val idPedido = PedidoDao.insertar(pedidoEntidad)
            if (idPedido <= 0) {
                return Result.failure(Exception("Error al crear el pedido"))
            }

            // Crear detalles del pedido
            val detallesEntidad = pedido.detalles.map { detalle ->
                DetallePedidoEntidad(
                    id_pedido = idPedido,
                    id_producto = detalle.idProducto,
                    cantidad = detalle.cantidad,
                    precio_unitario = detalle.precioUnitario
                )
            }

            val resultadoDetalles = DetallePedidoDao.insertarLote(detallesEntidad)
            if (!resultadoDetalles) {
                return Result.failure(Exception("Error al guardar los detalles del pedido"))
            }

            // Actualizar stock de productos
            for (detalle in pedido.detalles) {
                val producto = ProductoDao.obtenerPorId(detalle.idProducto)!!
                val nuevoStock = producto.stock - detalle.cantidad
                ProductoDao.actualizarStock(detalle.idProducto, nuevoStock)
            }

            Result.success(idPedido)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun obtenerPedidos(): List<PedidoModelo> {
        return PedidoDao.listar().map { entidadPedido ->
            val detalles = obtenerDetallesPedido(entidadPedido.id)

            PedidoModelo(
                id = entidadPedido.id,
                nombreCliente = entidadPedido.nombre_cliente,
                fechaPedido = entidadPedido.fecha_pedido,
                total = entidadPedido.total,
                detalles = detalles.toMutableList()
            )
        }
    }

    fun obtenerPedidoPorId(id: Int): PedidoModelo? {
        return PedidoDao.obtenerPorId(id)?.let { entidadPedido ->
            val detalles = obtenerDetallesPedido(id)

            PedidoModelo(
                id = entidadPedido.id,
                nombreCliente = entidadPedido.nombre_cliente,
                fechaPedido = entidadPedido.fecha_pedido,
                total = entidadPedido.total,
                detalles = detalles.toMutableList()
            )
        }
    }

    private fun obtenerDetallesPedido(idPedido: Int): List<DetallePedidoModelo> {
        return DetallePedidoDao.listarPorPedido(idPedido).map { entidadDetalle ->
            val producto = ProductoDao.obtenerPorId(entidadDetalle.id_producto)

            DetallePedidoModelo(
                id = entidadDetalle.id,
                idPedido = entidadDetalle.id_pedido,
                idProducto = entidadDetalle.id_producto,
                nombreProducto = producto?.nombre ?: "Producto no encontrado",
                cantidad = entidadDetalle.cantidad,
                precioUnitario = entidadDetalle.precio_unitario,
                urlProducto = producto?.url ?: ""
            )
        }
    }

    fun eliminarPedido(id: Int): Result<Boolean> {
        return try {
            if (id <= 0) {
                return Result.failure(Exception("ID de pedido inválido"))
            }

            val resultado = PedidoDao.eliminar(id)
            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar el pedido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun obtenerVentasDelDia(): Double {
        val hoy = Timestamp(System.currentTimeMillis())
        return PedidoDao.obtenerTotalVentasPorFecha(hoy)
    }

    fun obtenerTotalPedidosHoy(): Int {
        val hoy = java.sql.Date(System.currentTimeMillis())
        val manana = java.sql.Date(System.currentTimeMillis() + 86400000)
        return PedidoDao.listarPorFecha(
            Timestamp(hoy.time),
            Timestamp(manana.time)
        ).size
    }

    fun validarStockDisponible(idProducto: Int, cantidadSolicitada: Int): Result<Boolean> {
        return try {
            val producto = ProductoDao.obtenerPorId(idProducto)
            if (producto == null) {
                return Result.failure(Exception("Producto no encontrado"))
            }

            if (producto.stock < cantidadSolicitada) {
                return Result.failure(Exception("Stock insuficiente. Disponible: ${producto.stock}"))
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}