package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.DetallePedidoDao
import com.example.arquiprimerparcial.data.dao.PedidoDao
import com.example.arquiprimerparcial.data.dao.ProductoDao
import java.sql.Timestamp

object PedidoServicio {

    // Crear pedido con parámetros primitivos
    // detalles: Lista de arrays [idProducto, cantidad, precioUnitario]
    fun crearPedido(nombreCliente: String, detalles: List<Array<Any>>): Result<Int> {
        return try {
            // Validaciones primitivas
            if (nombreCliente.isBlank()) {
                return Result.failure(Exception("El nombre del cliente es requerido"))
            }

            if (nombreCliente.length > 100) {
                return Result.failure(Exception("El nombre del cliente no puede exceder 100 caracteres"))
            }

            if (detalles.isEmpty()) {
                return Result.failure(Exception("El pedido debe tener al menos un producto"))
            }

            // Verificar stock de todos los productos
            for (detalle in detalles) {
                val idProducto = detalle[0] as Int
                val cantidad = detalle[1] as Int

                val productoArray = ProductoDao.obtenerPorId(idProducto)
                if (productoArray == null) {
                    return Result.failure(Exception("Producto no encontrado con ID: $idProducto"))
                }

                val stock = productoArray[5] as Int
                val nombreProducto = productoArray[1] as String

                if (stock < cantidad) {
                    return Result.failure(Exception("Stock insuficiente para $nombreProducto. Stock disponible: $stock"))
                }
            }

            // Calcular total
            val total = calcularTotalDetalles(detalles)

            // Crear pedido en BD
            val idPedido = PedidoDao.insertar(
                nombreCliente = nombreCliente.trim(),
                fechaPedido = Timestamp(System.currentTimeMillis()),
                total = total
            )

            if (idPedido <= 0) {
                return Result.failure(Exception("Error al crear el pedido"))
            }

            // Crear detalles del pedido
            val detallesParaInsertar = detalles.map { detalle ->
                arrayOf(
                    idPedido,           // id_pedido
                    detalle[0] as Int,  // id_producto
                    detalle[1] as Int,  // cantidad
                    detalle[2] as Double // precio_unitario
                )
            }

            val resultadoDetalles = DetallePedidoDao.insertarLote(detallesParaInsertar)
            if (!resultadoDetalles) {
                return Result.failure(Exception("Error al guardar los detalles del pedido"))
            }

            // Actualizar stock de productos
            for (detalle in detalles) {
                val idProducto = detalle[0] as Int
                val cantidad = detalle[1] as Int

                val productoArray = ProductoDao.obtenerPorId(idProducto)!!
                val stockActual = productoArray[5] as Int
                val nuevoStock = stockActual - cantidad

                ProductoDao.actualizarStock(idProducto, nuevoStock)
            }

            Result.success(idPedido)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Retorna lista de arrays: [id, nombre_cliente, fecha_pedido, total]
    fun obtenerPedidos(): List<Array<Any>> {
        return PedidoDao.listar()
    }

    // Retorna array [id, nombre_cliente, fecha_pedido, total] o null
    fun obtenerPedidoPorId(id: Int): Array<Any>? {
        return PedidoDao.obtenerPorId(id)
    }

    // Retorna lista de arrays de detalles: [id_pedido, id_producto, cantidad, precio_unitario, producto_nombre, producto_url]
    fun obtenerDetallesPedido(idPedido: Int): List<Array<Any>> {
        return DetallePedidoDao.listarPorPedido(idPedido)
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
        return PedidoDao.calcularVentasDia()
    }

    fun obtenerTotalPedidosHoy(): Int {
        return PedidoDao.contarPedidosHoy()
    }

    fun validarStockDisponible(idProducto: Int, cantidadSolicitada: Int): Result<Boolean> {
        return try {
            val productoArray = ProductoDao.obtenerPorId(idProducto)
            if (productoArray == null) {
                return Result.failure(Exception("Producto no encontrado"))
            }

            val stock = productoArray[5] as Int
            if (stock < cantidadSolicitada) {
                return Result.failure(Exception("Stock insuficiente. Disponible: $stock"))
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Utilidades primitivas
    fun calcularTotalDetalles(detalles: List<Array<Any>>): Double {
        return detalles.sumOf { detalle ->
            val cantidad = detalle[1] as Int
            val precioUnitario = detalle[2] as Double
            cantidad * precioUnitario
        }
    }

    fun calcularSubtotal(cantidad: Int, precioUnitario: Double): Double {
        return cantidad * precioUnitario
    }

    fun formatearTotal(total: Double): String {
        return String.format("%.2f", total)
    }

    fun formatearPrecio(precio: Double): String {
        return String.format("%.2f", precio)
    }

    fun cantidadTotalProductos(detalles: List<Array<Any>>): Int {
        return detalles.sumOf { detalle ->
            detalle[1] as Int // cantidad
        }
    }

    // Validaciones primitivas
    fun validarNombreCliente(nombre: String): Boolean {
        return nombre.isNotBlank() && nombre.length <= 100
    }

    fun validarCantidad(cantidad: String): Boolean {
        return try {
            val cant = cantidad.toInt()
            cant > 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun validarDetalles(detalles: List<Array<Any>>): Boolean {
        if (detalles.isEmpty()) return false

        return detalles.all { detalle ->
            try {
                val idProducto = detalle[0] as Int
                val cantidad = detalle[1] as Int
                val precioUnitario = detalle[2] as Double

                idProducto > 0 && cantidad > 0 && precioUnitario > 0.0
            } catch (e: Exception) {
                false
            }
        }
    }
}