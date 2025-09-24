package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.CategoriaDao
import com.example.arquiprimerparcial.data.dao.ProductoDao
import com.example.arquiprimerparcial.data.entidad.ProductoEntidad
import com.example.arquiprimerparcial.negocio.modelo.ProductoModelo

object ProductoServicio {

    fun obtenerProductos(filtro: String = ""): List<ProductoModelo> {
        return ProductoDao.listar(filtro).map { entidad ->
            val categoria = if (entidad.id_categoria > 0) {
                CategoriaDao.obtenerPorId(entidad.id_categoria)
            } else null

            ProductoModelo(
                id = entidad.id,
                nombre = entidad.nombre,
                descripcion = entidad.descripcion,
                url = entidad.url,
                precio = entidad.precio,
                stock = entidad.stock,
                idCategoria = entidad.id_categoria,
                activo = entidad.activo,
                nombreCategoria = categoria?.nombre ?: ""
            )
        }
    }

    fun obtenerProductosPorCategoria(idCategoria: Int): List<ProductoModelo> {
        return ProductoDao.listarPorCategoria(idCategoria).map { entidad ->
            val categoria = CategoriaDao.obtenerPorId(entidad.id_categoria)

            ProductoModelo(
                id = entidad.id,
                nombre = entidad.nombre,
                descripcion = entidad.descripcion,
                url = entidad.url,
                precio = entidad.precio,
                stock = entidad.stock,
                idCategoria = entidad.id_categoria,
                activo = entidad.activo,
                nombreCategoria = categoria?.nombre ?: ""
            )
        }
    }

    fun obtenerProductoPorId(id: Int): ProductoModelo? {
        return ProductoDao.obtenerPorId(id)?.let { entidad ->
            val categoria = if (entidad.id_categoria > 0) {
                CategoriaDao.obtenerPorId(entidad.id_categoria)
            } else null

            ProductoModelo(
                id = entidad.id,
                nombre = entidad.nombre,
                descripcion = entidad.descripcion,
                url = entidad.url,
                precio = entidad.precio,
                stock = entidad.stock,
                idCategoria = entidad.id_categoria,
                activo = entidad.activo,
                nombreCategoria = categoria?.nombre ?: ""
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

            if (producto.nombre.length < 2) {
                return Result.failure(Exception("El nombre debe tener al menos 2 caracteres"))
            }

            if (producto.stock < 0) {
                return Result.failure(Exception("El stock no puede ser negativo"))
            }

            // Validar que la categoría existe si se especifica
            if (producto.idCategoria > 0) {
                val categoria = CategoriaDao.obtenerPorId(producto.idCategoria)
                if (categoria == null) {
                    return Result.failure(Exception("La categoría seleccionada no existe"))
                }
            }

            val entidad = ProductoEntidad(
                id = producto.id,
                nombre = producto.nombre.trim(),
                descripcion = producto.descripcion.trim(),
                url = producto.url.trim(),
                precio = producto.precio,
                stock = producto.stock,
                id_categoria = if (producto.idCategoria == 0) 0 else producto.idCategoria,
                activo = producto.activo
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

            val resultado = ProductoDao.eliminar(id) // Eliminación lógica
            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar el producto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun activarProducto(id: Int): Result<Boolean> {
        return try {
            val producto = ProductoDao.obtenerPorId(id)
            if (producto == null) {
                return Result.failure(Exception("Producto no encontrado"))
            }

            producto.activo = true
            val resultado = ProductoDao.actualizar(producto)

            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al activar el producto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun actualizarStock(id: Int, nuevoStock: Int): Result<Boolean> {
        return try {
            if (nuevoStock < 0) {
                return Result.failure(Exception("El stock no puede ser negativo"))
            }

            val resultado = ProductoDao.actualizarStock(id, nuevoStock)
            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al actualizar el stock"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun obtenerProductosStockBajo(limite: Int = 5): List<ProductoModelo> {
        return ProductoDao.listarStockBajo(limite).map { entidad ->
            val categoria = if (entidad.id_categoria > 0) {
                CategoriaDao.obtenerPorId(entidad.id_categoria)
            } else null

            ProductoModelo(
                id = entidad.id,
                nombre = entidad.nombre,
                descripcion = entidad.descripcion,
                url = entidad.url,
                precio = entidad.precio,
                stock = entidad.stock,
                idCategoria = entidad.id_categoria,
                activo = entidad.activo,
                nombreCategoria = categoria?.nombre ?: ""
            )
        }
    }

    fun validarPrecio(precio: String): Boolean {
        return try {
            val precioDouble = precio.toDouble()
            precioDouble > 0.0
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun validarStock(stock: String): Boolean {
        return try {
            val stockInt = stock.toInt()
            stockInt >= 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun validarUrl(url: String): Boolean {
        if (url.isBlank()) return true // URL es opcional
        return url.startsWith("http://") || url.startsWith("https://")
    }

    fun buscarProductos(query: String): List<ProductoModelo> {
        return obtenerProductos(query)
    }

//    fun obtenerEstadisticasProductos(): ProductoEstadisticasModelo {
//        val todosProductos = obtenerProductos()
//
//        return ProductoEstadisticasModelo(
//            totalProductos = todosProductos.size,
//            productosActivos = todosProductos.count { it.activo },
//            productosInactivos = todosProductos.count { !it.activo },
//            productosSinStock = todosProductos.count { it.sinStock() },
//            productosStockBajo = todosProductos.count { it.stockBajo() },
//            valorTotalInventario = todosProductos.sumOf { it.calcularValorInventario() }
//        )
//    }
}