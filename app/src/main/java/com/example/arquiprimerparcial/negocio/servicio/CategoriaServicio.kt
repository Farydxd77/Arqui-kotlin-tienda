package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.CategoriaDao
import com.example.arquiprimerparcial.data.dao.ProductoDao

class CategoriaServicio {

    private val categoriaDao: CategoriaDao = CategoriaDao()
    // Retorna lista de arrays: [id, nombre, descripcion]
    fun listarCategorias(): List<Array<Any>> {
        return categoriaDao.listar()
    }

    fun obtenerCategoriasConFiltro(filtro: String): List<Array<Any>> {
        return categoriaDao.listarConFiltro(filtro)
    }

    fun guardarCategoria(id: Int, nombre: String, descripcion: String): Result<Boolean> {
        return try {
            // Validaciones de negocio primitivas
            if (nombre.isBlank()) {
                return Result.failure(Exception("El nombre de la categoría es obligatorio"))
            }

            if (nombre.length < 2) {
                return Result.failure(Exception("El nombre debe tener al menos 2 caracteres"))
            }

            if (nombre.length > 50) {
                return Result.failure(Exception("El nombre no puede exceder 50 caracteres"))
            }

            if (descripcion.length > 200) {
                return Result.failure(Exception("La descripción no puede exceder 200 caracteres"))
            }

            // Verificar si el nombre ya existe
            if (categoriaDao.existeNombre(nombre, id)) {
                return Result.failure(Exception("Ya existe una categoría con este nombre"))
            }

            val resultado = if (id == 0) {
                // Crear nueva categoría
                categoriaDao.insertar(nombre.trim(), descripcion.trim())
            } else {
                // Actualizar categoría existente
                categoriaDao.actualizar(id, nombre.trim(), descripcion.trim())
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
            val totalProductos = categoriaDao.contarProductos(id)
            if (totalProductos > 0) {
                return Result.failure(Exception("No se puede eliminar. La categoría tiene $totalProductos producto(s) asociado(s)"))
            }

            val resultado = categoriaDao.eliminar(id)
            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar la categoría"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Retorna array [id, nombre, descripcion] o null
    fun obtenerCategoriaPorId(id: Int): Array<Any>? {
        return categoriaDao.obtenerPorId(id)
    }

    // Validaciones primitivas
    fun validarNombre(nombre: String): Boolean {
        return nombre.isNotBlank() && nombre.length >= 2 && nombre.length <= 50
    }

    fun validarDescripcion(descripcion: String): Boolean {
        return descripcion.length <= 200
    }

    fun existeNombre(nombre: String, idExcluir: Int = 0): Boolean {
        return categoriaDao.existeNombre(nombre, idExcluir)
    }

    fun contarProductosEnCategoria(idCategoria: Int): Int {
        return categoriaDao.contarProductos(idCategoria)
    }
}