package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion
import com.example.arquiprimerparcial.data.entidad.CategoriaEntidad

object CategoriaDao {

    fun listar(): List<CategoriaEntidad> {
        val lista = mutableListOf<CategoriaEntidad>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = "SELECT id, nombre, descripcion FROM categoria ORDER BY nombre"
            conexion.prepareStatement(sql).use { ps ->
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        lista.add(
                            CategoriaEntidad(
                                id = rs.getInt("id"),
                                nombre = rs.getString("nombre"),
                                descripcion = rs.getString("descripcion") ?: ""
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    fun listarConFiltro(filtro: String): List<CategoriaEntidad> {
        val lista = mutableListOf<CategoriaEntidad>()

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
                            CategoriaEntidad(
                                id = rs.getInt("id"),
                                nombre = rs.getString("nombre"),
                                descripcion = rs.getString("descripcion") ?: ""
                            )
                        )
                    }
                }
            }
        }
        return lista
    }

    fun insertar(categoria: CategoriaEntidad): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = "INSERT INTO categoria (nombre, descripcion) VALUES (?, ?)"
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, categoria.nombre)
                    ps.setString(2, categoria.descripcion.ifEmpty { null })
                    ps.executeUpdate() > 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun actualizar(categoria: CategoriaEntidad): Boolean {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = "UPDATE categoria SET nombre = ?, descripcion = ? WHERE id = ?"
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, categoria.nombre)
                    ps.setString(2, categoria.descripcion.ifEmpty { null })
                    ps.setInt(3, categoria.id)
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
                // Primero verificar si tiene productos asociados
                val sqlVerificar = "SELECT COUNT(*) as total FROM producto WHERE id_categoria = ?"
                conexion.prepareStatement(sqlVerificar).use { ps ->
                    ps.setInt(1, id)
                    ps.executeQuery().use { rs ->
                        if (rs.next() && rs.getInt("total") > 0) {
                            return false // No eliminar si tiene productos asociados
                        }
                    }
                }

                // Si no tiene productos, eliminar la categoría
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

    fun obtenerPorId(id: Int): CategoriaEntidad? {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = "SELECT id, nombre, descripcion FROM categoria WHERE id = ?"
                conexion.prepareStatement(sql).use { ps ->
                    ps.setInt(1, id)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            CategoriaEntidad(
                                id = rs.getInt("id"),
                                nombre = rs.getString("nombre"),
                                descripcion = rs.getString("descripcion") ?: ""
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
                    "SELECT COUNT(*) as total FROM categoria WHERE LOWER(nombre) = LOWER(?) AND id != ?"
                } else {
                    "SELECT COUNT(*) as total FROM categoria WHERE LOWER(nombre) = LOWER(?)"
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
            true // Si hay error, asumir que existe para evitar duplicados
        }
    }

    fun contarProductos(idCategoria: Int): Int {
        return try {
            PostgresqlConexion.getConexion().use { conexion ->
                val sql = "SELECT COUNT(*) as total FROM producto WHERE id_categoria = ? AND activo = true"
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

    fun listarConContadorProductos(): List<Pair<CategoriaEntidad, Int>> {
        val lista = mutableListOf<Pair<CategoriaEntidad, Int>>()

        PostgresqlConexion.getConexion().use { conexion ->
            val sql = """
                SELECT c.id, c.nombre, c.descripcion, 
                       COUNT(p.id) as total_productos
                FROM categoria c
                LEFT JOIN producto p ON c.id = p.id_categoria AND p.activo = true
                GROUP BY c.id, c.nombre, c.descripcion
                ORDER BY c.nombre
            """
            conexion.prepareStatement(sql).use { ps ->
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        val categoria = CategoriaEntidad(
                            id = rs.getInt("id"),
                            nombre = rs.getString("nombre"),
                            descripcion = rs.getString("descripcion") ?: ""
                        )
                        val totalProductos = rs.getInt("total_productos")
                        lista.add(Pair(categoria, totalProductos))
                    }
                }
            }
        }
        return lista
    }
}