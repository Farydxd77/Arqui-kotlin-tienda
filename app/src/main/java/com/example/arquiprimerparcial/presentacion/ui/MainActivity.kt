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
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arquiprimerparcial.R
import com.example.arquiprimerparcial.databinding.ActivityMainBinding
import com.example.arquiprimerparcial.negocio.servicio.ProductoServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ProductoAdapterIntegrado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAdapter()
        initListener()
        cargarProductos("")
    }

    override fun onResume() {
        super.onResume()
        if (!existeCambio) return
        existeCambio = false
        cargarProductos(binding.etBuscar.text.toString().trim())
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
                    setTitle("Eliminar")
                    setMessage("¿Desea eliminar el registro: $nombre?")
                    setCancelable(false)
                    setNegativeButton("NO") { dialog, _ -> dialog.dismiss() }
                    setPositiveButton("SI") { dialog, _ ->
                        eliminarProducto(id)
                        dialog.dismiss()
                    }
                }.create().show()
            }
        )

        binding.rvLista.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun initListener() {
        // Botón para crear nuevo producto
        binding.includeToolbar.ibAccion.setOnClickListener {
            startActivity(Intent(this, OperacionProductoActivity::class.java))
        }

        // Botón para gestionar productos
        binding.btnGestionarProductos.setOnClickListener {
            startActivity(Intent(this, ProductoActivity::class.java))
        }

        // Botón para gestionar categorías
        binding.btnCategorias.setOnClickListener {
            startActivity(Intent(this, CategoriaActivity::class.java))
        }

        binding.btnCrearPedido.setOnClickListener {
            startActivity(Intent(this, CrearPedidoActivity::class.java))
        }

        binding.btnVerPedidos.setOnClickListener {
            startActivity(Intent(this, HistorialPedidosActivity::class.java))
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
            }
        }
    }

    private fun eliminarProducto(id: Int) = lifecycleScope.launch {
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
                    Toast.makeText(this@MainActivity, "Registro eliminado", Toast.LENGTH_SHORT).show()
                    cargarProductos(binding.etBuscar.text.toString().trim())
                } else {
                    mostrarError("Error al eliminar el producto")
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

    private fun ocultarTeclado() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    companion object {
        var existeCambio = false
    }

    // ================================
    // ADAPTADOR INTEGRADO DIRECTAMENTE
    // ================================
    private class ProductoAdapterIntegrado(
        private val onClickEditar: (Array<Any>) -> Unit,
        private val onClickEliminar: (Array<Any>) -> Unit
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

                // Click en toda la card
                itemView.setOnClickListener { onClickEditar(productoArray) }
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