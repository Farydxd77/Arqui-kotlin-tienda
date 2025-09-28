package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion
import com.example.arquiprimerparcial.data.entidad.PedidoEntidad
import com.example.arquiprimerparcial.negocio.modelo.DetallePedidoModelo

object HistorialPedidosDao {

    fun listarTodos(): List<PedidoEntidad> {
        val lista = mutableListOf<PedidoEntidad>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT id, nombre_cliente, fecha_pedido, total 
                FROM pedido 
                ORDER BY fecha_pedido DESC
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            PedidoEntidad(
                                id = rs.getInt("id"),
                                nombre_cliente = rs.getString("nombre_cliente"),
                                fecha_pedido = rs.getTimestamp("fecha_pedido"),
                                total = rs.getDouble("total")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    fun obtenerDetallesPedido(idPedido: Int): List<DetallePedidoModelo> {
        val lista = mutableListOf<DetallePedidoModelo>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT dp.id, dp.cantidad, dp.precio_unitario, p.nombre
                FROM detalle_pedido dp
                JOIN producto p ON dp.id_producto = p.id
                WHERE dp.id_pedido = ?
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setInt(1, idPedido)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            DetallePedidoModelo(
                                id = rs.getInt("id"),
                                idPedido = idPedido,
                                nombreProducto = rs.getString("nombre"),
                                cantidad = rs.getInt("cantidad"),
                                precioUnitario = rs.getDouble("precio_unitario")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    fun calcularVentasDia(): Double {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    SELECT COALESCE(SUM(total), 0) as total_ventas
                    FROM pedido 
                    WHERE DATE(fecha_pedido) = CURRENT_DATE
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.executeQuery().use { rs ->
                        if (rs.next()) rs.getDouble("total_ventas") else 0.0
                    }
                }
            }
        } catch (e: Exception) {
            0.0
        }
    }

    fun contarPedidosHoy(): Int {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    SELECT COUNT(*) as total
                    FROM pedido 
                    WHERE DATE(fecha_pedido) = CURRENT_DATE
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.executeQuery().use { rs ->
                        if (rs.next()) rs.getInt("total") else 0
                    }
                }
            }
        } catch (e: Exception) {
            0
        }
    }

    fun eliminarPedido(id: Int): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = "DELETE FROM pedido WHERE id = ?"
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, id)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            false
        }
    }
}