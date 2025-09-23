package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.ProductoDao
import com.example.arquiprimerparcial.data.entidad.ProductoEntidad
import com.example.arquiprimerparcial.negocio.modelo.ProductoModelo

object ProductoServicio {

    fun obtenerProductos(filtro: String = ""): List<ProductoModelo> {
        return ProductoDao.listar(filtro).map { entidad ->
            ProductoModelo(
                id = entidad.id,
                descripcion = entidad.descripcion,
                codigobarra = entidad.codigobarra,
                precio = entidad.precio
            )
        }
    }

    fun guardarProducto(producto: ProductoModelo): Result<Boolean> {
        return try {
            // Validaciones de negocio
            if (!producto.esValido()) {
                return Result.failure(Exception("Datos del producto inválidos"))
            }

            if (producto.precio < 0.01) {
                return Result.failure(Exception("El precio debe ser mayor a 0.01"))
            }

            if (producto.descripcion.length < 3) {
                return Result.failure(Exception("La descripción debe tener al menos 3 caracteres"))
            }

            val entidad = ProductoEntidad(
                id = producto.id,
                descripcion = producto.descripcion.trim(),
                codigobarra = producto.codigobarra.trim(),
                precio = producto.precio
            )

            val resultado = if (producto.id == 0) {
                ProductoDao.insertar(entidad)
            } else {
                ProductoDao.actualizar(entidad)
            }

            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al guardar el producto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun eliminarProducto(id: Int): Result<Boolean> {
        return try {
            if (id <= 0) {
                return Result.failure(Exception("ID de producto inválido"))
            }

            val resultado = ProductoDao.eliminar(id)
            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar el producto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun validarCodigoBarra(codigo: String): Boolean {
        return codigo.isNotBlank() && codigo.length >= 8
    }

    fun validarPrecio(precio: String): Boolean {
        return try {
            val precioDouble = precio.toDouble()
            precioDouble > 0.0
        } catch (e: NumberFormatException) {
            false
        }
    }
}