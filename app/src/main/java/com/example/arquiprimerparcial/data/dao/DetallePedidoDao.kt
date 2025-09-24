package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion
import com.example.arquiprimerparcial.data.entidad.DetallePedidoEntidad

object DetallePedidoDao {

    fun listarPorPedido(idPedido: Int): List<DetallePedidoEntidad> {
        val lista = mutableListOf<DetallePedidoEntidad>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT dp.id, dp.id_pedido, dp.id_producto, dp.cantidad, dp.precio_unitario,
                       p.nombre as producto_nombre
                FROM detalle_pedido dp
                JOIN producto p ON dp.id_producto = p.id
                WHERE dp.id_pedido = ?
                ORDER BY dp.id
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setInt(1, idPedido)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            DetallePedidoEntidad(
                                id = rs.getInt("id"),
                                id_pedido = rs.getInt("id_pedido"),
                                id_producto = rs.getInt("id_producto"),
                                cantidad = rs.getInt("cantidad"),
                                precio_unitario = rs.getDouble("precio_unitario")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    fun insertar(detalle: DetallePedidoEntidad): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) 
                    VALUES (?, ?, ?, ?)
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, detalle.id_pedido)
                    ps.setInt(2, detalle.id_producto)
                    ps.setInt(3, detalle.cantidad)
                    ps.setDouble(4, detalle.precio_unitario)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun actualizar(detalle: DetallePedidoEntidad): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    UPDATE detalle_pedido 
                    SET cantidad = ?, precio_unitario = ?
                    WHERE id = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, detalle.cantidad)
                    ps.setDouble(2, detalle.precio_unitario)
                    ps.setInt(3, detalle.id)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun eliminar(id: Int): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = "DELETE FROM detalle_pedido WHERE id = ?"
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, id)
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

    fun obtenerPorId(id: Int): DetallePedidoEntidad? {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    SELECT id, id_pedido, id_producto, cantidad, precio_unitario 
                    FROM detalle_pedido 
                    WHERE id = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, id)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            DetallePedidoEntidad(
                                id = rs.getInt("id"),
                                id_pedido = rs.getInt("id_pedido"),
                                id_producto = rs.getInt("id_producto"),
                                cantidad = rs.getInt("cantidad"),
                                precio_unitario = rs.getDouble("precio_unitario")
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

    fun insertarLote(detalles: List<DetallePedidoEntidad>): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                conexion.autoCommit = false
                val sql = """
                    INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) 
                    VALUES (?, ?, ?, ?)
                """
                conexion.prepareStatement(sql).use { ps ->
                    for (detalle in detalles) {
                        ps.setInt(1, detalle.id_pedido)
                        ps.setInt(2, detalle.id_producto)
                        ps.setInt(3, detalle.cantidad)
                        ps.setDouble(4, detalle.precio_unitario)
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

    fun obtenerProductosMasVendidos(limite: Int = 10): List<Pair<Int, Int>> {
        val lista = mutableListOf<Pair<Int, Int>>() // Pair<id_producto, cantidad_total>

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
                            Pair(
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