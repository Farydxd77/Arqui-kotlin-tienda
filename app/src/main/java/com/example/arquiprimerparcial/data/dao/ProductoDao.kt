package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion
import com.example.arquiprimerparcial.data.entidad.ProductoEntidad

object ProductoDao {

    fun listarProducto(filtro: String): List<ProductoEntidad> {
        val lista = mutableListOf<ProductoEntidad>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT p.id, p.nombre, p.descripcion, p.url, p.precio, p.stock, 
                       p.id_categoria, p.activo, c.nombre as categoria_nombre
                FROM producto p
                LEFT JOIN categoria c ON p.id_categoria = c.id
                WHERE p.activo = true AND LOWER(p.nombre) LIKE '%' || LOWER(?) || '%'
                ORDER BY p.nombre
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setString(1, filtro)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            ProductoEntidad(
                                id = rs.getInt("id"),
                                nombre = rs.getString("nombre"),
                                descripcion = rs.getString("descripcion") ?: "",
                                url = rs.getString("url") ?: "",
                                precio = rs.getDouble("precio"),
                                stock = rs.getInt("stock"),
                                id_categoria = rs.getInt("id_categoria"),
                                activo = rs.getBoolean("activo")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    fun listarPorCategoria(idCategoria: Int): List<ProductoEntidad> {
        val lista = mutableListOf<ProductoEntidad>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT id, nombre, descripcion, url, precio, stock, 
                       id_categoria, activo
                FROM producto 
                WHERE activo = true AND id_categoria = ?
                ORDER BY nombre
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setInt(1, idCategoria)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            ProductoEntidad(
                                id = rs.getInt("id"),
                                nombre = rs.getString("nombre"),
                                descripcion = rs.getString("descripcion") ?: "",
                                url = rs.getString("url") ?: "",
                                precio = rs.getDouble("precio"),
                                stock = rs.getInt("stock"),
                                id_categoria = rs.getInt("id_categoria"),
                                activo = rs.getBoolean("activo")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    fun crearProducto(producto: ProductoEntidad): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    INSERT INTO producto (nombre, descripcion, url, precio, stock, id_categoria, activo) 
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, producto.nombre)
                    ps.setString(2, producto.descripcion.ifEmpty { null })
                    ps.setString(3, producto.url.ifEmpty { null })
                    ps.setDouble(4, producto.precio)
                    ps.setInt(5, producto.stock)
                    if (producto.id_categoria == 0) {
                        ps.setNull(6, java.sql.Types.INTEGER)
                    } else {
                        ps.setInt(6, producto.id_categoria)
                    }
                    ps.setBoolean(7, producto.activo)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun actualizarProducto(producto: ProductoEntidad): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    UPDATE producto 
                    SET nombre = ?, descripcion = ?, url = ?, precio = ?, 
                        stock = ?, id_categoria = ?, activo = ?
                    WHERE id = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, producto.nombre)
                    ps.setString(2, producto.descripcion.ifEmpty { null })
                    ps.setString(3, producto.url.ifEmpty { null })
                    ps.setDouble(4, producto.precio)
                    ps.setInt(5, producto.stock)
                    if (producto.id_categoria == 0) {
                        ps.setNull(6, java.sql.Types.INTEGER)
                    } else {
                        ps.setInt(6, producto.id_categoria)
                    }
                    ps.setBoolean(7, producto.activo)
                    ps.setInt(8, producto.id)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun desactivarProducto(id: Int): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                // Eliminación lógica: marcar como inactivo
                val sql = "UPDATE producto SET activo = false WHERE id = ?"
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

    fun eliminarFisico(id: Int): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = "DELETE FROM producto WHERE id = ?"
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

    fun obtenerPorId(id: Int): ProductoEntidad? {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    SELECT id, nombre, descripcion, url, precio, stock, 
                           id_categoria, activo
                    FROM producto 
                    WHERE id = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, id)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            ProductoEntidad(
                                id = rs.getInt("id"),
                                nombre = rs.getString("nombre"),
                                descripcion = rs.getString("descripcion") ?: "",
                                url = rs.getString("url") ?: "",
                                precio = rs.getDouble("precio"),
                                stock = rs.getInt("stock"),
                                id_categoria = rs.getInt("id_categoria"),
                                activo = rs.getBoolean("activo")
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

    fun actualizarStock(id: Int, nuevoStock: Int): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = "UPDATE producto SET stock = ? WHERE id = ?"
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, nuevoStock)
                    ps.setInt(2, id)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun listarStockBajo(limite: Int = 5): List<ProductoEntidad> {
        val lista = mutableListOf<ProductoEntidad>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT id, nombre, descripcion, url, precio, stock, 
                       id_categoria, activo
                FROM producto 
                WHERE activo = true AND stock <= ?
                ORDER BY stock ASC
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setInt(1, limite)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            ProductoEntidad(
                                id = rs.getInt("id"),
                                nombre = rs.getString("nombre"),
                                descripcion = rs.getString("descripcion") ?: "",
                                url = rs.getString("url") ?: "",
                                precio = rs.getDouble("precio"),
                                stock = rs.getInt("stock"),
                                id_categoria = rs.getInt("id_categoria"),
                                activo = rs.getBoolean("activo")
                            )
                        )
                    }
                }
            }
        }
        return lista
    }
}