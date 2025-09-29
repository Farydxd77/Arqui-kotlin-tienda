package com.example.arquiprimerparcial.negocio.servicio

import com.example.arquiprimerparcial.data.dao.CategoriaDao
import com.example.arquiprimerparcial.data.dao.ProductoDao

class ProductoServicio {

    // ✅ Instancias privadas de los DAOs
    private val productoDao: ProductoDao = ProductoDao()
    private val categoriaDao: CategoriaDao = CategoriaDao()

    fun listarProductosPrimitivos(filtro: String = ""): List<Map<String, Any>> {
        return try {
            val productosArray = productoDao.listarProducto(filtro)

            val resultado = mutableListOf<Map<String, Any>>()

            for (producto in productosArray) {
                resultado.add(mapOf(
                    "id" to (producto[0] as Int),
                    "nombre" to (producto[1] as String),
                    "descripcion" to (producto[2] as String),
                    "url" to (producto[3] as String),
                    "precio" to (producto[4] as Double),
                    "stock" to (producto[5] as Int),
                    "idCategoria" to (producto[6] as Int),
                    "activo" to (producto[7] as Boolean),
                    "categoriaNombre" to (producto[8] as String)
                ))
            }

            resultado
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun listarProductos(filtro: String = ""): List<Array<Any>> {
        return productoDao.listarProducto(filtro)
    }

    fun obtenerProductosPorCategoria(idCategoria: Int): List<Array<Any>> {
        return productoDao.listarPorCategoria(idCategoria)
    }

    fun obtenerProductoPorId(id: Int): Array<Any>? {
        return productoDao.obtenerPorId(id)
    }

    fun crearProductoActualizar(id: Int, nombre: String, descripcion: String, url: String,
                                precio: Double, stock: Int, idCategoria: Int, activo: Boolean = true): Result<Boolean> {
        return try {
            if (nombre.isBlank()) {
                return Result.failure(Exception("El nombre del producto es obligatorio"))
            }

            if (nombre.length < 3) {
                return Result.failure(Exception("El nombre debe tener al menos 3 caracteres"))
            }

            if (nombre.length > 200) {
                return Result.failure(Exception("El nombre no puede exceder 200 caracteres"))
            }

            if (precio < 0.01) {
                return Result.failure(Exception("El precio debe ser mayor a 0.01"))
            }

            if (stock < 0) {
                return Result.failure(Exception("El stock no puede ser negativo"))
            }

            if (descripcion.length > 500) {
                return Result.failure(Exception("La descripción no puede exceder 500 caracteres"))
            }

            if (url.isNotEmpty() && !validarUrl(url)) {
                return Result.failure(Exception("La URL debe comenzar con http:// o https://"))
            }

            if (idCategoria > 0) {
                val categoria = categoriaDao.obtenerPorId(idCategoria)
                if (categoria == null) {
                    return Result.failure(Exception("La categoría seleccionada no existe"))
                }
            }

            val resultado = if (id == 0) {
                productoDao.crearProducto(
                    nombre.trim(), descripcion.trim(), url.trim(),
                    precio, stock, idCategoria, activo
                )
            } else {
                productoDao.actualizarProducto(
                    id, nombre.trim(), descripcion.trim(), url.trim(),
                    precio, stock, idCategoria, activo
                )
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

    fun desactivarProducto(id: Int): Result<Boolean> {
        return try {
            if (id <= 0) {
                return Result.failure(Exception("ID de producto inválido"))
            }

            val resultado = productoDao.desactivarProducto(id)
            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al desactivar el producto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun activarProducto(id: Int): Result<Boolean> {
        return try {
            if (id <= 0) {
                return Result.failure(Exception("ID de producto inválido"))
            }

            val productoArray = productoDao.obtenerPorId(id)
            if (productoArray == null) {
                return Result.failure(Exception("Producto no encontrado"))
            }

            val resultado = productoDao.actualizarProducto(
                id = productoArray[0] as Int,
                nombre = productoArray[1] as String,
                descripcion = productoArray[2] as String,
                url = productoArray[3] as String,
                precio = productoArray[4] as Double,
                stock = productoArray[5] as Int,
                idCategoria = productoArray[6] as Int,
                activo = true
            )

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

            val resultado = productoDao.actualizarStock(id, nuevoStock)
            if (resultado) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al actualizar el stock"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun obtenerProductosStockBajo(limite: Int = 5): List<Array<Any>> {
        return productoDao.listarStockBajo(limite)
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
        if (url.isBlank()) return true
        return url.startsWith("http://") || url.startsWith("https://")
    }

    fun validarNombre(nombre: String): Boolean {
        return nombre.isNotBlank() && nombre.length >= 3 && nombre.length <= 200
    }

    fun validarDescripcion(descripcion: String): Boolean {
        return descripcion.length <= 500
    }

    fun buscarProductos(query: String): List<Array<Any>> {
        return listarProductos(query)
    }

    fun calcularValorInventario(productoArray: Array<Any>): Double {
        val precio = productoArray[4] as Double
        val stock = productoArray[5] as Int
        return precio * stock
    }

    fun tieneStock(productoArray: Array<Any>): Boolean {
        val stock = productoArray[5] as Int
        return stock > 0
    }

    fun stockBajo(productoArray: Array<Any>, limite: Int = 5): Boolean {
        val stock = productoArray[5] as Int
        return stock <= limite && stock > 0
    }

    fun sinStock(productoArray: Array<Any>): Boolean {
        val stock = productoArray[5] as Int
        return stock == 0
    }

    fun tieneStockPrimitivo(producto: Map<String, Any>): Boolean {
        val stock = producto["stock"] as Int
        return stock > 0
    }

    fun stockBajoPrimitivo(producto: Map<String, Any>, limite: Int = 5): Boolean {
        val stock = producto["stock"] as Int
        return stock <= limite && stock > 0
    }

    fun sinStockPrimitivo(producto: Map<String, Any>): Boolean {
        val stock = producto["stock"] as Int
        return stock == 0
    }

    fun formatearPrecio(precio: Double): String {
        return String.format("%.2f", precio)
    }
}