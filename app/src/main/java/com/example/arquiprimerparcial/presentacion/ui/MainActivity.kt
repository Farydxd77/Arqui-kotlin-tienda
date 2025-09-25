package com.example.arquiprimerparcial.presentacion.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arquiprimerparcial.databinding.ActivityMainBinding
import com.example.arquiprimerparcial.negocio.modelo.ProductoModelo
import com.example.arquiprimerparcial.negocio.servicio.ProductoServicio
import com.example.arquiprimerparcial.presentacion.adapter.ProductoAdapter
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.presentacion.common.makeCall
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), ProductoAdapter.IOnClickListener {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
        cargarProductos("")
    }

    override fun onResume() {
        super.onResume()
        if (!existeCambio) return
        existeCambio = false
        cargarProductos(binding.etBuscar.text.toString().trim())
    }

    private fun initListener() {
        binding.includeToolbar.ibAccion.setOnClickListener {
            startActivity(Intent(this, OperacionProductoActivity::class.java))
        }

        // Add button listeners for navigation
        binding.btnCrearPedido.setOnClickListener {
            startActivity(Intent(this, CrearPedidoActivity::class.java))
        }

        binding.btnVerPedidos.setOnClickListener {
            startActivity(Intent(this, HistorialPedidosActivity::class.java))
        }

        binding.rvLista.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ProductoAdapter(this@MainActivity)
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
            setTitle("Eliminar")
            setMessage("¿Desea eliminar el registro: ${producto.nombre}?") // Fixed from descripcion
            setCancelable(false)
            setNegativeButton("NO") { dialog, _ -> dialog.dismiss() }
            setPositiveButton("SI") { dialog, _ ->
                eliminarProducto(producto.id)
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun cargarProductos(filtro: String) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall { ProductoServicio.obtenerProductos(filtro) }.let { result ->
            binding.progressBar.isVisible = false

            when (result) {
                is UiState.Error -> mostrarError(result.message)
                is UiState.Success -> {
                    (binding.rvLista.adapter as ProductoAdapter).setList(result.data)
                }
            }
        }
    }

    private fun eliminarProducto(id: Int) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall { ProductoServicio.eliminarProducto(id) }.let { result ->
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
}