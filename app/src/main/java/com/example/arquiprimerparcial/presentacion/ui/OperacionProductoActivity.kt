package com.example.arquiprimerparcial.presentacion.ui

import android.net.Uri
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
import coil.load
import com.example.arquiprimerparcial.R
import com.example.arquiprimerparcial.databinding.ActivityOperacionProductoBinding
import com.example.arquiprimerparcial.negocio.modelo.ProductoModelo
import com.example.arquiprimerparcial.negocio.servicio.ProductoServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.utils.CloudinaryHelper
import com.example.arquiprimerparcial.utils.ImagePickerHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OperacionProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOperacionProductoBinding
    private var productoId = 0
    private var imageUrl = ""
    private var selectedImageUri: Uri? = null

    private lateinit var imagePickerHelper: ImagePickerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOperacionProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✨ Inicializar Cloudinary
        CloudinaryHelper.init(this)

        // ✨ Inicializar Image Picker
        imagePickerHelper = ImagePickerHelper(this) { uri ->
            selectedImageUri = uri
            binding.ivPreview.load(uri) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_background)
            }
            binding.ivPreview.isVisible = true
        }

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

        // ✨ Botón para seleccionar imagen
        binding.btnSeleccionarImagen.setOnClickListener {
            imagePickerHelper.selectImage()
        }

        // ✨ Botón para eliminar imagen
        binding.btnEliminarImagen.setOnClickListener {
            selectedImageUri = null
            imageUrl = ""
            binding.ivPreview.isVisible = false
        }
    }

    private fun cargarDatosProducto() {
        productoId = intent.extras?.getInt("id", 0) ?: 0
        binding.etDescripcion.setText(intent.extras?.getString("nombre") ?: "")
        binding.etCodigoBarra.setText(intent.extras?.getString("descripcion") ?: "")
        binding.etPrecio.setText(intent.extras?.getDouble("precio", 0.0).toString())
        binding.etStock.setText(intent.extras?.getInt("stock", 0).toString())

        imageUrl = intent.extras?.getString("url") ?: ""
        if (imageUrl.isNotEmpty()) {
            binding.ivPreview.load(imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
            }
            binding.ivPreview.isVisible = true
        }
    }

    private fun validarDatos(): Boolean {
        val nombre = binding.etDescripcion.text.toString().trim()
        val descripcion = binding.etCodigoBarra.text.toString().trim()
        val precio = binding.etPrecio.text.toString().trim()
        val stock = binding.etStock.text.toString().trim()

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
            stock.isEmpty() -> {
                mostrarAdvertencia("El stock es obligatorio")
                binding.etStock.requestFocus()
                return false
            }
        }

        return true
    }

    private fun guardarProducto() = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        try {
            // ✨ Si hay una imagen seleccionada, subirla primero
            if (selectedImageUri != null) {
                binding.tvEstadoSubida.isVisible = true
                binding.tvEstadoSubida.text = "☁️ Subiendo imagen a Cloudinary..."

                imageUrl = CloudinaryHelper.uploadImage(selectedImageUri!!)

                binding.tvEstadoSubida.text = "✅ Imagen subida"
            }

            val producto = ProductoModelo(
                id = productoId,
                nombre = binding.etDescripcion.text.toString().trim(),
                descripcion = binding.etCodigoBarra.text.toString().trim(),
                precio = binding.etPrecio.text.toString().toDouble(),
                stock = binding.etStock.text.toString().toInt(),
                url = imageUrl,
                idCategoria = 0,
                activo = true
            )

            val result = withContext(Dispatchers.IO) {
                try {
                    UiState.Success(ProductoServicio.guardarProducto(producto))
                } catch (e: Exception) {
                    UiState.Error(e.message.orEmpty())
                }
            }

            binding.progressBar.isVisible = false
            binding.tvEstadoSubida.isVisible = false

            when (result) {
                is UiState.Error -> mostrarError(result.message)
                is UiState.Success -> {
                    if (result.data.isSuccess) {
                        Toast.makeText(this@OperacionProductoActivity, "✅ Producto guardado", Toast.LENGTH_SHORT).show()
                        limpiarCampos()
                        binding.etDescripcion.requestFocus()
                        productoId = 0
                        MainActivity.existeCambio = true
                    } else {
                        mostrarError(result.data.exceptionOrNull()?.message ?: "Error desconocido")
                    }
                }
            }
        } catch (e: Exception) {
            binding.progressBar.isVisible = false
            binding.tvEstadoSubida.isVisible = false
            mostrarError("❌ Error al subir imagen: ${e.message}")
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
        selectedImageUri = null
        imageUrl = ""
        binding.ivPreview.isVisible = false
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