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
import com.example.arquiprimerparcial.databinding.ActivityCategoriaBinding
import com.example.arquiprimerparcial.negocio.modelo.CategoriaModelo
import com.example.arquiprimerparcial.negocio.servicio.CategoriaServicio
import com.example.arquiprimerparcial.presentacion.adapter.CategoriaAdapter
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriaActivity : AppCompatActivity(), CategoriaAdapter.IOnClickListener {
    private lateinit var binding: ActivityCategoriaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initListener()
        cargarCategorias("")
    }

    override fun onResume() {
        super.onResume()
        cargarCategorias(binding.etBuscar.text.toString().trim())
    }

    private fun initToolbar() {
        binding.includeToolbar.toolbar.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            subtitle = "Gestión de Categorías"
            navigationIcon = AppCompatResources.getDrawable(
                this@CategoriaActivity,
                R.drawable.baseline_arrow_back_24
            )
            setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun initListener() {
        // Botón para crear nueva categoría
        binding.includeToolbar.ibAccion.setOnClickListener {
            startActivity(Intent(this, OperacionCategoriaActivity::class.java))
        }

        // RecyclerView
        binding.rvLista.apply {
            layoutManager = LinearLayoutManager(this@CategoriaActivity)
            adapter = CategoriaAdapter(this@CategoriaActivity)
        }

        // Búsqueda
        binding.tilBuscar.setEndIconOnClickListener {
            cargarCategorias(binding.etBuscar.text.toString().trim())
        }

        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (binding.etBuscar.text.toString().trim().isEmpty()) {
                    cargarCategorias("")
                    ocultarTeclado()
                }
            }
        })
    }

    override fun clickEditar(categoria: CategoriaModelo) {
        startActivity(
            Intent(this, OperacionCategoriaActivity::class.java).apply {
                putExtra("id", categoria.id)
                putExtra("nombre", categoria.nombre)
                putExtra("descripcion", categoria.descripcion)
            }
        )
    }

    override fun clickEliminar(categoria: CategoriaModelo) {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Eliminar Categoría")
            setMessage("¿Desea eliminar la categoría: ${categoria.nombre}?\n\nNota: No se puede eliminar si tiene productos asociados.")
            setCancelable(false)
            setNegativeButton("CANCELAR") { dialog, _ -> dialog.dismiss() }
            setPositiveButton("ELIMINAR") { dialog, _ ->
                eliminarCategoria(categoria.id)
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun cargarCategorias(filtro: String) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        val result = withContext(Dispatchers.IO) {
            try {
                if (filtro.isEmpty()) {
                    UiState.Success(CategoriaServicio.listarCategorias())
                } else {
                    UiState.Success(CategoriaServicio.obtenerCategoriasConFiltro(filtro))
                }
            } catch (e: Exception) {
                UiState.Error(e.message.orEmpty())
            }
        }

        binding.progressBar.isVisible = false

        when (result) {
            is UiState.Error -> mostrarError(result.message)
            is UiState.Success -> {
                (binding.rvLista.adapter as CategoriaAdapter).setList(result.data)

                // Mostrar mensaje si está vacío
                if (result.data.isEmpty()) {
                    if (filtro.isEmpty()) {
                        mostrarInfo("No hay categorías registradas")
                    } else {
                        mostrarInfo("No se encontraron categorías con el filtro: '$filtro'")
                    }
                }
            }
        }
    }

    private fun eliminarCategoria(id: Int) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        val result = withContext(Dispatchers.IO) {
            try {
                UiState.Success(CategoriaServicio.eliminarCategoria(id))
            } catch (e: Exception) {
                UiState.Error(e.message.orEmpty())
            }
        }

        binding.progressBar.isVisible = false

        when (result) {
            is UiState.Error -> mostrarError(result.message)
            is UiState.Success -> {
                if (result.data.isSuccess) {
                    Toast.makeText(this@CategoriaActivity, "✅ Categoría eliminada", Toast.LENGTH_SHORT).show()
                    cargarCategorias(binding.etBuscar.text.toString().trim())
                } else {
                    mostrarError(result.data.exceptionOrNull()?.message ?: "Error al eliminar la categoría")
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