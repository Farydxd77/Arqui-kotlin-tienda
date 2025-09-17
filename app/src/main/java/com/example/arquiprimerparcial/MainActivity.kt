package com.example.arquiprimerparcial

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arquiprimerparcial.data.dao.ProductoDao
import com.example.arquiprimerparcial.data.model.ProductoModel
import com.example.arquiprimerparcial.databinding.ActivityMainBinding
import com.example.arquiprimerparcial.presentacion.adapter.ProductoAdapter
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.presentacion.common.makeCall
import com.example.arquiprimerparcial.presentacion.ui.OperacionProductoActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), ProductoAdapter.IOnClickListener {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()

        leerProducto("")
    }

    override fun onResume() {
        super.onResume()

        if (!existeCambio) return

        existeCambio = false
        leerProducto(binding.etBuscar.text.toString().trim())
    }

    private fun initListener() {
        binding.includeToolbar.ibAccion.setOnClickListener {
            startActivity(
                Intent(this, OperacionProductoActivity::class.java)
            )
        }

        binding.rvLista.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ProductoAdapter(this@MainActivity)
        }

        binding.tilBuscar.setEndIconOnClickListener {
            leerProducto(binding.etBuscar.text.toString().trim())
        }

        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if(binding.etBuscar.text.toString().trim().isEmpty()){
                    leerProducto("")
                    // Ocultar teclado nativo
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
                }
            }
        })
    }

    override fun clickEditar(producto: ProductoModel) {
        startActivity(
            Intent(this, OperacionProductoActivity::class.java).apply {
                putExtra("id", producto.id)
                putExtra("descripcion", producto.descripcion)
                putExtra("codigobarra", producto.codigobarra)
                putExtra("precio", producto.precio)
            }
        )
    }

    override fun clickEliminar(producto: ProductoModel) {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Eliminar")
            setMessage("¿Desea eliminar el registro: ${producto.descripcion}?")
            setCancelable(false)

            setNegativeButton("NO") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("SI") { dialog, _ ->
                eliminar(producto)
                leerProducto(binding.etBuscar.text.toString().trim())
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun leerProducto(dato: String) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall { ProductoDao.listar(dato) }.let { result ->
            binding.progressBar.isVisible = false

            when (result) {
                is UiState.Error -> {
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle("ERROR")
                        .setMessage(result.message)
                        .setPositiveButton("OK", null)
                        .show()
                }

                is UiState.Success -> {
                    (binding.rvLista.adapter as ProductoAdapter).setList(result.data)
                }
            }
        }
    }

    private fun eliminar(model: ProductoModel) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        makeCall { ProductoDao.eliminar(model) }.let { result ->
            binding.progressBar.isVisible = false

            when (result) {
                is UiState.Error -> {
                    // AlertDialog nativo
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("ERROR")
                        .setMessage(result.message)
                        .setPositiveButton("OK", null)
                        .show()
                }

                is UiState.Success -> {
                    // Toast nativo
                    Toast.makeText(this@MainActivity, "Registro eliminado", Toast.LENGTH_SHORT).show()
                    leerProducto(binding.etBuscar.text.toString().trim())
                }
            }
        }
    }

    companion object {
        var existeCambio = false
    }
}