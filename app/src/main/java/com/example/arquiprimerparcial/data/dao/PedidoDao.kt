package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion
import java.sql.Timestamp

class PedidoDao {

    fun listar(): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

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
                            arrayOf(
                                rs.getInt("id"),
                                rs.getString("nombre_cliente"),
                                rs.getTimestamp("fecha_pedido"),
                                rs.getDouble("total")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    fun listarPorFecha(fechaInicio: Timestamp, fechaFin: Timestamp): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

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
                            arrayOf(
                                rs.getInt("id"),
                                rs.getString("nombre_cliente"),
                                rs.getTimestamp("fecha_pedido"),
                                rs.getDouble("total")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    fun insertar(nombreCliente: String, fechaPedido: Timestamp?, total: Double): Int {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    INSERT INTO pedido (nombre_cliente, fecha_pedido, total) 
                    VALUES (?, ?, ?) 
                    RETURNING id
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, nombreCliente)
                    ps.setTimestamp(2, fechaPedido ?: Timestamp(System.currentTimeMillis()))
                    ps.setDouble(3, total)

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

    fun actualizar(id: Int, nombreCliente: String, total: Double): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    UPDATE pedido 
                    SET nombre_cliente = ?, total = ?
                    WHERE id = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, nombreCliente)
                    ps.setDouble(2, total)
                    ps.setInt(3, id)
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

    fun obtenerPorId(id: Int): Array<Any>? {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    SELECT id, nombre_cliente, fecha_pedido, total 
                    FROM pedido 
                    WHERE id = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, id)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            arrayOf(
                                rs.getInt("id"),
                                rs.getString("nombre_cliente"),
                                rs.getTimestamp("fecha_pedido"),
                                rs.getDouble("total")
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
            e.printStackTrace()
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
            e.printStackTrace()
            0
        }
    }
}