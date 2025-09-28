package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion

object CategoriaDao {

    // Retorna lista de arrays: [id, nombre, descripcion]
    fun listar(): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT id, nombre, descripcion 
                FROM categoria 
                ORDER BY nombre
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            arrayOf(
                                rs.getInt("id"),
                                rs.getString("nombre"),
                                rs.getString("descripcion") ?: ""
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    fun listarConFiltro(filtro: String): List<Array<Any>> {
        val lista = mutableListOf<Array<Any>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT id, nombre, descripcion 
                FROM categoria 
                WHERE LOWER(nombre) LIKE '%' || LOWER(?) || '%'
                ORDER BY nombre
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.setString(1, filtro)
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            arrayOf(
                                rs.getInt("id"),
                                rs.getString("nombre"),
                                rs.getString("descripcion") ?: ""
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    // Parámetros primitivos: nombre, descripcion
    fun insertar(nombre: String, descripcion: String): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    INSERT INTO categoria (nombre, descripcion) 
                    VALUES (?, ?)
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, nombre)
                    ps.setString(2, descripcion.ifEmpty { null })
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun actualizar(id: Int, nombre: String, descripcion: String): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    UPDATE categoria 
                    SET nombre = ?, descripcion = ? 
                    WHERE id = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, nombre)
                    ps.setString(2, descripcion.ifEmpty { null })
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
                // Verificar productos asociados
                val sqlVerificar = """
                    SELECT COUNT(*) as total 
                    FROM producto 
                    WHERE id_categoria = ?
                """
                conexion.prepareStatement(sqlVerificar).use { ps ->
                    ps.setInt(1, id)
                    ps.executeQuery().use { rs ->
                        if (rs.next() && rs.getInt("total") > 0) {
                            return false
                        }
                    }
                }

                // Eliminar categoría
                val sql = "DELETE FROM categoria WHERE id = ?"
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

    // Retorna array [id, nombre, descripcion] o null
    fun obtenerPorId(id: Int): Array<Any>? {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    SELECT id, nombre, descripcion 
                    FROM categoria 
                    WHERE id = ?
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, id)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            arrayOf(
                                rs.getInt("id"),
                                rs.getString("nombre"),
                                rs.getString("descripcion") ?: ""
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

    fun existeNombre(nombre: String, idExcluir: Int = 0): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = if (idExcluir > 0) {
                    """
                        SELECT COUNT(*) as total 
                        FROM categoria 
                        WHERE LOWER(nombre) = LOWER(?) AND id != ?
                    """
                } else {
                    """
                        SELECT COUNT(*) as total 
                        FROM categoria 
                        WHERE LOWER(nombre) = LOWER(?)
                    """
                }

                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, nombre)
                    if (idExcluir > 0) {
                        ps.setInt(2, idExcluir)
                    }
                    ps.executeQuery().use { rs ->
                        rs.next() && rs.getInt("total") > 0
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }

    fun contarProductos(idCategoria: Int): Int {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = """
                    SELECT COUNT(*) as total 
                    FROM producto 
                    WHERE id_categoria = ? AND activo = true
                """
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, idCategoria)
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