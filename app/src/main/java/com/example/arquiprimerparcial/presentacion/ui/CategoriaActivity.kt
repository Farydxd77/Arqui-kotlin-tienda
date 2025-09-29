package com.example.arquiprimerparcial.presentacion.ui

import android.content.Context
import android.content.Intent
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
import com.example.arquiprimerparcial.databinding.ActivityCategoriaBinding
import com.example.arquiprimerparcial.negocio.servicio.CategoriaServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoriaBinding
    private lateinit var adapter: CategoriaAdapterIntegrado
    private val categoriaServicio: CategoriaServicio = CategoriaServicio()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initAdapter()
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

    private fun initAdapter() {
        adapter = CategoriaAdapterIntegrado(
            onClickEditar = { categoriaArray ->
                val id = categoriaArray[0] as Int
                val nombre = categoriaArray[1] as String
                val descripcion = categoriaArray[2] as String

                startActivity(
                    Intent(this, OperacionCategoriaActivity::class.java).apply {
                        putExtra("id", id)
                        putExtra("nombre", nombre)
                        putExtra("descripcion", descripcion)
                    }
                )
            },
            onClickEliminar = { categoriaArray ->
                val id = categoriaArray[0] as Int
                val nombre = categoriaArray[1] as String

                MaterialAlertDialogBuilder(this).apply {
                    setTitle("Eliminar Categoría")
                    setMessage("¿Desea eliminar la categoría: $nombre?\n\nNota: No se puede eliminar si tiene productos asociados.")
                    setCancelable(false)
                    setNegativeButton("CANCELAR") { dialog, _ -> dialog.dismiss() }
                    setPositiveButton("ELIMINAR") { dialog, _ ->
                        eliminarCategoria(id)
                        dialog.dismiss()
                    }
                }.create().show()
            }
        )

        binding.rvLista.apply {
            layoutManager = LinearLayoutManager(this@CategoriaActivity)
            adapter = this@CategoriaActivity.adapter
        }
    }

    private fun initListener() {
        // Botón para crear nueva categoría
        binding.includeToolbar.ibAccion.setOnClickListener {
            startActivity(Intent(this, OperacionCategoriaActivity::class.java))
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

    private fun cargarCategorias(filtro: String) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        val result = withContext(Dispatchers.IO) {
            try {
                if (filtro.isEmpty()) {
                    UiState.Success(categoriaServicio.listarCategorias())
                } else {
                    UiState.Success(categoriaServicio.obtenerCategoriasConFiltro(filtro))
                }
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
                UiState.Success(categoriaServicio.eliminarCategoria(id))
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

    // ================================
    // ADAPTADOR INTEGRADO DIRECTAMENTE
    // ================================
    private class CategoriaAdapterIntegrado(
        private val onClickEditar: (Array<Any>) -> Unit,
        private val onClickEliminar: (Array<Any>) -> Unit
    ) : RecyclerView.Adapter<CategoriaAdapterIntegrado.CategoriaViewHolder>() {

        private var lista = emptyList<Array<Any>>()

        inner class CategoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvId: TextView = itemView.findViewById(R.id.tv_id)
            private val tvNombre: TextView = itemView.findViewById(R.id.tv_nombre)
            private val tvDescripcion: TextView = itemView.findViewById(R.id.tv_descripcion)
            private val ibEditar: ImageButton = itemView.findViewById(R.id.ib_editar)
            private val ibEliminar: ImageButton = itemView.findViewById(R.id.ib_eliminar)

            fun enlazar(categoriaArray: Array<Any>) {
                val id = categoriaArray[0] as Int
                val nombre = categoriaArray[1] as String
                val descripcion = categoriaArray[2] as String

                tvId.text = "ID: $id"
                tvNombre.text = nombre
                tvDescripcion.text = if (descripcion.isNotEmpty()) {
                    descripcion
                } else {
                    "Sin descripción"
                }

                ibEditar.setOnClickListener {
                    onClickEditar(categoriaArray)
                }

                ibEliminar.setOnClickListener {
                    onClickEliminar(categoriaArray)
                }

                // Click en toda la card para editar
                itemView.setOnClickListener {
                    onClickEditar(categoriaArray)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.items_categoria, parent, false)
            return CategoriaViewHolder(view)
        }

        override fun getItemCount(): Int = lista.size

        override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
            holder.enlazar(lista[position])
        }

        fun setList(listaCategoria: List<Array<Any>>) {
            this.lista = listaCategoria
            notifyDataSetChanged()
        }
    }
}