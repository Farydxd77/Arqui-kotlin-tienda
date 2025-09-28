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
import com.example.arquiprimerparcial.negocio.servicio.HistorialPedidosServicio
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
        cargarDatos()
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

    // ✅ MÉTODO UNIFICADO para cargar todo
    private fun cargarDatos() = lifecycleScope.launch {
        cargarListaPedidos()
        cargarEstadisticas()
    }

    private fun cargarListaPedidos() = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall {
            HistorialPedidosServicio.obtenerTodosPedidos()
        }.let { result ->
            binding.progressBar.isVisible = false
            when (result) {
                is UiState.Success -> pedidoAdapter.setList(result.data)
                is UiState.Error -> mostrarError(result.message)
            }
        }
    }

    private fun cargarEstadisticas() = lifecycleScope.launch {
        makeCall {
            HistorialPedidosServicio.obtenerEstadisticasDia()
        }.let { result ->
            when (result) {
                is UiState.Success -> {
                    val (ventas, totalPedidos) = result.data
                    binding.tvVentasDelDia.text = "S/ ${"%.2f".format(ventas)}"
                    binding.tvTotalPedidos.text = "$totalPedidos pedidos"
                }
                is UiState.Error -> {
                    // Ignorar errores de estadísticas, no son críticos
                    binding.tvVentasDelDia.text = "S/ 0.00"
                    binding.tvTotalPedidos.text = "0 pedidos"
                }
            }
        }
    }

    override fun clickVerDetalle(pedido: PedidoModelo) {
        val mensaje = construirMensajeDetalle(pedido)

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
                eliminarPedido(pedido.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ✅ MÉTODO SEPARADO para eliminar
    private fun eliminarPedido(idPedido: Int) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall {
            HistorialPedidosServicio.eliminarPedidoCompleto(idPedido)  // ✅ USAR SOLO HistorialPedidosServicio
        }.let { result ->
            binding.progressBar.isVisible = false

            when (result) {
                is UiState.Success -> {
                    if (result.data.isSuccess) {
                        mostrarExito("Pedido eliminado correctamente")
                        cargarDatos() // ✅ RECARGAR AMBOS: lista y estadísticas
                    } else {
                        mostrarError(result.data.exceptionOrNull()?.message ?: "Error al eliminar")
                    }
                }
                is UiState.Error -> mostrarError(result.message)
            }
        }
    }

    // ✅ MÉTODO SEPARADO para construir mensaje
    private fun construirMensajeDetalle(pedido: PedidoModelo): String {
        return buildString {
            append("📦 Pedido #${pedido.id}\n\n")
            append("👤 Cliente: ${pedido.nombreCliente}\n")
            append("📅 Fecha: ${pedido.fechaPedido}\n\n")
            append("🛒 Productos:\n")
            pedido.detalles.forEach { detalle ->
                append("• ${detalle.nombreProducto}\n")
                append("  ${detalle.cantidad} x S/ ${detalle.formatearPrecioUnitario()}")
                append(" = S/ ${detalle.formatearSubtotal()}\n")
            }
            append("\n💰 Total: S/ ${pedido.formatearTotal()}")
        }
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