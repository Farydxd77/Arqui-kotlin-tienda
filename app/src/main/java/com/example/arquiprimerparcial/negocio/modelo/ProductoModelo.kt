package com.example.arquiprimerparcial.negocio.modelo

data class ProductoModelo(
    var id: Int = 0,
    var nombre: String = "",
    var descripcion: String = "",
    var url: String = "",
    var precio: Double = 0.0,
    var stock: Int = 0,
    var idCategoria: Int = 0,
    var activo: Boolean = true,
    var nombreCategoria: String = "" // Para mostrar en UI
) {
    fun esValido(): Boolean {
        return nombre.isNotBlank() &&
                precio > 0.0 &&
                stock >= 0
    }

    fun formatearPrecio(): String {
        return String.format("%.2f", precio)
    }

    fun tieneStock(): Boolean {
        return stock > 0
    }

    fun stockBajo(limite: Int = 5): Boolean {
        return stock <= limite && stock > 0
    }

    fun sinStock(): Boolean {
        return stock == 0
    }

    fun calcularValorInventario(): Double {
        return precio * stock
    }

    fun getEstadoStock(): String {
        return when {
            sinStock() -> "Sin Stock"
            stockBajo() -> "Stock Bajo"
            else -> "Stock OK"
        }
    }
}
