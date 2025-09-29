package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion
import java.sql.Date

class ReporteDao {

    // Ventas por rango de fechas
    fun obtenerVentasPorPeriodo(fechaInicio: String, fechaFin: String): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT 
                    DATE(fecha_pedido) as fecha,
                    COUNT(*) as total_pedidos,
                    SUM(total) as total_ventas
                FROM pedido
                WHERE DATE(fecha_pedido) BETWEEN ?::date AND ?::date
                GROUP BY DATE(fecha_pedido)
                ORDER BY fecha DESC
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setString(1, fechaInicio)
                ps.setString(2, fechaFin)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            arrayOf(
                                rs.getDate("fecha").toString(),
                                rs.getInt("total_pedidos"),
                                rs.getDouble("total_ventas")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    // Productos más vendidos
    fun obtenerProductosMasVendidos(fechaInicio: String, fechaFin: String, limite: Int = 10): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT 
                    p.nombre,
                    COALESCE(c.nombre, 'Sin categoría') as categoria,
                    SUM(dp.cantidad) as total_vendido,
                    SUM(dp.cantidad * dp.precio_unitario) as total_ingresos
                FROM detalle_pedido dp
                JOIN producto p ON dp.id_producto = p.id
                JOIN pedido ped ON dp.id_pedido = ped.id
                LEFT JOIN categoria c ON p.id_categoria = c.id
                WHERE DATE(ped.fecha_pedido) BETWEEN ?::date AND ?::date
                GROUP BY p.nombre, c.nombre
                ORDER BY total_vendido DESC
                LIMIT ?
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setString(1, fechaInicio)
                ps.setString(2, fechaFin)
                ps.setInt(3, limite)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            arrayOf(
                                rs.getString("nombre"),
                                rs.getString("categoria"),
                                rs.getInt("total_vendido"),
                                rs.getDouble("total_ingresos")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    // Productos con bajo stock
    fun obtenerProductosBajoStock(limite: Int = 5): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT 
                    p.nombre,
                    COALESCE(c.nombre, 'Sin categoría') as categoria,
                    p.stock,
                    p.precio
                FROM producto p
                LEFT JOIN categoria c ON p.id_categoria = c.id
                WHERE p.activo = true AND p.stock <= ?
                ORDER BY p.stock ASC
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setInt(1, limite)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            arrayOf(
                                rs.getString("nombre"),
                                rs.getString("categoria"),
                                rs.getInt("stock"),
                                rs.getDouble("precio")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    // Resumen general de ventas
    fun obtenerResumenGeneral(fechaInicio: String, fechaFin: String): Map<String, Any> {
        val resumen = mutableMapOf<String, Any>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT 
                    COUNT(*) as total_pedidos,
                    COALESCE(SUM(total), 0) as total_ventas,
                    COALESCE(AVG(total), 0) as promedio_venta,
                    COALESCE(MAX(total), 0) as venta_maxima,
                    COALESCE(MIN(total), 0) as venta_minima
                FROM pedido
                WHERE DATE(fecha_pedido) BETWEEN ?::date AND ?::date
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setString(1, fechaInicio)
                ps.setString(2, fechaFin)
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        resumen["totalPedidos"] = rs.getInt("total_pedidos")
                        resumen["totalVentas"] = rs.getDouble("total_ventas")
                        resumen["promedioVenta"] = rs.getDouble("promedio_venta")
                        resumen["ventaMaxima"] = rs.getDouble("venta_maxima")
                        resumen["ventaMinima"] = rs.getDouble("venta_minima")
                    }
                }
            }
        }
        return resumen
    }

    // Ventas por categoría
    fun obtenerVentasPorCategoria(fechaInicio: String, fechaFin: String): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT 
                    COALESCE(c.nombre, 'Sin categoría') as categoria,
                    COUNT(DISTINCT ped.id) as total_pedidos,
                    SUM(dp.cantidad) as productos_vendidos,
                    SUM(dp.cantidad * dp.precio_unitario) as total_ventas
                FROM detalle_pedido dp
                JOIN pedido ped ON dp.id_pedido = ped.id
                JOIN producto p ON dp.id_producto = p.id
                LEFT JOIN categoria c ON p.id_categoria = c.id
                WHERE DATE(ped.fecha_pedido) BETWEEN ?::date AND ?::date
                GROUP BY c.nombre
                ORDER BY total_ventas DESC
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setString(1, fechaInicio)
                ps.setString(2, fechaFin)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            arrayOf(
                                rs.getString("categoria"),
                                rs.getInt("total_pedidos"),
                                rs.getInt("productos_vendidos"),
                                rs.getDouble("total_ventas")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    // Clientes frecuentes
    fun obtenerClientesFrecuentes(fechaInicio: String, fechaFin: String, limite: Int = 10): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT 
                    nombre_cliente,
                    COUNT(*) as total_pedidos,
                    SUM(total) as total_gastado,
                    AVG(total) as promedio_gasto
                FROM pedido
                WHERE DATE(fecha_pedido) BETWEEN ?::date AND ?::date
                GROUP BY nombre_cliente
                ORDER BY total_pedidos DESC
                LIMIT ?
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setString(1, fechaInicio)
                ps.setString(2, fechaFin)
                ps.setInt(3, limite)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            arrayOf(
                                rs.getString("nombre_cliente"),
                                rs.getInt("total_pedidos"),
                                rs.getDouble("total_gastado"),
                                rs.getDouble("promedio_gasto")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }
}