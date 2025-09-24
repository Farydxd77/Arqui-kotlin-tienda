package com.example.arquiprimerparcial.negocio.modelo
import java.sql.Timestamp
data class CategoriaModelo(
    var id: Int = 0,
    var nombre: String = "",
    var descripcion: String = ""
) {
    fun esValida(): Boolean {
        return nombre.isNotBlank() && nombre.length >= 2
    }

    fun tieneProdutos(): Boolean {
        // Esta función se usará en el servicio para verificar antes de eliminar
        return true
    }
}
