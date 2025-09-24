package com.example.arquiprimerparcial.negocio.modelo

data class DetallePedidoModelo(
    var id: Int = 0,
    var idPedido: Int = 0,
    var idProducto: Int = 0,
    var nombreProducto: String = "",
    var cantidad: Int = 0,
    var precioUnitario: Double = 0.0,
    var urlProducto: String = "" // Para mostrar imagen en la UI
) {
    fun calcularSubtotal(): Double {
        return cantidad * precioUnitario
    }

    fun formatearSubtotal(): String {
        return String.format("%.2f", calcularSubtotal())
    }

    fun formatearPrecioUnitario(): String {
        return String.format("%.2f", precioUnitario)
    }

    fun esValido(): Boolean {
        return idProducto > 0 &&
                cantidad > 0 &&
                precioUnitario > 0.0 &&
                nombreProducto.isNotBlank()
    }

    // Función para crear un detalle rápidamente desde un producto
    companion object {
        fun desdeProducto(producto: ProductoModelo, cantidad: Int): DetallePedidoModelo {
            return DetallePedidoModelo(
                idProducto = producto.id,
                nombreProducto = producto.nombre,
                cantidad = cantidad,
                precioUnitario = producto.precio,
                urlProducto = producto.url
            )
        }
    }
}