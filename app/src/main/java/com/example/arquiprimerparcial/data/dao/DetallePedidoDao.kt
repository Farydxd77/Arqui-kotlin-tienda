package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion

object DetallePedidoDao {

    // Retorna lista de arrays: [id_pedido, id_producto, cantidad, precio_unitario, producto_nombre, producto_url]
    fun listarPorPedido(idPedido: Int): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT dp.id_pedido, dp.id_producto, dp.cantidad, dp.precio_unitario,
                       p.nombre as producto_nombre, p.url as producto_url
                FROM detalle_pedido dp
                JOIN producto p ON dp.id_producto = p.id
                WHERE dp.id_pedido = ?
                ORDER BY p.nombre
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setInt(1, idPedido)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            arrayOf(
                                rs.getInt("id_pedido"),
                                rs.getInt("id_producto"),
                                rs.getInt("cantidad"),
                                rs.getDouble("precio_unitario"),
                                rs.getString("producto_nombre"),
                                rs.getString("producto_url") ?: ""
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    // Parámetros primitivos: idPedido, idProducto, cantidad, precioUnitario
    fun insertar(idPedido: Int, idProducto: Int, cantidad: Int, precioUnitario: Double): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) 
                    VALUES (?, ?, ?, ?)
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, idPedido)
                    ps.setInt(2, idProducto)
                    ps.setInt(3, cantidad)
                    ps.setDouble(4, precioUnitario)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun actualizar(idPedido: Int, idProducto: Int, cantidad: Int, precioUnitario: Double): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    UPDATE detalle_pedido 
                    SET cantidad = ?, precio_unitario = ?
                    WHERE id_pedido = ? AND id_producto = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, cantidad)
                    ps.setDouble(2, precioUnitario)
                    ps.setInt(3, idPedido)
                    ps.setInt(4, idProducto)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun eliminar(idPedido: Int, idProducto: Int): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    DELETE FROM detalle_pedido 
                    WHERE id_pedido = ? AND id_producto = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, idPedido)
                    ps.setInt(2, idProducto)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun eliminarPorPedido(idPedido: Int): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = "DELETE FROM detalle_pedido WHERE id_pedido = ?"
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, idPedido)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Retorna array [id_pedido, id_producto, cantidad, precio_unitario, producto_nombre, producto_url] o null
    fun obtenerPorPedidoYProducto(idPedido: Int, idProducto: Int): Array<Any>? {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    SELECT dp.id_pedido, dp.id_producto, dp.cantidad, dp.precio_unitario,
                           p.nombre as producto_nombre, p.url as producto_url
                    FROM detalle_pedido dp
                    JOIN producto p ON dp.id_producto = p.id
                    WHERE dp.id_pedido = ? AND dp.id_producto = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, idPedido)
                    ps.setInt(2, idProducto)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            arrayOf(
                                rs.getInt("id_pedido"),
                                rs.getInt("id_producto"),
                                rs.getInt("cantidad"),
                                rs.getDouble("precio_unitario"),
                                rs.getString("producto_nombre"),
                                rs.getString("producto_url") ?: ""
                            )
                        } else null
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Para insertar múltiples detalles de una vez
    // Cada detalle debe ser array de [idPedido, idProducto, cantidad, precioUnitario]
    fun insertarLote(detalles: List<Array<Any>>): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                conexion.autoCommit = false
                val sql = """
                    INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) 
                    VALUES (?, ?, ?, ?)
                """
                conexion.prepareStatement(sql).use { ps ->
                    for (detalle in detalles) {
                        ps.setInt(1, detalle[0] as Int)       // idPedido
                        ps.setInt(2, detalle[1] as Int)       // idProducto
                        ps.setInt(3, detalle[2] as Int)       // cantidad
                        ps.setDouble(4, detalle[3] as Double) // precioUnitario
                        ps.addBatch()
                    }
                    val results = ps.executeBatch()
                    conexion.commit()
                    results.all { it > 0 }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Retorna lista de arrays [id_producto, cantidad_total_vendida]
    fun obtenerProductosMasVendidos(limite: Int = 10): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT id_producto, SUM(cantidad) as total_vendido
                FROM detalle_pedido
                GROUP BY id_producto
                ORDER BY total_vendido DESC
                LIMIT ?
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setInt(1, limite)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            arrayOf(
                                rs.getInt("id_producto"),
                                rs.getInt("total_vendido")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }
}