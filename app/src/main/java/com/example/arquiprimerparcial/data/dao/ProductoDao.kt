package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion

object ProductoDao {

    // Retorna lista de arrays: [id, nombre, descripcion, url, precio, stock, id_categoria, activo, categoria_nombre]
    fun listarProducto(filtro: String): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

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
                            arrayOf(
                                rs.getInt("id"),
                                rs.getString("nombre"),
                                rs.getString("descripcion") ?: "",
                                rs.getString("url") ?: "",
                                rs.getDouble("precio"),
                                rs.getInt("stock"),
                                rs.getInt("id_categoria"),
                                rs.getBoolean("activo"),
                                rs.getString("categoria_nombre") ?: ""
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    fun listarPorCategoria(idCategoria: Int): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT p.id, p.nombre, p.descripcion, p.url, p.precio, p.stock, 
                       p.id_categoria, p.activo, c.nombre as categoria_nombre
                FROM producto p
                LEFT JOIN categoria c ON p.id_categoria = c.id
                WHERE p.activo = true AND p.id_categoria = ?
                ORDER BY p.nombre
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setInt(1, idCategoria)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            arrayOf(
                                rs.getInt("id"),
                                rs.getString("nombre"),
                                rs.getString("descripcion") ?: "",
                                rs.getString("url") ?: "",
                                rs.getDouble("precio"),
                                rs.getInt("stock"),
                                rs.getInt("id_categoria"),
                                rs.getBoolean("activo"),
                                rs.getString("categoria_nombre") ?: ""
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    // Parámetros primitivos: nombre, descripcion, url, precio, stock, idCategoria, activo
    fun crearProducto(nombre: String, descripcion: String, url: String,
                      precio: Double, stock: Int, idCategoria: Int, activo: Boolean): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    INSERT INTO producto (nombre, descripcion, url, precio, stock, id_categoria, activo) 
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, nombre)
                    ps.setString(2, descripcion.ifEmpty { null })
                    ps.setString(3, url.ifEmpty { null })
                    ps.setDouble(4, precio)
                    ps.setInt(5, stock)
                    if (idCategoria == 0) {
                        ps.setNull(6, java.sql.Types.INTEGER)
                    } else {
                        ps.setInt(6, idCategoria)
                    }
                    ps.setBoolean(7, activo)
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun actualizarProducto(id: Int, nombre: String, descripcion: String, url: String,
                           precio: Double, stock: Int, idCategoria: Int, activo: Boolean): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    UPDATE producto 
                    SET nombre = ?, descripcion = ?, url = ?, precio = ?, 
                        stock = ?, id_categoria = ?, activo = ?
                    WHERE id = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, nombre)
                    ps.setString(2, descripcion.ifEmpty { null })
                    ps.setString(3, url.ifEmpty { null })
                    ps.setDouble(4, precio)
                    ps.setInt(5, stock)
                    if (idCategoria == 0) {
                        ps.setNull(6, java.sql.Types.INTEGER)
                    } else {
                        ps.setInt(6, idCategoria)
                    }
                    ps.setBoolean(7, activo)
                    ps.setInt(8, id)
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
                val sql = """
                    UPDATE producto 
                    SET activo = false 
                    WHERE id = ?
                """
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

    // Retorna array [id, nombre, descripcion, url, precio, stock, id_categoria, activo, categoria_nombre] o null
    fun obtenerPorId(id: Int): Array<Any>? {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    SELECT p.id, p.nombre, p.descripcion, p.url, p.precio, p.stock, 
                           p.id_categoria, p.activo, c.nombre as categoria_nombre
                    FROM producto p
                    LEFT JOIN categoria c ON p.id_categoria = c.id
                    WHERE p.id = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, id)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            arrayOf(
                                rs.getInt("id"),
                                rs.getString("nombre"),
                                rs.getString("descripcion") ?: "",
                                rs.getString("url") ?: "",
                                rs.getDouble("precio"),
                                rs.getInt("stock"),
                                rs.getInt("id_categoria"),
                                rs.getBoolean("activo"),
                                rs.getString("categoria_nombre") ?: ""
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
                val sql = """
                    UPDATE producto 
                    SET stock = ? 
                    WHERE id = ?
                """
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

    fun listarStockBajo(limite: Int = 5): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT p.id, p.nombre, p.descripcion, p.url, p.precio, p.stock, 
                       p.id_categoria, p.activo, c.nombre as categoria_nombre
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
                                rs.getInt("id"),
                                rs.getString("nombre"),
                                rs.getString("descripcion") ?: "",
                                rs.getString("url") ?: "",
                                rs.getDouble("precio"),
                                rs.getInt("stock"),
                                rs.getInt("id_categoria"),
                                rs.getBoolean("activo"),
                                rs.getString("categoria_nombre") ?: ""
                            )
                        )
                    }
                }
            }
        }
        return lista
    }
}