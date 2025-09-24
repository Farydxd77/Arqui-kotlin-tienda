package com.example.arquiprimerparcial.data.entidad

data class DetallePedidoEntidad(
    var id: Int = 0,
    var id_pedido: Int = 0,
    var id_producto: Int = 0,
    var cantidad: Int = 0,
    var precio_unitario: Double = 0.0
) {
    // Función para calcular subtotal
    fun calcularSubtotal(): Double {
        return cantidad * precio_unitario
    }
}