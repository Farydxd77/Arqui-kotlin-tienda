package com.example.arquiprimerparcial.presentacion.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arquiprimerparcial.R
import com.example.arquiprimerparcial.databinding.ActivityMainBinding
import com.example.arquiprimerparcial.negocio.servicio.ProductoServicio
import com.example.arquiprimerparcial.negocio.servicio.ReporteServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.utils.PdfGenerator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ProductoAdapterIntegrado

    private val productoServicio: ProductoServicio = ProductoServicio()
    private val reporteServicio: ReporteServicio = ReporteServicio()

    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        verificarPermisos()
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

    private fun verificarPermisos() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Permisos concedidos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "⚠️ Se necesitan permisos para exportar PDFs", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initAdapter() {
        adapter = ProductoAdapterIntegrado(
            productoServicio = productoServicio,
            onClickEditar = { productoMap ->
                val id = productoMap["id"] as Int
                val nombre = productoMap["nombre"] as String
                val descripcion = productoMap["descripcion"] as String
                val url = productoMap["url"] as String
                val precio = productoMap["precio"] as Double
                val stock = productoMap["stock"] as Int
                val idCategoria = productoMap["idCategoria"] as Int

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
            onClickEliminar = { productoMap ->
                val id = productoMap["id"] as Int
                val nombre = productoMap["nombre"] as String

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
        binding.includeToolbar.ibAccion.setOnClickListener {
            startActivity(Intent(this, OperacionProductoActivity::class.java))
        }

        binding.btnGestionarProductos.setOnClickListener {
            startActivity(Intent(this, ProductoActivity::class.java))
        }

        binding.btnCategorias.setOnClickListener {
            startActivity(Intent(this, CategoriaActivity::class.java))
        }

        binding.btnCrearPedido.setOnClickListener {
            startActivity(Intent(this, CrearPedidoActivity::class.java))
        }

        binding.btnVerPedidos.setOnClickListener {
            startActivity(Intent(this, HistorialPedidosActivity::class.java))
        }

        // En initListener() de MainActivity.kt
        binding.btnExportarReportes.setOnClickListener {
            startActivity(Intent(this, ReportesActivity::class.java))
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

    private fun mostrarDialogoReportes() {
        val opciones = arrayOf(
            "📅 Reporte del Día",
            "📆 Reporte de la Semana Actual",
            "📊 Reporte del Mes Actual",
            "🗓️ Últimos 7 Días",
            "📈 Últimos 30 Días",
            "📉 Mes Pasado"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("📊 Generar Reporte en PDF")
            .setMessage("Seleccione el período del reporte:")
            .setItems(opciones) { _, which ->
                val tipoReporte = when (which) {
                    0 -> "HOY"
                    1 -> "SEMANA_ACTUAL"
                    2 -> "MES_ACTUAL"
                    3 -> "ULTIMOS_7_DIAS"
                    4 -> "ULTIMOS_30_DIAS"
                    5 -> "MES_PASADO"
                    else -> "HOY"
                }
                generarReportePDF(tipoReporte)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun generarReportePDF(tipoReporte: String) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        val result = withContext(Dispatchers.IO) {
            try {
                val datosReporte = reporteServicio.generarDatosReporte(tipoReporte)
                val pdfGenerator = PdfGenerator(this@MainActivity)
                val rutaPdf = pdfGenerator.generarReportePDF(datosReporte)
                UiState.Success(rutaPdf)
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Error al generar el reporte")
            }
        }

        binding.progressBar.isVisible = false

        when (result) {
            is UiState.Success -> {
                mostrarExitoReporte(result.data)
            }
            is UiState.Error -> {
                mostrarError(result.message)
            }
        }
    }

    private fun mostrarExitoReporte(rutaPdf: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reporte Generado Exitosamente")
            .setMessage("El reporte se ha generado y guardado en:\n\n📁 Descargas/\n📄 ${File(rutaPdf).name}")
            .setPositiveButton("Abrir PDF") { _, _ ->
                abrirPDF(rutaPdf)
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun abrirPDF(rutaPdf: String) {
        val file = File(rutaPdf)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            file
        )

        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se encontró una app para abrir PDFs", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarProductos(filtro: String) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        val result = withContext(Dispatchers.IO) {
            try {
                UiState.Success(productoServicio.listarProductosPrimitivos(filtro))
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
                UiState.Success(productoServicio.desactivarProducto(id))
            } catch (e: Exception) {
                UiState.Error(e.message.orEmpty())
            }
        }

        binding.progressBar.isVisible = false

        when (result) {
            is UiState.Error -> mostrarError(result.message)
            is UiState.Success -> {
                if (result.data.isSuccess) {
                    Toast.makeText(this@MainActivity, "✅ Registro eliminado", Toast.LENGTH_SHORT).show()
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

    private class ProductoAdapterIntegrado(
        private val productoServicio: ProductoServicio,
        private val onClickEditar: (Map<String, Any>) -> Unit,
        private val onClickEliminar: (Map<String, Any>) -> Unit
    ) : RecyclerView.Adapter<ProductoAdapterIntegrado.ProductoViewHolder>() {

        private var lista = emptyList<Map<String, Any>>()

        inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvTitulo: TextView = itemView.findViewById(R.id.tv_titulo)
            private val tvNombre: TextView = itemView.findViewById(R.id.tv_nombre)
            private val tvCategoria: TextView = itemView.findViewById(R.id.tv_categoria)
            private val tvStock: TextView = itemView.findViewById(R.id.tv_stock)
            private val tvPrecio: TextView = itemView.findViewById(R.id.tv_precio)
            private val ibEditar: ImageButton = itemView.findViewById(R.id.ib_editar)
            private val ibEliminar: ImageButton = itemView.findViewById(R.id.ib_eliminar)

            fun enlazar(productoMap: Map<String, Any>) {
                val nombre = productoMap["nombre"] as String
                val precio = productoMap["precio"] as Double
                val stock = productoMap["stock"] as Int
                val idCategoria = productoMap["idCategoria"] as Int
                val categoriaNombre = productoMap["categoriaNombre"] as String

                tvTitulo.text = nombre
                tvNombre.text = nombre

                tvCategoria.text = when {
                    categoriaNombre.isNotEmpty() -> "🏷️ $categoriaNombre"
                    idCategoria > 0 -> "🏷️ Categoría ID: $idCategoria"
                    else -> "🏷️ Sin categoría"
                }
                tvStock.text = "Stock: $stock"
                tvPrecio.text = "S/ ${productoServicio.formatearPrecio(precio)}"

                when {
                    productoServicio.sinStockPrimitivo(productoMap) -> {
                        tvStock.setTextColor(Color.RED)
                        itemView.alpha = 0.6f
                    }
                    productoServicio.stockBajoPrimitivo(productoMap) -> {
                        tvStock.setTextColor(Color.parseColor("#FF9800"))
                        itemView.alpha = 0.8f
                    }
                    else -> {
                        tvStock.setTextColor(Color.parseColor("#4CAF50"))
                        itemView.alpha = 1.0f
                    }
                }

                ibEditar.setOnClickListener { onClickEditar(productoMap) }
                ibEliminar.setOnClickListener { onClickEliminar(productoMap) }
                itemView.setOnClickListener { onClickEditar(productoMap) }
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

        fun setList(listaProducto: List<Map<String, Any>>) {
            this.lista = listaProducto
            notifyDataSetChanged()
        }
    }
}