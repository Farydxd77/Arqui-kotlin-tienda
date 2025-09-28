package com.example.arquiprimerparcial.presentacion.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.arquiprimerparcial.R
import com.example.arquiprimerparcial.databinding.ActivityOperacionCategoriaBinding
import com.example.arquiprimerparcial.negocio.modelo.CategoriaModelo
import com.example.arquiprimerparcial.negocio.servicio.CategoriaServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OperacionCategoriaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOperacionCategoriaBinding
    private var categoriaId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOperacionCategoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
        if (intent.extras != null) cargarDatosCategoria()
    }

    private fun initListener() {
        binding.includeToolbar.toolbar.apply {
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            subtitle = if (categoriaId == 0) "Crear Categoría" else "Editar Categoría"
            navigationIcon = AppCompatResources.getDrawable(
                this@OperacionCategoriaActivity,
                R.drawable.baseline_arrow_back_24
            )
        }

        binding.includeToolbar.ibAccion.setImageResource(R.drawable.baseline_done_all_24)
        binding.includeToolbar.ibAccion.setOnClickListener {
            if (validarDatos()) {
                guardarCategoria()
            }
        }
    }

    private fun cargarDatosCategoria() {
        categoriaId = intent.extras?.getInt("id", 0) ?: 0
        binding.etNombre.setText(intent.extras?.getString("nombre") ?: "")
        binding.etDescripcion.setText(intent.extras?.getString("descripcion") ?: "")

        // Actualizar título
        binding.includeToolbar.toolbar.subtitle = "Editar Categoría"
    }

    private fun validarDatos(): Boolean {
        val nombre = binding.etNombre.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()

        when {
            nombre.isEmpty() -> {
                mostrarAdvertencia("El nombre de la categoría es obligatorio")
                binding.etNombre.requestFocus()
                return false
            }
            nombre.length < 2 -> {
                mostrarAdvertencia("El nombre debe tener al menos 2 caracteres")
                binding.etNombre.requestFocus()
                return false
            }
            nombre.length > 50 -> {
                mostrarAdvertencia("El nombre no puede exceder 50 caracteres")
                binding.etNombre.requestFocus()
                return false
            }
            descripcion.length > 200 -> {
                mostrarAdvertencia("La descripción no puede exceder 200 caracteres")
                binding.etDescripcion.requestFocus()
                return false
            }
        }

        return true
    }

    private fun guardarCategoria() = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        val categoria = CategoriaModelo(
            id = categoriaId,
            nombre = binding.etNombre.text.toString().trim(),
            descripcion = binding.etDescripcion.text.toString().trim()
        )

        val result = withContext(Dispatchers.IO) {
            try {
                UiState.Success(CategoriaServicio.guardarCategoria(categoria))
            } catch (e: Exception) {
                UiState.Error(e.message.orEmpty())
            }
        }

        binding.progressBar.isVisible = false

        when (result) {
            is UiState.Error -> mostrarError(result.message)
            is UiState.Success -> {
                if (result.data.isSuccess) {
                    val mensaje = if (categoriaId == 0) "✅ Categoría creada" else "✅ Categoría actualizada"
                    Toast.makeText(this@OperacionCategoriaActivity, mensaje, Toast.LENGTH_SHORT).show()

                    if (categoriaId == 0) {
                        limpiarCampos()
                        binding.etNombre.requestFocus()
                    } else {
                        // Si es edición, regresar
                        finish()
                    }
                } else {
                    mostrarError(result.data.exceptionOrNull()?.message ?: "Error desconocido")
                }
            }
        }
    }

    private fun mostrarAdvertencia(mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("ADVERTENCIA")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun mostrarError(mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle("ERROR")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun limpiarCampos() {
        clearAllEditTexts(binding.root)
        categoriaId = 0
        binding.includeToolbar.toolbar.subtitle = "Crear Categoría"
    }

    private fun clearAllEditTexts(view: View) {
        when (view) {
            is EditText -> view.text?.clear()
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    clearAllEditTexts(view.getChildAt(i))
                }
            }
        }
    }
}