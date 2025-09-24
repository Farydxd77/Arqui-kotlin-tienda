package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion
import com.example.arquiprimerparcial.data.entidad.PedidoEntidad
import java.sql.Timestamp

object PedidoDao {

    fun listar(): List<PedidoEntidad> {
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

    fun listarPorFecha(fechaInicio: Timestamp, fechaFin: Timestamp): List<PedidoEntidad> {
        val lista = mutableListOf<PedidoEntidad>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT id, nombre_cliente, fecha_pedido, total 
                FROM pedido 
                WHERE fecha_pedido BETWEEN ? AND ?
                ORDER BY fecha_pedido DESC
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setTimestamp(1, fechaInicio)
                ps.setTimestamp(2, fechaFin)
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

    fun insertar(pedido: PedidoEntidad): Int {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    INSERT INTO pedido (nombre_cliente, fecha_pedido, total) 
                    VALUES (?, ?, ?) 
                    RETURNING id
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, pedido.nombre_cliente)
                    ps.setTimestamp(2, pedido.fecha_pedido ?: Timestamp(System.currentTimeMillis()))
                    ps.setDouble(3, pedido.total)

                    ps.executeQuery().use { rs ->
                        if (rs.next()) rs.getInt("id") else 0
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    fun actualizar(pedido: PedidoEntidad): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    UPDATE pedido 
                    SET nombre_cliente = ?, total = ?
                    WHERE id = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, pedido.nombre_cliente)
                    ps.setDouble(2, pedido.total)
                    ps.setInt(3, pedido.id)
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
                val sql = "DELETE FROM pedido WHERE id = ?"
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

    fun obtenerPorId(id: Int): PedidoEntidad? {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = "SELECT id, nombre_cliente, fecha_pedido, total FROM pedido WHERE id = ?"
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, id)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            PedidoEntidad(
                                id = rs.getInt("id"),
                                nombre_cliente = rs.getString("nombre_cliente"),
                                fecha_pedido = rs.getTimestamp("fecha_pedido"),
                                total = rs.getDouble("total")
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

    fun obtenerTotalVentasPorFecha(fecha: Timestamp): Double {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    SELECT COALESCE(SUM(total), 0) as total_ventas
                    FROM pedido 
                    WHERE DATE(fecha_pedido) = DATE(?)
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setTimestamp(1, fecha)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) rs.getDouble("total_ventas") else 0.0
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }
}