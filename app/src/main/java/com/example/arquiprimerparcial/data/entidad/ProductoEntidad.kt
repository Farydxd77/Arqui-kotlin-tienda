package com.example.arquiprimerparcial.data.entidad

data class ProductoEntidad(
    var id: Int = 0,
    var nombre: String = "",
    var descripcion: String = "",
    var url: String = "",
    var precio: Double = 0.0,
    var stock: Int = 0,           // SOLO ESTE
    var activo: Boolean = true,
    var id_categoria: Int = 0

)