package com.example.arquiprimerparcial.negocio.modelo

data class ProductoModelo(
    var id: Int = 0,
    var descripcion: String = "",
    var codigobarra: String = "",
    var precio: Double = 0.0
) {
    fun esValido(): Boolean {
        return descripcion.isNotBlank() &&
                codigobarra.isNotBlank() &&
                precio > 0.0
    }

    fun formatearPrecio(): String {
        return String.format("%.2f", precio)
    }
}