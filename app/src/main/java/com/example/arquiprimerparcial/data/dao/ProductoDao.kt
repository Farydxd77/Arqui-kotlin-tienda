package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.conexion.PostgresqlConexion
import com.example.arquiprimerparcial.data.entidad.ProductoEntidad


object ProductoDao {

        fun listar(filtro: String): List<ProductoEntidad> {
            val lista = mutableListOf<ProductoEntidad>()

            PostgresqlConexion.getConexion().use { conexion ->
                val sql =
                    "SELECT id, descripcion, codigobarra, precio FROM producto WHERE LOWER(descripcion) LIKE '%' || LOWER(?) || '%'"
                conexion.prepareStatement(sql).use { ps ->
                    ps.setString(1, filtro)
                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            lista.add(
                                ProductoEntidad(
                                    id = rs.getInt("id"),
                                    descripcion = rs.getString("descripcion"),
                                    codigobarra = rs.getString("codigobarra"),
                                    precio = rs.getDouble("precio")
                                )
                            )
                        }
                    }
                }
            }

            return lista
        }

        fun insertar(producto: ProductoEntidad): Boolean {
            return try {
                PostgresqlConexion.getConexion().use { conexion ->
                    val sql =
                        "INSERT INTO producto (descripcion, codigobarra, precio) VALUES (?, ?, ?)"
                    conexion.prepareStatement(sql).use { ps ->
                        ps.setString(1, producto.descripcion)
                        ps.setString(2, producto.codigobarra)
                        ps.setDouble(3, producto.precio)
                        ps.executeUpdate() > 0
                    }
                }
            } catch (e: Exception) {
                false
            }
        }

        fun actualizar(producto: ProductoEntidad): Boolean {
            return try {
                PostgresqlConexion.getConexion().use { conexion ->
                    val sql =
                        "UPDATE producto SET descripcion = ?, codigobarra = ?, precio = ? WHERE id = ?"
                    conexion.prepareStatement(sql).use { ps ->
                        ps.setString(1, producto.descripcion)
                        ps.setString(2, producto.codigobarra)
                        ps.setDouble(3, producto.precio)
                        ps.setInt(4, producto.id)
                        ps.executeUpdate() > 0
                    }
                }
            } catch (e: Exception) {
                false
            }
        }

        fun eliminar(id: Int): Boolean {
            return try {
                PostgresqlConexion.getConexion().use { conexion ->
                    val sql = "DELETE FROM producto WHERE id = ?"
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
