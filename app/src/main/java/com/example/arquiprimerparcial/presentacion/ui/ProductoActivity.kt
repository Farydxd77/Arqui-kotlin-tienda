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

    // ✅ Instancia del servicio
    private val productoServicio: ProductoServicio = ProductoServicio()

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

        binding.includeToolbar.ibAccion.apply {
            isVisible = true
            setImageResource(R.drawable.baseline_add_24)
        }
    }

    private fun initAdapter() {
        adapter = ProductoAdapterIntegrado(
            productoServicio = productoServicio, // ✅ PASAR LA INSTANCIA AQUÍ
            onClickEditar = { productoMap ->
                val id = productoMap["id"] as Int
                val nombre = productoMap["nombre"] as String
                val descripcion = productoMap["descripcion"] as String
                val url = productoMap["url"] as String
                val precio = productoMap["precio"] as Double
                val stock = productoMap["stock"] as Int
                val idCategoria = productoMap["idCategoria"] as Int

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
            onClickEliminar = { productoMap ->
                val id = productoMap["id"] as Int
                val nombre = productoMap["nombre"] as String

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
            }
        )

        binding.rvLista.apply {
            layoutManager = LinearLayoutManager(this@ProductoActivity)
            adapter = this@ProductoActivity.adapter
        }
    }

    private fun initListener() {
        binding.includeToolbar.ibAccion.setOnClickListener {
            startActivity(Intent(this, OperacionProductoActivity::class.java))
        }

        binding.btnCrearProductoGrande.setOnClickListener {
            startActivity(Intent(this, OperacionProductoActivity::class.java))
        }

        binding.fabCrearProducto.setOnClickListener {
            startActivity(Intent(this, OperacionProductoActivity::class.java))
        }

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
                UiState.Success(productoServicio.listarProductosPrimitivos(filtro))
            } catch (e: Exception) {
                UiState.Error(e.message.orEmpty())
            }
        }

        binding.progressBar.isVisible = false

        when (result) {
            is UiState.Error -> mostrarError(result.message)
            is UiState.Success -> {
                adapter.setList(result.data)

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
                UiState.Success(productoServicio.desactivarProducto(id))
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
    // ADAPTADOR INTEGRADO
    // ================================
    private class ProductoAdapterIntegrado(
        private val productoServicio: ProductoServicio, // ✅ RECIBE LA INSTANCIA
        private val onClickEditar: (Map<String, Any>) -> Unit,
        private val onClickEliminar: (Map<String, Any>) -> Unit
    ) : RecyclerView.Adapter<ProductoAdapterIntegrado.ProductoViewHolder>() {

        private var lista = emptyList<Map<String, Any>>()

        inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvTitulo: TextView = itemView.findViewById(R.id.tv_titulo)
            private val tvNombre: TextView = itemView.findViewById(R.id.tv_nombre)
            private val tvCategoria: TextView = itemView.findViewById(R.id.tv_categoria)
            private val tvStock: TextView = itemView.findViewById(R.id.tv_stock)
            private val tvPrecio: TextView = itemView.findViewById(R.id.tv_precio)
            private val ibEditar: ImageButton = itemView.findViewById(R.id.ib_editar)
            private val ibEliminar: ImageButton = itemView.findViewById(R.id.ib_eliminar)

            fun enlazar(productoMap: Map<String, Any>) {
                val nombre = productoMap["nombre"] as String
                val precio = productoMap["precio"] as Double
                val stock = productoMap["stock"] as Int
                val idCategoria = productoMap["idCategoria"] as Int
                val categoriaNombre = productoMap["categoriaNombre"] as String

                tvTitulo.text = nombre
                tvNombre.text = nombre

                tvCategoria.text = when {
                    categoriaNombre.isNotEmpty() -> "🏷️ $categoriaNombre"
                    idCategoria > 0 -> "🏷️ Categoría ID: $idCategoria"
                    else -> "🏷️ Sin categoría"
                }

                tvStock.text = "Stock: $stock"

                // ✅ USA LA INSTANCIA DEL SERVICIO
                tvPrecio.text = "S/ ${productoServicio.formatearPrecio(precio)}"

                when {
                    productoServicio.sinStockPrimitivo(productoMap) -> {
                        tvStock.setTextColor(Color.RED)
                        itemView.alpha = 0.6f
                    }
                    productoServicio.stockBajoPrimitivo(productoMap) -> {
                        tvStock.setTextColor(Color.parseColor("#FF9800"))
                        itemView.alpha = 0.8f
                    }
                    else -> {
                        tvStock.setTextColor(Color.parseColor("#4CAF50"))
                        itemView.alpha = 1.0f
                    }
                }

                ibEditar.setOnClickListener { onClickEditar(productoMap) }
                ibEliminar.setOnClickListener { onClickEliminar(productoMap) }
                itemView.setOnClickListener { onClickEditar(productoMap) }
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

        fun setList(listaProducto: List<Map<String, Any>>) {
            this.lista = listaProducto
            notifyDataSetChanged()
        }
    }
}