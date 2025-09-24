package com.example.arquiprimerparcial.negocio.modelo
import java.sql.Timestamp


data class PedidoModelo(
    var id: Int = 0,
    var nombreCliente: String = "",
    var fechaPedido: Timestamp? = null,
    var total: Double = 0.0,
    var detalles: MutableList<DetallePedidoModelo> = mutableListOf()
) {
    fun esValido(): Boolean {
        return nombreCliente.isNotBlank() &&
                detalles.isNotEmpty() &&
                total > 0.0
    }

    fun calcularTotal(): Double {
        return detalles.sumOf { it.calcularSubtotal() }
    }

    fun actualizarTotal() {
        total = calcularTotal()
    }

    fun agregarDetalle(detalle: DetallePedidoModelo) {
        // Verificar si ya existe el producto en el pedido
        val existente = detalles.find { it.idProducto == detalle.idProducto }
        if (existente != null) {
            // Si existe, actualizar cantidad
            existente.cantidad += detalle.cantidad
        } else {
            // Si no existe, agregar nuevo detalle
            detalles.add(detalle)
        }
        actualizarTotal()
    }

    fun eliminarDetalle(idProducto: Int) {
        detalles.removeAll { it.idProducto == idProducto }
        actualizarTotal()
    }

    fun modificarCantidad(idProducto: Int, nuevaCantidad: Int) {
        val detalle = detalles.find { it.idProducto == idProducto }
        if (detalle != null && nuevaCantidad > 0) {
            detalle.cantidad = nuevaCantidad
            actualizarTotal()
        } else if (nuevaCantidad <= 0) {
            eliminarDetalle(idProducto)
        }
    }

    fun formatearTotal(): String {
        return String.format("%.2f", total)
    }

    fun cantidadTotalProductos(): Int {
        return detalles.sumOf { it.cantidad }
    }

    fun limpiarDetalles() {
        detalles.clear()
        total = 0.0
    }
}
