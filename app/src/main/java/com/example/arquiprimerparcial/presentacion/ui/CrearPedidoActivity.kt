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
import com.example.arquiprimerparcial.databinding.ActivityCrearPedidoBinding
import com.example.arquiprimerparcial.databinding.ItemsProductoBinding
import com.example.arquiprimerparcial.databinding.ItemsPedidoDetalleBinding
import com.example.arquiprimerparcial.negocio.servicio.PedidoServicio
import com.example.arquiprimerparcial.negocio.servicio.ProductoServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.presentacion.common.makeCall
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class CrearPedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearPedidoBinding

    // ✅ SOLO PRIMITIVOS
    private var listaProductos = mutableListOf<Map<String, Any>>()
    private var detallesPedido = mutableListOf<Map<String, Any>>()
    private var totalPedido = 0.0
    private var cantidadTotal = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initUI()
        initListeners()
        cargarProductos()
    }

    private fun initToolbar() {
        binding.includeToolbar.toolbar.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            subtitle = "Crear nuevo pedido"
            navigationIcon = AppCompatResources.getDrawable(
                this@CrearPedidoActivity,
                R.drawable.baseline_arrow_back_24
            )
            setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        binding.includeToolbar.ibAccion.isVisible = false
    }

    private fun initUI() {
        // Configurar RecyclerView de productos
        binding.rvProductos.apply {
            layoutManager = LinearLayoutManager(this@CrearPedidoActivity)
            adapter = ProductoAdapter()
        }

        // Configurar RecyclerView de detalles
        binding.rvDetallesPedido.apply {
            layoutManager = LinearLayoutManager(this@CrearPedidoActivity)
            adapter = DetalleAdapter()
        }

        actualizarResumen()
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

    // ✅ ADAPTADOR INTERNO PARA PRODUCTOS
    inner class ProductoAdapter : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

        inner class ProductoViewHolder(private val binding: ItemsProductoBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun enlazar(producto: Map<String, Any>) {
                val id = producto["id"] as Int
                val nombre = producto["nombre"] as String
                val precio = producto["precio"] as Double
                val stock = producto["stock"] as Int

                binding.tvNombre.text = nombre
                binding.tvPrecio.text = "S/ ${"%.2f".format(precio)}"
                binding.tvStock.text = "Stock: $stock"

                binding.btnSeleccionar.isEnabled = stock > 0
                binding.btnSeleccionar.text = if (stock > 0) "Agregar" else "Sin Stock"

                binding.btnSeleccionar.setOnClickListener {
                    if (stock > 0) {
                        mostrarDialogoCantidad(producto)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
            return ProductoViewHolder(
                ItemsProductoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun getItemCount(): Int = listaProductos.size

        override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
            holder.enlazar(listaProductos[position])
        }
    }

    // ✅ ADAPTADOR INTERNO PARA DETALLES
    inner class DetalleAdapter : RecyclerView.Adapter<DetalleAdapter.DetalleViewHolder>() {

        inner class DetalleViewHolder(private val binding: ItemsPedidoDetalleBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun enlazar(detalle: Map<String, Any>) {
                val idProducto = detalle["idProducto"] as Int
                val nombreProducto = detalle["nombreProducto"] as String
                val precioUnitario = detalle["precioUnitario"] as Double
                val cantidad = detalle["cantidad"] as Int
                val subtotal = detalle["subtotal"] as Double

                binding.tvNombreProducto.text = nombreProducto
                binding.tvPrecioUnitario.text = "S/ ${"%.2f".format(precioUnitario)}"
                binding.tvCantidad.text = cantidad.toString()
                binding.tvSubtotal.text = "S/ ${"%.2f".format(subtotal)}"

                binding.btnMenos.setOnClickListener {
                    if (cantidad > 1) {
                        modificarCantidad(idProducto, cantidad - 1)
                    }
                }

                binding.btnMas.setOnClickListener {
                    modificarCantidad(idProducto, cantidad + 1)
                }

                binding.btnEliminar.setOnClickListener {
                    eliminarDetalle(idProducto)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetalleViewHolder {
            return DetalleViewHolder(
                ItemsPedidoDetalleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun getItemCount(): Int = detallesPedido.size

        override fun onBindViewHolder(holder: DetalleViewHolder, position: Int) {
            holder.enlazar(detallesPedido[position])
        }
    }

    private fun mostrarDialogoCantidad(producto: Map<String, Any>) {
        val nombre = producto["nombre"] as String
        val stock = producto["stock"] as Int

        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            hint = "Cantidad"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Agregar $nombre")
            .setMessage("Stock disponible: $stock")
            .setView(input)
            .setPositiveButton("Agregar") { _, _ ->
                val cantidadStr = input.text.toString()
                if (cantidadStr.isNotEmpty()) {
                    val cantidad = cantidadStr.toIntOrNull() ?: 0
                    if (cantidad > 0 && cantidad <= stock) {
                        agregarDetalle(producto, cantidad)
                    } else {
                        mostrarError("Cantidad inválida o mayor al stock disponible")
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun agregarDetalle(producto: Map<String, Any>, cantidad: Int) {
        val idProducto = producto["id"] as Int
        val nombre = producto["nombre"] as String
        val precio = producto["precio"] as Double

        // Buscar si ya existe el producto
        val detalleExistente = detallesPedido.find { it["idProducto"] == idProducto }

        if (detalleExistente != null) {
            // Actualizar cantidad existente
            val cantidadActual = detalleExistente["cantidad"] as Int
            modificarCantidad(idProducto, cantidadActual + cantidad)
        } else {
            // Agregar nuevo detalle
            val detalle = mapOf(
                "idProducto" to idProducto,
                "nombreProducto" to nombre,
                "precioUnitario" to precio,
                "cantidad" to cantidad,
                "subtotal" to (precio * cantidad)
            )
            detallesPedido.add(detalle)
        }

        actualizarUI()
    }

    private fun modificarCantidad(idProducto: Int, nuevaCantidad: Int) {
        val index = detallesPedido.indexOfFirst { it["idProducto"] == idProducto }
        if (index != -1) {
            val detalle = detallesPedido[index].toMutableMap()
            val precio = detalle["precioUnitario"] as Double
            detalle["cantidad"] = nuevaCantidad
            detalle["subtotal"] = precio * nuevaCantidad
            detallesPedido[index] = detalle
            actualizarUI()
        }
    }

    private fun eliminarDetalle(idProducto: Int) {
        detallesPedido.removeAll { it["idProducto"] == idProducto }
        actualizarUI()
    }

    private fun cargarProductos(filtro: String = "") = lifecycleScope.launch {
        binding.progressBar.isVisible = true
        makeCall { ProductoServicio.listarProductos(filtro) }.let { result ->
            binding.progressBar.isVisible = false
            when (result) {
                is UiState.Error -> mostrarError(result.message)
                is UiState.Success -> {
                    // Convertir modelos a primitivos
                    listaProductos.clear()
                    result.data.forEach { producto ->
                        listaProductos.add(mapOf(
                            "id" to producto.id,
                            "nombre" to producto.nombre,
                            "precio" to producto.precio,
                            "stock" to producto.stock
                        ))
                    }
                    binding.rvProductos.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun actualizarUI() {
        binding.rvDetallesPedido.adapter?.notifyDataSetChanged()
        actualizarResumen()
    }

    private fun actualizarResumen() {
        totalPedido = detallesPedido.sumOf { it["subtotal"] as Double }
        cantidadTotal = detallesPedido.sumOf { it["cantidad"] as Int }

        binding.tvTotalPedido.text = "Total: S/ ${"%.2f".format(totalPedido)}"
        binding.tvCantidadProductos.text = "$cantidadTotal productos"
        binding.btnFinalizarPedido.isEnabled = detallesPedido.isNotEmpty()
    }

    private fun validarPedido(): Boolean {
        val nombreCliente = binding.etNombreCliente.text.toString().trim()

        when {
            nombreCliente.isEmpty() -> {
                binding.etNombreCliente.error = "Ingrese el nombre del cliente"
                return false
            }
            detallesPedido.isEmpty() -> {
                mostrarError("Agregue al menos un producto al pedido")
                return false
            }
        }
        return true
    }

    private fun confirmarPedido() = lifecycleScope.launch {
        binding.progressBar.isVisible = true
        val nombreCliente = binding.etNombreCliente.text.toString().trim()

        // Crear estructura primitiva para el servicio
        val pedidoData = mapOf(
            "nombreCliente" to nombreCliente,
            "detalles" to detallesPedido,
            "total" to totalPedido
        )

        makeCall { PedidoServicio.crearPedidoPrimitivo(pedidoData) }.let { result ->
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
        detallesPedido.clear()
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