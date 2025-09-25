package com.example.arquiprimerparcial.presentacion.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arquiprimerparcial.databinding.ActivityCrearPedidoBinding
import com.example.arquiprimerparcial.negocio.modelo.DetallePedidoModelo
import com.example.arquiprimerparcial.negocio.modelo.PedidoModelo
import com.example.arquiprimerparcial.negocio.modelo.ProductoModelo
import com.example.arquiprimerparcial.negocio.servicio.PedidoServicio
import com.example.arquiprimerparcial.negocio.servicio.ProductoServicio
import com.example.arquiprimerparcial.presentacion.adapter.PedidoDetalleAdapter
import com.example.arquiprimerparcial.presentacion.adapter.ProductoAdapter
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.presentacion.common.makeCall
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class CrearPedidoActivity : AppCompatActivity(),
    ProductoAdapter.IOnClickListener,
    PedidoDetalleAdapter.IOnClickListener {

    private lateinit var binding: ActivityCrearPedidoBinding
    private lateinit var productoAdapter: ProductoAdapter
    private lateinit var detalleAdapter: PedidoDetalleAdapter
    private var pedidoActual = PedidoModelo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        initListeners()
        cargarProductos()
    }

    private fun initUI() {
        // Setup RecyclerView productos
        productoAdapter = ProductoAdapter(this)
        binding.rvProductos.apply {
            layoutManager = LinearLayoutManager(this@CrearPedidoActivity)
            adapter = productoAdapter
        }

        // Setup RecyclerView detalles pedido
        detalleAdapter = PedidoDetalleAdapter(this)
        binding.rvDetallesPedido.apply {
            layoutManager = LinearLayoutManager(this@CrearPedidoActivity)
            adapter = detalleAdapter
        }

        actualizarResumenPedido()
    }

    private fun initListeners() {
        binding.btnFinalizarPedido.setOnClickListener {
            if (validarPedido()) {
                confirmarPedido()
            }
        }

        binding.btnLimpiarPedido.setOnClickListener {
            limpiarPedido()
        }

        binding.etBuscarProducto.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                cargarProductos(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Implementación ProductoAdapter.IOnClickListener
    override fun clickSeleccionar(producto: ProductoModelo) {
        if (producto.sinStock()) {
            mostrarError("Producto sin stock disponible")
            return
        }

        mostrarDialogoCantidad(producto)
    }

    override fun clickEditar(producto: ProductoModelo) {
        // No implementado en esta pantalla
    }

    override fun clickEliminar(producto: ProductoModelo) {
        // No implementado en esta pantalla
    }

    // Implementación PedidoDetalleAdapter.IOnClickListener
    override fun clickEliminar(detalle: DetallePedidoModelo) {
        pedidoActual.eliminarDetalle(detalle.idProducto)
        actualizarUI()
    }

    override fun clickModificarCantidad(detalle: DetallePedidoModelo, nuevaCantidad: Int) {
        pedidoActual.modificarCantidad(detalle.idProducto, nuevaCantidad)
        actualizarUI()
    }

    private fun mostrarDialogoCantidad(producto: ProductoModelo) {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Agregar ${producto.nombre}")
            .setMessage("Stock disponible: ${producto.stock}")

        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Cantidad"
        }

        dialog.setView(input)
        dialog.setPositiveButton("Agregar") { _, _ ->
            val cantidadStr = input.text.toString()
            if (cantidadStr.isNotEmpty()) {
                val cantidad = cantidadStr.toIntOrNull() ?: 0
                if (cantidad > 0 && cantidad <= producto.stock) {
                    val detalle = DetallePedidoModelo.desdeProducto(producto, cantidad)
                    pedidoActual.agregarDetalle(detalle)
                    actualizarUI()
                } else {
                    mostrarError("Cantidad inválida o mayor al stock disponible")
                }
            }
        }
        dialog.setNegativeButton("Cancelar", null)
        dialog.show()
    }

    private fun cargarProductos(filtro: String = "") = lifecycleScope.launch {
        binding.progressBar.isVisible = true
        makeCall { ProductoServicio.obtenerProductos(filtro) }.let { result ->
            binding.progressBar.isVisible = false
            when (result) {
                is UiState.Error -> mostrarError(result.message)
                is UiState.Success -> productoAdapter.setList(result.data)
            }
        }
    }

    private fun actualizarUI() {
        detalleAdapter.setList(pedidoActual.detalles)
        actualizarResumenPedido()
    }

    private fun actualizarResumenPedido() {
        binding.tvTotalPedido.text = "Total: S/ ${pedidoActual.formatearTotal()}"
        binding.tvCantidadProductos.text = "${pedidoActual.cantidadTotalProductos()} productos"

        binding.btnFinalizarPedido.isEnabled = pedidoActual.detalles.isNotEmpty()
    }

    private fun validarPedido(): Boolean {
        val nombreCliente = binding.etNombreCliente.text.toString().trim()

        when {
            nombreCliente.isEmpty() -> {
                binding.etNombreCliente.error = "Ingrese el nombre del cliente"
                return false
            }
            pedidoActual.detalles.isEmpty() -> {
                mostrarError("Agregue al menos un producto al pedido")
                return false
            }
        }

        pedidoActual.nombreCliente = nombreCliente
        return true
    }

    private fun confirmarPedido() = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall { PedidoServicio.crearPedido(pedidoActual) }.let { result ->
            binding.progressBar.isVisible = false

            when (result) {
                is UiState.Error -> mostrarError(result.message)
                is UiState.Success -> {
                    if (result.data.isSuccess) {
                        val pedidoId = result.data.getOrNull() ?: 0
                        mostrarExito("Pedido #$pedidoId creado exitosamente")
                        limpiarPedido()
                    } else {
                        mostrarError(result.data.exceptionOrNull()?.message ?: "Error al crear pedido")
                    }
                }
            }
        }
    }

    private fun limpiarPedido() {
        pedidoActual = PedidoModelo()
        binding.etNombreCliente.text?.clear()
        actualizarUI()
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