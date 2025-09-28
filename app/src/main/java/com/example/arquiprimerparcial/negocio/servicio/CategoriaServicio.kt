package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.CategoriaDao
import com.example.arquiprimerparcial.data.entidad.CategoriaEntidad
import com.example.arquiprimerparcial.negocio.modelo.CategoriaModelo

object CategoriaServicio {

    fun listarCategorias(): List<CategoriaModelo> {
        return CategoriaDao.listar().map { entidad ->
            CategoriaModelo(
                id = entidad.id,
                nombre = entidad.nombre,
                descripcion = entidad.descripcion
            )
        }
    }

    fun obtenerCategoriasConFiltro(filtro: String): List<CategoriaModelo> {
        return CategoriaDao.listarConFiltro(filtro).map { entidad ->
            CategoriaModelo(
                id = entidad.id,
                nombre = entidad.nombre,
                descripcion = entidad.descripcion
            )
        }
    }

    fun guardarCategoria(categoria: CategoriaModelo): Result<Boolean> {
        return try {
            // Validaciones de negocio
            if (!categoria.esValida()) {
                return Result.failure(Exception("Datos de la categoría inválidos"))
            }

            if (categoria.nombre.length < 2) {
                return Result.failure(Exception("El nombre debe tener al menos 2 caracteres"))
            }

            // Verificar si el nombre ya existe
            if (CategoriaDao.existeNombre(categoria.nombre, categoria.id)) {
                return Result.failure(Exception("Ya existe una categoría con este nombre"))
            }

            val entidad = CategoriaEntidad(
                id = categoria.id,
                nombre = categoria.nombre.trim(),
                descripcion = categoria.descripcion.trim()
            )

            val resultado = if (categoria.id == 0) {
                CategoriaDao.insertar(entidad)
            } else {
                CategoriaDao.actualizar(entidad)
            }

            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al guardar la categoría"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun eliminarCategoria(id: Int): Result<Boolean> {
        return try {
            if (id <= 0) {
                return Result.failure(Exception("ID de categoría inválido"))
            }

            // Verificar si tiene productos asociados
            val totalProductos = CategoriaDao.contarProductos(id)
            if (totalProductos > 0) {
                return Result.failure(Exception("No se puede eliminar. La categoría tiene $totalProductos producto(s) asociado(s)"))
            }

            val resultado = CategoriaDao.eliminar(id)
            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar la categoría"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun obtenerCategoriaPorId(id: Int): CategoriaModelo? {
        return CategoriaDao.obtenerPorId(id)?.let { entidad ->
            CategoriaModelo(
                id = entidad.id,
                nombre = entidad.nombre,
                descripcion = entidad.descripcion
            )
        }
    }
}
