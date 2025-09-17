package com.example.arquiprimerparcial.data.dao

import com.example.arquiprimerparcial.data.model.ProductoModel

object ProductoDao {

    fun listar(dato: String): List<ProductoModel> {
        var lista = mutableListOf<ProductoModel>()

        PostgresqlConexion.getConexion().prepareStatement(
            "Select id, descripcion, codigobarra, precio FROM producto WHERE LOWER(descripcion) LIKE '%' || LOWER(?) || '%';"
        ).use { ps ->
            ps.setString(1, dato)
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    lista.add(
                        ProductoModel(
                            rs.getInt("id"),
                            rs.getString("descripcion"),
                            rs.getString("codigobarra"),
                            rs.getDouble("precio")
                        )
                    )
                }
            }
        }

        return lista
    }

    private fun registrar(producto: ProductoModel) {
        PostgresqlConexion.getConexion().prepareStatement(
            "INSERT INTO producto (descripcion, codigobarra, precio) VALUES (?, ?, ?);"
        ).use { ps ->

            ps.setString(1, producto.descripcion)
            ps.setString(2, producto.codigobarra)
            ps.setDouble(3, producto.precio)
            ps.executeUpdate()
        }
    }

    private fun actualizar(producto: ProductoModel) {
        PostgresqlConexion.getConexion().prepareStatement(
            "UPDATE producto SET descripcion = ?, codigobarra = ?, precio = ? WHERE id = ?;"
        ).use { ps ->

            ps.setString(1, producto.descripcion)
            ps.setString(2, producto.codigobarra)
            ps.setDouble(3, producto.precio)
            ps.setInt(4, producto.id)
            ps.executeUpdate()
        }
    }

    fun eliminar(producto: ProductoModel) {
        PostgresqlConexion.getConexion().prepareStatement(
            "DELETE FROM producto WHERE id = ?;"
        ).use { ps ->
            ps.setInt(1, producto.id)
            ps.executeUpdate()
        }
    }

    fun grabar(producto: ProductoModel) {
        if (producto.id == 0) {
            registrar(producto)
        } else {
            actualizar(producto)
        }
    }
}