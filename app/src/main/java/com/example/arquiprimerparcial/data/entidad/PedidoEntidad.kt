package com.example.arquiprimerparcial.data.entidad

import java.sql.Timestamp

data class PedidoEntidad(
    var id: Int = 0,
    var nombre_cliente: String = "",
    var fecha_pedido: Timestamp? = null,
    var total: Double = 0.0
)