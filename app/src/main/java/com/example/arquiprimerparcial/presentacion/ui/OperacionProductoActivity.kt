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
import com.example.arquiprimerparcial.databinding.ActivityOperacionProductoBinding
import com.example.arquiprimerparcial.negocio.modelo.ProductoModelo
import com.example.arquiprimerparcial.negocio.servicio.ProductoServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.presentacion.common.makeCall
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class OperacionProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOperacionProductoBinding
    private var productoId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOperacionProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
        if (intent.extras != null) cargarDatosProducto()
    }

    private fun initListener() {
        binding.includeToolbar.toolbar.apply {
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            subtitle = "Registrar | Editar producto"
            navigationIcon = AppCompatResources.getDrawable(
                this@OperacionProductoActivity,
                R.drawable.baseline_arrow_back_24
            )
        }

        binding.includeToolbar.ibAccion.setImageResource(R.drawable.baseline_done_all_24)
        binding.includeToolbar.ibAccion.setOnClickListener {
            if (validarDatos()) {
                guardarProducto()
            }
        }
    }

    private fun cargarDatosProducto() {
        productoId = intent.extras?.getInt("id", 0) ?: 0
        binding.etDescripcion.setText(intent.extras?.getString("nombre") ?: "")
        binding.etCodigoBarra.setText(intent.extras?.getString("descripcion") ?: "")
        binding.etPrecio.setText(intent.extras?.getDouble("precio", 0.0).toString())
    }

    private fun validarDatos(): Boolean {
        val nombre = binding.etDescripcion.text.toString().trim()
        val descripcion = binding.etCodigoBarra.text.toString().trim()
        val precio = binding.etPrecio.text.toString().trim()

        when {
            nombre.isEmpty() -> {
                mostrarAdvertencia("El nombre del producto es obligatorio")
                binding.etDescripcion.requestFocus()
                return false
            }
            nombre.length < 3 -> {
                mostrarAdvertencia("El nombre debe tener al menos 3 caracteres")
                binding.etDescripcion.requestFocus()
                return false
            }
            precio.isEmpty() -> {
                mostrarAdvertencia("El precio es obligatorio")
                binding.etPrecio.requestFocus()
                return false
            }
            !ProductoServicio.validarPrecio(precio) -> {
                mostrarAdvertencia("El precio debe ser un número válido mayor a 0")
                binding.etPrecio.requestFocus()
                return false
            }
        }

        return true
    }

    private fun guardarProducto() = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        val producto = ProductoModelo(
            id = productoId,
            nombre = binding.etDescripcion.text.toString().trim(),
            descripcion = binding.etCodigoBarra.text.toString().trim(),
            precio = binding.etPrecio.text.toString().toDouble(),
            stock = 0, // Default stock
            url = "", // Default empty URL
            idCategoria = 0, // Default no category
            activo = true
        )

        makeCall { ProductoServicio.guardarProducto(producto) }.let { result ->
            binding.progressBar.isVisible = false

            when (result) {
                is UiState.Error -> mostrarError(result.message)
                is UiState.Success -> {
                    if (result.data.isSuccess) {
                        Toast.makeText(this@OperacionProductoActivity, "Datos guardados", Toast.LENGTH_SHORT).show()
                        limpiarCampos()
                        binding.etDescripcion.requestFocus()
                        productoId = 0
                        MainActivity.existeCambio = true
                    } else {
                        mostrarError(result.data.exceptionOrNull()?.message ?: "Error desconocido")
                    }
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