package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion

class HistorialPedidosDao {

    fun listarTodos(): List<Array<Any>> {
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

    fun obtenerDetallesPedido(idPedido: Int): List<Array<Any>> {
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
            e.printStackTrace()
            false
        }
    }

    fun calcularVentasPorPeriodo(fechaInicio: String, fechaFin: String): Double {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    SELECT COALESCE(SUM(total), 0) as total_ventas
                    FROM pedido 
                    WHERE DATE(fecha_pedido) BETWEEN ? AND ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, fechaInicio)
                    ps.setString(2, fechaFin)
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

    fun obtenerEstadisticasCompletas(): Map<String, Any> {
        val estadisticas = mutableMapOf<String, Any>()

        PostgresqlConexion.getConexion().use { conexion ->
            var sql = "SELECT COUNT(*) as total FROM pedido"
            conexion.prepareStatement(sql).use { ps ->
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        estadisticas["total_pedidos"] = rs.getInt("total")
                    }
                }
            }

            sql = "SELECT COALESCE(SUM(total), 0) as total_ventas FROM pedido"
            conexion.prepareStatement(sql).use { ps ->
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        estadisticas["total_ventas"] = rs.getDouble("total_ventas")
                    }
                }
            }

            sql = "SELECT COALESCE(AVG(total), 0) as promedio FROM pedido"
            conexion.prepareStatement(sql).use { ps ->
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        estadisticas["promedio_por_pedido"] = rs.getDouble("promedio")
                    }
                }
            }
        }

        return estadisticas
    }

    fun listarPorRangoFechas(fechaInicio: String, fechaFin: String): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT id, nombre_cliente, fecha_pedido, total 
                FROM pedido 
                WHERE DATE(fecha_pedido) BETWEEN ? AND ?
                ORDER BY fecha_pedido DESC
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setString(1, fechaInicio)
                ps.setString(2, fechaFin)
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
}