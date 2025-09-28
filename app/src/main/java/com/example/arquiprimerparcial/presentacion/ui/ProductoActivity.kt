package com.example.arquiprimerparcial.presentacion.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arquiprimerparcial.R
import com.example.arquiprimerparcial.databinding.ActivityProductoBinding
import com.example.arquiprimerparcial.negocio.servicio.ProductoServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductoBinding
    private lateinit var adapter: ProductoAdapterIntegrado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initAdapter()
        initListener()
        cargarProductos("")
    }

    override fun onResume() {
        super.onResume()
        cargarProductos(binding.etBuscar.text.toString().trim())
    }

    private fun initToolbar() {
        binding.includeToolbar.toolbar.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            subtitle = "Gestión de Productos"
            navigationIcon = AppCompatResources.getDrawable(
                this@ProductoActivity,
                R.drawable.baseline_arrow_back_24
            )
            setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }

        // Botón del toolbar para crear nuevo producto
        binding.includeToolbar.ibAccion.apply {
            isVisible = true
            setImageResource(R.drawable.baseline_add_24)
        }
    }

    private fun initAdapter() {
        adapter = ProductoAdapterIntegrado(
            onClickEditar = { productoArray ->
                val id = productoArray[0] as Int
                val nombre = productoArray[1] as String
                val descripcion = productoArray[2] as String
                val url = productoArray[3] as String
                val precio = productoArray[4] as Double
                val stock = productoArray[5] as Int
                val idCategoria = productoArray[6] as Int

                startActivity(
                    Intent(this, OperacionProductoActivity::class.java).apply {
                        putExtra("id", id)
                        putExtra("nombre", nombre)
                        putExtra("descripcion", descripcion)
                        putExtra("precio", precio)
                        putExtra("stock", stock)
                        putExtra("url", url)
                        putExtra("idCategoria", idCategoria)
                    }
                )
            },
            onClickEliminar = { productoArray ->
                val id = productoArray[0] as Int
                val nombre = productoArray[1] as String

                MaterialAlertDialogBuilder(this).apply {
                    setTitle("Desactivar Producto")
                    setMessage("¿Desea desactivar el producto: $nombre?\n\nNota: El producto quedará oculto pero se conservará en el historial.")
                    setCancelable(false)
                    setNegativeButton("CANCELAR") { dialog, _ -> dialog.dismiss() }
                    setPositiveButton("DESACTIVAR") { dialog, _ ->
                        desactivarProducto(id)
                        dialog.dismiss()
                    }
                }.create().show()
            },
            onClickSeleccionar = { /* No usado en esta activity */ }
        )

        binding.rvLista.apply {
            layoutManager = LinearLayoutManager(this@ProductoActivity)
            adapter = this@ProductoActivity.adapter
        }
    }

    private fun initListener() {
        // Botón del toolbar para crear nuevo producto
        binding.includeToolbar.ibAccion.setOnClickListener {
            startActivity(Intent(this, OperacionProductoActivity::class.java))
        }

        // Botón grande para crear nuevo producto
        binding.btnCrearProductoGrande.setOnClickListener {
            startActivity(Intent(this, OperacionProductoActivity::class.java))
        }

        // FAB para crear nuevo producto
        binding.fabCrearProducto.setOnClickListener {
            startActivity(Intent(this, OperacionProductoActivity::class.java))
        }

        // Búsqueda
        binding.tilBuscar.setEndIconOnClickListener {
            cargarProductos(binding.etBuscar.text.toString().trim())
        }

        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (binding.etBuscar.text.toString().trim().isEmpty()) {
                    cargarProductos("")
                    ocultarTeclado()
                }
            }
        })
    }

    private fun cargarProductos(filtro: String) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        val result = withContext(Dispatchers.IO) {
            try {
                UiState.Success(ProductoServicio.listarProductos(filtro))
            } catch (e: Exception) {
                UiState.Error(e.message.orEmpty())
            }
        }

        binding.progressBar.isVisible = false

        when (result) {
            is UiState.Error -> mostrarError(result.message)
            is UiState.Success -> {
                adapter.setList(result.data)

                // Mostrar mensaje si está vacío
                if (result.data.isEmpty()) {
                    if (filtro.isEmpty()) {
                        mostrarInfo("No hay productos registrados")
                    } else {
                        mostrarInfo("No se encontraron productos con el filtro: '$filtro'")
                    }
                }
            }
        }
    }

    private fun desactivarProducto(id: Int) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        val result = withContext(Dispatchers.IO) {
            try {
                UiState.Success(ProductoServicio.desactivarProducto(id))
            } catch (e: Exception) {
                UiState.Error(e.message.orEmpty())
            }
        }

        binding.progressBar.isVisible = false

        when (result) {
            is UiState.Error -> mostrarError(result.message)
            is UiState.Success -> {
                if (result.data.isSuccess) {
                    Toast.makeText(this@ProductoActivity, "✅ Producto desactivado", Toast.LENGTH_SHORT).show()
                    cargarProductos(binding.etBuscar.text.toString().trim())
                } else {
                    mostrarError(result.data.exceptionOrNull()?.message ?: "Error al desactivar el producto")
                }
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("ERROR")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun mostrarInfo(mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Información")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun ocultarTeclado() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    // ================================
    // ADAPTADOR INTEGRADO DIRECTAMENTE
    // ================================
    private class ProductoAdapterIntegrado(
        private val onClickEditar: (Array<Any>) -> Unit,
        private val onClickEliminar: (Array<Any>) -> Unit,
        private val onClickSeleccionar: (Array<Any>) -> Unit
    ) : RecyclerView.Adapter<ProductoAdapterIntegrado.ProductoViewHolder>() {

        private var lista = emptyList<Array<Any>>()

        inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvTitulo: TextView = itemView.findViewById(R.id.tv_titulo)
            private val tvCategoria: TextView = itemView.findViewById(R.id.tv_categoria)
            private val tvStock: TextView = itemView.findViewById(R.id.tv_stock)
            private val tvPrecio: TextView = itemView.findViewById(R.id.tv_precio)
            private val ibEditar: ImageButton = itemView.findViewById(R.id.ib_editar)
            private val ibEliminar: ImageButton = itemView.findViewById(R.id.ib_eliminar)

            fun enlazar(productoArray: Array<Any>) {
                val nombre = productoArray[1] as String
                val precio = productoArray[4] as Double
                val stock = productoArray[5] as Int
                val idCategoria = productoArray[6] as Int
                val categoriaNombre = productoArray[8] as String

                tvTitulo.text = nombre
                tvCategoria.text = when {
                    categoriaNombre.isNotEmpty() -> "🏷️ $categoriaNombre"
                    idCategoria > 0 -> "🏷️ Categoría ID: $idCategoria"
                    else -> "🏷️ Sin categoría"
                }
                tvStock.text = "Stock: $stock"
                tvPrecio.text = "S/ ${ProductoServicio.formatearPrecio(precio)}"

                // Color según estado del stock
                when {
                    ProductoServicio.sinStock(productoArray) -> {
                        tvStock.setTextColor(Color.RED)
                        itemView.alpha = 0.6f
                    }
                    ProductoServicio.stockBajo(productoArray) -> {
                        tvStock.setTextColor(Color.parseColor("#FF9800"))
                        itemView.alpha = 0.8f
                    }
                    else -> {
                        tvStock.setTextColor(Color.parseColor("#4CAF50"))
                        itemView.alpha = 1.0f
                    }
                }

                ibEditar.setOnClickListener { onClickEditar(productoArray) }
                ibEliminar.setOnClickListener { onClickEliminar(productoArray) }

                // Para selección en pedidos
                itemView.setOnClickListener { onClickSeleccionar(productoArray) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.items_producto, parent, false)
            return ProductoViewHolder(view)
        }

        override fun getItemCount(): Int = lista.size

        override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
            holder.enlazar(lista[position])
        }

        fun setList(listaProducto: List<Array<Any>>) {
            this.lista = listaProducto
            notifyDataSetChanged()
        }
    }
}