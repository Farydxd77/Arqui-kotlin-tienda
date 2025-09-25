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
        cargarPedidos()
        cargarResumenDelDia()
    }

    private fun initToolbar() {
        binding.includeToolbar.toolbar.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            subtitle = "Historial de pedidos"
            navigationIcon = AppCompatResources.getDrawable(
                this@HistorialPedidosActivity,
                R.drawable.baseline_arrow_back_24
            )
            setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        // Ocultar el botón de acción del toolbar
        binding.includeToolbar.ibAccion.isVisible = false
    }

    private fun initUI() {
        pedidoAdapter = PedidoAdapter(this)
        binding.rvPedidos.apply {
            layoutManager = LinearLayoutManager(this@HistorialPedidosActivity)
            adapter = pedidoAdapter
        }
    }

    override fun clickVerDetalle(pedido: PedidoModelo) {
        mostrarDetallePedido(pedido)
    }

    override fun clickEliminar(pedido: PedidoModelo) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar Pedido")
            .setMessage("¿Está seguro de eliminar el pedido #${pedido.id}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarPedido(pedido.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cargarPedidos() = lifecycleScope.launch {
        binding.progressBar.isVisible = true
        makeCall { PedidoServicio.obtenerPedidos() }.let { result ->
            binding.progressBar.isVisible = false
            when (result) {
                is UiState.Error -> mostrarError(result.message)
                is UiState.Success -> pedidoAdapter.setList(result.data)
            }
        }
    }

    private fun cargarResumenDelDia() = lifecycleScope.launch {
        makeCall {
            Pair(
                PedidoServicio.obtenerVentasDelDia(),
                PedidoServicio.obtenerTotalPedidosHoy()
            )
        }.let { result ->
            when (result) {
                is UiState.Error -> {}
                is UiState.Success -> {
                    val (ventasDelDia, totalPedidos) = result.data
                    binding.tvVentasDelDia.text = "S/ ${String.format("%.2f", ventasDelDia)}"
                    binding.tvTotalPedidos.text = "$totalPedidos pedidos"
                }
            }
        }
    }

    private fun mostrarDetallePedido(pedido: PedidoModelo) {
        val detalles = pedido.detalles.joinToString("\n") { detalle ->
            "• ${detalle.nombreProducto} x${detalle.cantidad} = S/ ${detalle.formatearSubtotal()}"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Pedido #${pedido.id}")
            .setMessage("Cliente: ${pedido.nombreCliente}\n\nProductos:\n$detalles\n\nTotal: S/ ${pedido.formatearTotal()}")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun eliminarPedido(id: Int) = lifecycleScope.launch {
        makeCall { PedidoServicio.eliminarPedido(id) }.let { result ->
            when (result) {
                is UiState.Error -> mostrarError(result.message)
                is UiState.Success -> {
                    if (result.data.isSuccess) {
                        cargarPedidos()
                        cargarResumenDelDia()
                    } else {
                        mostrarError("Error al eliminar pedido")
                    }
                }
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }
}