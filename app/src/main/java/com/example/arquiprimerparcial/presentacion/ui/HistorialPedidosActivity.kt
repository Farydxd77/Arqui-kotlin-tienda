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

class HistorialPedidosActivity : AppCompatActivity(), PedidoAdapter.IOnClickListener {

    private lateinit var binding: ActivityHistorialPedidosBinding
    private lateinit var pedidoAdapter: PedidoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialPedidosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initUI()
        cargarTodo()
        cargarResumen()
    }

    private fun initToolbar() {
        binding.includeToolbar.toolbar.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            subtitle = "Historial de Pedidos"
            navigationIcon = AppCompatResources.getDrawable(
                this@HistorialPedidosActivity,
                R.drawable.baseline_arrow_back_24
            )
            setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        binding.includeToolbar.ibAccion.isVisible = false
    }

    private fun initUI() {
        pedidoAdapter = PedidoAdapter(this)
        binding.rvPedidos.apply {
            layoutManager = LinearLayoutManager(this@HistorialPedidosActivity)
            adapter = pedidoAdapter
        }
    }

    private fun cargarTodo() = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall {
            PedidoServicio.obtenerPedidos()
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
            val ventas = PedidoServicio.obtenerVentasDelDia()
            val totalPedidos = PedidoServicio.obtenerTotalPedidosHoy()
            Pair(ventas, totalPedidos)
        }.let { result ->
            when (result) {
                is UiState.Success -> {
                    val (ventas, totalPedidos) = result.data
                    binding.tvVentasDelDia.text = "S/ ${"%.2f".format(ventas)}"
                    binding.tvTotalPedidos.text = "$totalPedidos pedidos"
                }
                is UiState.Error -> { /* ignorar error del resumen */ }
            }
        }
    }

    override fun clickVerDetalle(pedido: PedidoModelo) {
        // Mostrar detalles del pedido
        val mensaje = buildString {
            append("📦 Pedido #${pedido.id}\n\n")
            append("👤 Cliente: ${pedido.nombreCliente}\n")
            append("📅 Fecha: ${pedido.fechaPedido}\n\n")
            append("🛒 Productos:\n")
            pedido.detalles.forEach { detalle ->
                append("• ${detalle.nombreProducto}\n")
                append("  ${detalle.cantidad} x S/ ${detalle.formatearPrecioUnitario()} = S/ ${detalle.formatearSubtotal()}\n")
            }
            append("\n💰 Total: S/ ${pedido.formatearTotal()}")
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Detalle del Pedido")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun clickEliminar(pedido: PedidoModelo) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar Pedido")
            .setMessage("¿Está seguro de eliminar el pedido #${pedido.id}?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    binding.progressBar.isVisible = true

                    makeCall {
                        PedidoServicio.eliminarPedido(pedido.id)
                    }.let { result ->
                        binding.progressBar.isVisible = false

                        when (result) {
                            is UiState.Success -> {
                                if (result.data.isSuccess) {
                                    mostrarExito("Pedido eliminado correctamente")
                                    cargarTodo()
                                    cargarResumen()
                                } else {
                                    mostrarError(result.data.exceptionOrNull()?.message ?: "Error al eliminar")
                                }
                            }
                            is UiState.Error -> mostrarError(result.message)
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarError(mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun mostrarExito(mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Éxito")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }
}