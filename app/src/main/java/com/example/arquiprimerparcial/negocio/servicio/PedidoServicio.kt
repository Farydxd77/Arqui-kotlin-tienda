package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.DetallePedidoDao
import com.example.arquiprimerparcial.data.dao.PedidoDao
import com.example.arquiprimerparcial.data.dao.ProductoDao
import java.sql.Timestamp

object PedidoServicio {

    // ✅ CAPA DE NEGOCIO - Solo llama a DAOs (capa de datos)
    // ✅ RESPONSABILIDAD: Lógica de negocio + validaciones + reglas + orquestación de operaciones
    // ❌ NUNCA accede directamente a Base de Datos

    fun crearPedidoPrimitivo(pedidoData: Map<String, Any>): Result<Int> {
        return try {
            // ✅ TRANSFORMACIÓN DE DATOS (de primitivos a estructura interna)
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
            // ✅ LÓGICA DE NEGOCIO ESTRICTA (validaciones y reglas de dominio)

            // Validación 1: Cliente requerido
            if (nombreCliente.isBlank()) {
                return Result.failure(Exception("El nombre del cliente es requerido"))
            }

            // Validación 2: Longitud del nombre del cliente
            if (nombreCliente.length > 100) {
                return Result.failure(Exception("El nombre del cliente no puede exceder 100 caracteres"))
            }

            // Validación 3: Pedido debe tener productos
            if (detalles.isEmpty()) {
                return Result.failure(Exception("El pedido debe tener al menos un producto"))
            }

            // ✅ REGLA DE NEGOCIO: Verificar disponibilidad de stock ANTES de crear el pedido
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

            // ✅ LÓGICA DE NEGOCIO: Calcular total del pedido
            val total = calcularTotalDetalles(detalles)

            // ✅ OPERACIÓN TRANSACCIONAL (orquestación de múltiples DAOs)

            // Paso 1: Crear el pedido principal
            val idPedido = PedidoDao.insertar(
                nombreCliente = nombreCliente.trim(),
                fechaPedido = Timestamp(System.currentTimeMillis()),
                total = total
            )

            if (idPedido <= 0) {
                return Result.failure(Exception("Error al crear el pedido"))
            }

            // Paso 2: Crear los detalles del pedido
            val detallesParaInsertar = detalles.map { detalle ->
                arrayOf<Any>(
                    idPedido,
                    detalle[0] as Int,
                    detalle[1] as Int,
                    detalle[2] as Double
                )
            }

            val resultadoDetalles = DetallePedidoDao.insertarLote(detallesParaInsertar)
            if (!resultadoDetalles) {
                return Result.failure(Exception("Error al guardar los detalles del pedido"))
            }

            // Paso 3: Actualizar stock de productos (regla de negocio)
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

    fun obtenerPedidosPrimitivos(): List<Map<String, Any>> {
        return try {
            // ✅ TRANSFORMACIÓN COMPLEJA (orquestación de múltiples DAOs + lógica de agregación)
            val pedidosArray = PedidoDao.listar()

            val resultado = mutableListOf<Map<String, Any>>()

            for (pedido in pedidosArray) {
                val id = pedido[0] as Int
                val nombreCliente = pedido[1] as String
                val fechaPedido = pedido[2] as java.sql.Timestamp
                val total = pedido[3] as Double

                val detallesArray = DetallePedidoDao.listarPorPedido(id)
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
                    "fecha" to fechaPedido,
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

    fun obtenerPedidos(): List<Array<Any>> {
        return PedidoDao.listar()
    }

    fun obtenerPedidoPorId(id: Int): Array<Any>? {
        return PedidoDao.obtenerPorId(id)
    }

    fun obtenerDetallesPedido(idPedido: Int): List<Array<Any>> {
        return DetallePedidoDao.listarPorPedido(idPedido)
    }

    fun eliminarPedido(id: Int): Result<Boolean> {
        return try {
            // ✅ VALIDACIÓN DE NEGOCIO
            if (id <= 0) {
                return Result.failure(Exception("ID de pedido inválido"))
            }

            // Operación de datos - Delegar a DAO
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
            // ✅ REGLA DE NEGOCIO: Verificar disponibilidad
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

    // ✅ LÓGICA DE NEGOCIO PURA (cálculos de dominio)
    fun calcularTotalDetalles(detalles: List<Array<Any>>): Double {
        var total = 0.0
        for (detalle in detalles) {
            val cantidad = detalle[1] as Int
            val precioUnitario = detalle[2] as Double
            total += cantidad * precioUnitario
        }
        return total
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
        var total = 0
        for (detalle in detalles) {
            total += detalle[1] as Int
        }
        return total
    }

    // ✅ VALIDACIONES DE DOMINIO
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