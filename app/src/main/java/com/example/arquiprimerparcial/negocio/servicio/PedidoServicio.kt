package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.DetallePedidoDao
import com.example.arquiprimerparcial.data.dao.PedidoDao
import com.example.arquiprimerparcial.data.dao.ProductoDao
import java.sql.Timestamp

class PedidoServicio {

    // Instancias privadas de los DAOs
    private val pedidoDao: PedidoDao = PedidoDao()
    private val detallePedidoDao: DetallePedidoDao = DetallePedidoDao()
    private val productoDao: ProductoDao = ProductoDao()

    fun crearPedidoPrimitivo(pedidoData: Map<String, Any>): Result<Int> {
        return try {
            val nombreCliente = pedidoData["nombreCliente"] as String
            @Suppress("UNCHECKED_CAST")
            val detalles = pedidoData["detalles"] as List<Map<String, Any>>

            val detallesArray = detalles.map { detalle ->
                arrayOf<Any>(
                    detalle["idProducto"] as Int,
                    detalle["cantidad"] as Int,
                    detalle["precioUnitario"] as Double
                )
            }

            crearPedido(nombreCliente, detallesArray)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun crearPedido(nombreCliente: String, detalles: List<Array<Any>>): Result<Int> {
        return try {
            if (nombreCliente.isBlank()) {
                return Result.failure(Exception("El nombre del cliente es requerido"))
            }

            if (nombreCliente.length > 100) {
                return Result.failure(Exception("El nombre del cliente no puede exceder 100 caracteres"))
            }

            if (detalles.isEmpty()) {
                return Result.failure(Exception("El pedido debe tener al menos un producto"))
            }

            for (detalle in detalles) {
                val idProducto = detalle[0] as Int
                val cantidad = detalle[1] as Int

                val productoArray = productoDao.obtenerPorId(idProducto)
                if (productoArray == null) {
                    return Result.failure(Exception("Producto no encontrado con ID: $idProducto"))
                }

                val stock = productoArray[5] as Int
                val nombreProducto = productoArray[1] as String

                if (stock < cantidad) {
                    return Result.failure(Exception("Stock insuficiente para $nombreProducto. Stock disponible: $stock"))
                }
            }

            val total = calcularTotalDetalles(detalles)

            val idPedido = pedidoDao.insertar(
                nombreCliente = nombreCliente.trim(),
                fechaPedido = Timestamp(System.currentTimeMillis()),
                total = total
            )

            if (idPedido <= 0) {
                return Result.failure(Exception("Error al crear el pedido"))
            }

            val detallesParaInsertar = detalles.map { detalle ->
                arrayOf<Any>(
                    idPedido,
                    detalle[0] as Int,
                    detalle[1] as Int,
                    detalle[2] as Double
                )
            }

            val resultadoDetalles = detallePedidoDao.insertarLote(detallesParaInsertar)
            if (!resultadoDetalles) {
                return Result.failure(Exception("Error al guardar los detalles del pedido"))
            }

            for (detalle in detalles) {
                val idProducto = detalle[0] as Int
                val cantidad = detalle[1] as Int

                val productoArray = productoDao.obtenerPorId(idProducto)!!
                val stockActual = productoArray[5] as Int
                val nuevoStock = stockActual - cantidad

                productoDao.actualizarStock(idProducto, nuevoStock)
            }

            Result.success(idPedido)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun calcularTotalDetalles(detalles: List<Array<Any>>): Double {
        var total = 0.0
        for (detalle in detalles) {
            val cantidad = detalle[1] as Int
            val precioUnitario = detalle[2] as Double
            total += cantidad * precioUnitario
        }
        return total
    }

}