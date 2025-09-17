package com.example.arquiprimerparcial.presentacion.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.arquiprimerparcial.MainActivity
import com.example.arquiprimerparcial.R
import com.example.arquiprimerparcial.data.dao.ProductoDao
import com.example.arquiprimerparcial.data.model.ProductoModel
import com.example.arquiprimerparcial.databinding.ActivityOperacionProductoBinding
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.presentacion.common.makeCall
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class OperacionProductoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOperacionProductoBinding
    private var _id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOperacionProductoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()

        if (intent.extras != null)
            obtenerProducto()
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
            if (binding.etDescripcion.text.toString().trim().isEmpty() ||
                binding.etCodigoBarra.text.toString().trim().isEmpty() ||
                binding.etPrecio.text.toString().trim().isEmpty()
            ) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("ADVERTENCIA")
                    .setMessage("Debe llenar todos los campos")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            grabar(
                ProductoModel(
                    id = _id,
                    descripcion = binding.etDescripcion.text.toString(),
                    codigobarra = binding.etCodigoBarra.text.toString(),
                    precio = binding.etPrecio.text.toString().toDouble()
                )
            )
        }
    }

    private fun obtenerProducto() {
        _id = intent.extras?.getInt("id", 0) ?: 0
        binding.etDescripcion.setText(intent.extras?.getString("descripcion"))
        binding.etCodigoBarra.setText(intent.extras?.getString("codigobarra"))
        binding.etPrecio.setText(intent.extras?.getDouble("precio").toString())
    }

    private fun grabar(producto: ProductoModel) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall { ProductoDao.grabar(producto) }.let {
            when (it) {
                is UiState.Error -> {
                    binding.progressBar.isVisible = false
                    AlertDialog.Builder(this@OperacionProductoActivity)
                        .setTitle("ERROR")
                        .setMessage(it.message)
                        .setPositiveButton("OK", null)
                        .show()
                }

                is UiState.Success -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(this@OperacionProductoActivity, "Datos grabados", Toast.LENGTH_SHORT).show()
                    clearAllEditTexts(binding.root)
                    binding.etDescripcion.requestFocus()
                    _id = 0
                    MainActivity.existeCambio = true
                }
            }
        }
    }
    fun clearAllEditTexts(view: View) {
        if (view is EditText) {
            view.text?.clear()
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                clearAllEditTexts(view.getChildAt(i))
            }
        }
    }
}