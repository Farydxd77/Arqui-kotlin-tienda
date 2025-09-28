package com.example.arquiprimerparcial.presentacion.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arquiprimerparcial.R
import com.example.arquiprimerparcial.databinding.ActivityProductoBinding
import com.example.arquiprimerparcial.negocio.modelo.ProductoModelo
import com.example.arquiprimerparcial.negocio.servicio.ProductoServicio
import com.example.arquiprimerparcial.presentacion.adapter.ProductoAdapter
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductoActivity : AppCompatActivity(), ProductoAdapter.IOnClickListener {
    private lateinit var binding: ActivityProductoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
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

        // ✅ ASEGURARSE QUE EL BOTÓN + ESTÉ VISIBLE
        binding.includeToolbar.ibAccion.apply {
            isVisible = true
            setImageResource(R.drawable.baseline_add_24)
        }
    }

    private fun initListener() {
        // Botón del toolbar para crear nuevo producto
        binding.includeToolbar.ibAccion.setOnClickListener {
            startActivity(Intent(this, OperacionProductoActivity::class.java))
        }

        // ✅ BOTÓN GRANDE para crear nuevo producto
        binding.btnCrearProductoGrande.setOnClickListener {
            startActivity(Intent(this, OperacionProductoActivity::class.java))
        }

        // ✅ FLOATING ACTION BUTTON para crear nuevo producto
        binding.fabCrearProducto.setOnClickListener {
            startActivity(Intent(this, OperacionProductoActivity::class.java))
        }

        // RecyclerView
        binding.rvLista.apply {
            layoutManager = LinearLayoutManager(this@ProductoActivity)
            adapter = ProductoAdapter(this@ProductoActivity)
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

    override fun clickEditar(producto: ProductoModelo) {
        startActivity(
            Intent(this, OperacionProductoActivity::class.java).apply {
                putExtra("id", producto.id)
                putExtra("nombre", producto.nombre)
                putExtra("descripcion", producto.descripcion)
                putExtra("precio", producto.precio)
                putExtra("stock", producto.stock)
                putExtra("url", producto.url)
                putExtra("idCategoria", producto.idCategoria)
            }
        )
    }

    override fun clickEliminar(producto: ProductoModelo) {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Desactivar Producto")
            setMessage("¿Desea desactivar el producto: ${producto.nombre}?\n\nNota: El producto quedará oculto pero se conservará en el historial.")
            setCancelable(false)
            setNegativeButton("CANCELAR") { dialog, _ -> dialog.dismiss() }
            setPositiveButton("DESACTIVAR") { dialog, _ ->
                desactivarProducto(producto.id)
                dialog.dismiss()
            }
        }.create().show()
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
                (binding.rvLista.adapter as ProductoAdapter).setList(result.data)

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
}