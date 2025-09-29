package com.example.arquiprimerparcial.presentacion.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arquiprimerparcial.R
import com.example.arquiprimerparcial.databinding.ActivityHistorialPedidosBinding
import com.example.arquiprimerparcial.databinding.ItemsPedidoBinding
import com.example.arquiprimerparcial.negocio.servicio.HistorialPedidosServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.presentacion.common.makeCall
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class HistorialPedidosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialPedidosBinding
    private val historialPedidosServicio: HistorialPedidosServicio = HistorialPedidosServicio()

    private var listaPedidos = mutableListOf<Map<String, Any>>()
    private var ventasDelDia = 0.0
    private var totalPedidosHoy = 0

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
        binding.rvPedidos.apply {
            layoutManager = LinearLayoutManager(this@HistorialPedidosActivity)
            adapter = PedidoAdapter()
        }
    }

    inner class PedidoAdapter : RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>() {

        inner class PedidoViewHolder(private val binding: ItemsPedidoBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun enlazar(pedido: Map<String, Any>) {
                val id = pedido["id"] as Int
                val nombreCliente = pedido["nombreCliente"] as String
                val fecha = pedido["fecha"] as String
                val total = pedido["total"] as Double
                val cantidadProductos = pedido["cantidadProductos"] as Int
                @Suppress("UNCHECKED_CAST")
                val detalles = pedido["detalles"] as List<Map<String, Any>>

                binding.tvNumeroPedido.text = "#${id.toString().padStart(3, '0')}"
                binding.tvNombreCliente.text = nombreCliente
                binding.tvFecha.text = fecha
                binding.tvTotal.text = "S/ ${"%.2f".format(total)}"
                binding.tvCantidadProductos.text = "$cantidadProductos productos"

                binding.root.setOnClickListener {
                    verDetallePedido(id, nombreCliente, fecha, total, detalles)
                }

                binding.btnEliminar.setOnClickListener {
                    confirmarEliminar(id)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
            return PedidoViewHolder(
                ItemsPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun getItemCount(): Int = listaPedidos.size

        override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
            holder.enlazar(listaPedidos[position])
        }
    }

    private fun cargarDatos() = lifecycleScope.launch {
        cargarListaPedidos()
        cargarEstadisticas()
    }

    private fun cargarListaPedidos() = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall {
            historialPedidosServicio.obtenerTodosPedidosPrimitivos()
        }.let { result ->
            binding.progressBar.isVisible = false
            when (result) {
                is UiState.Success -> {
                    listaPedidos.clear()
                    listaPedidos.addAll(result.data)
                    binding.rvPedidos.adapter?.notifyDataSetChanged()
                }
                is UiState.Error -> mostrarError(result.message)
            }
        }
    }

    private fun cargarEstadisticas() = lifecycleScope.launch {
        makeCall {
            historialPedidosServicio.obtenerEstadisticasDia()
        }.let { result ->
            when (result) {
                is UiState.Success -> {
                    val (ventas, totalPedidos) = result.data
                    ventasDelDia = ventas
                    totalPedidosHoy = totalPedidos

                    binding.tvVentasDelDia.text = "S/ ${"%.2f".format(ventas)}"
                    binding.tvTotalPedidos.text = "$totalPedidos pedidos"
                }
                is UiState.Error -> {
                    binding.tvVentasDelDia.text = "S/ 0.00"
                    binding.tvTotalPedidos.text = "0 pedidos"
                }
            }
        }
    }

    private fun verDetallePedido(
        id: Int,
        nombreCliente: String,
        fecha: String,
        total: Double,
        detalles: List<Map<String, Any>>
    ) {
        val mensaje = historialPedidosServicio.construirMensajeDetallePrimitivo(
            id, nombreCliente, fecha, total, detalles
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Detalle del Pedido")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmarEliminar(idPedido: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar Pedido")
            .setMessage("¿Está seguro de eliminar el pedido #$idPedido?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarPedido(idPedido)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarPedido(idPedido: Int) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall {
            historialPedidosServicio.eliminarPedidoCompleto(idPedido)
        }.let { result ->
            binding.progressBar.isVisible = false

            when (result) {
                is UiState.Success -> {
                    if (result.data.isSuccess) {
                        mostrarExito("Pedido eliminado correctamente")
                        listaPedidos.removeAll { it["id"] == idPedido }
                        binding.rvPedidos.adapter?.notifyDataSetChanged()
                        cargarEstadisticas()
                    } else {
                        mostrarError(result.data.exceptionOrNull()?.message ?: "Error al eliminar")
                    }
                }
                is UiState.Error -> mostrarError(result.message)
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

    private fun mostrarExito(mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Éxito")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }
}