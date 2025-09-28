package com.example.arquiprimerparcial.presentacion.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arquiprimerparcial.R
import com.example.arquiprimerparcial.databinding.ActivityHistorialPedidosBinding
import com.example.arquiprimerparcial.negocio.modelo.PedidoModelo
import com.example.arquiprimerparcial.negocio.servicio.PedidoServicio
import com.example.arquiprimerparcial.presentacion.adapter.PedidoAdapter
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.presentacion.common.makeCall
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class HistorialPedidosActivity : AppCompatActivity() {

    private fun cargarTodo() = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        // ✅ SOLO UNA llamada al servicio específico
        makeCall {
            HistorialServicio().obtenerHistorialCompleto()
        }.let { result ->
            binding.progressBar.isVisible = false
            when (result) {
                is UiState.Success -> pedidoAdapter.setList(result.data)
                is UiState.Error -> mostrarError(result.message)
            }
        }
    }

    private fun cargarResumen() = lifecycleScope.launch {
        makeCall {
            HistorialServicio().obtenerResumenDelDia()
        }.let { result ->
            when (result) {
                is UiState.Success -> {
                    val (ventas, totalPedidos) = result.data
                    binding.tvVentasDelDia.text = "S/ ${"%.2f".format(ventas)}"
                    binding.tvTotalPedidos.text = "$totalPedidos pedidos"
                }
                is UiState.Error -> { /* manejar error */ }
            }
        }
    }

    override fun clickEliminar(pedido: PedidoModelo) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar Pedido")
            .setMessage("¿Está seguro de eliminar el pedido #${pedido.id}?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    makeCall {
                        HistorialServicio().eliminarPedidoDelHistorial(pedido.id)
                    }.let { result ->
                        when (result) {
                            is UiState.Success -> if (result.data) {
                                cargarTodo() // Recargar
                                cargarResumen()
                            }
                            is UiState.Error -> mostrarError(result.message)
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}