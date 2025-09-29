package com.example.arquiprimerparcial.presentacion.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.arquiprimerparcial.R
import com.example.arquiprimerparcial.databinding.ActivityReportesBinding
import com.example.arquiprimerparcial.negocio.servicio.ReporteServicio
import com.example.arquiprimerparcial.presentacion.common.UiState
import com.example.arquiprimerparcial.utils.PdfGenerator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ReportesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportesBinding
    private val reporteServicio: ReporteServicio = ReporteServicio()
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        verificarPermisos()
        initListeners()
    }

    private fun initToolbar() {
        binding.includeToolbar.toolbar.apply {
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            subtitle = "Generar Reportes PDF"
            navigationIcon = AppCompatResources.getDrawable(
                this@ReportesActivity,
                R.drawable.baseline_arrow_back_24
            )
            setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        binding.includeToolbar.ibAccion.isVisible = false
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
                Toast.makeText(
                    this,
                    "⚠️ Se necesitan permisos para exportar PDFs",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun initListeners() {
        // Reportes Rápidos
        binding.btnReporteHoy.setOnClickListener {
            generarReportePDF("HOY")
        }

        binding.btnReporteSemana.setOnClickListener {
            generarReportePDF("SEMANA_ACTUAL")
        }

        binding.btnReporteMes.setOnClickListener {
            generarReportePDF("MES_ACTUAL")
        }

        // Reportes Personalizados
        binding.btnReporte7dias.setOnClickListener {
            generarReportePDF("ULTIMOS_7_DIAS")
        }

        binding.btnReporte30dias.setOnClickListener {
            generarReportePDF("ULTIMOS_30_DIAS")
        }

        binding.btnReporteMesPasado.setOnClickListener {
            generarReportePDF("MES_PASADO")
        }
    }

    private fun generarReportePDF(tipoReporte: String) = lifecycleScope.launch {
        binding.progressBar.isVisible = true

        val result = withContext(Dispatchers.IO) {
            try {
                val datosReporte = reporteServicio.generarDatosReporte(tipoReporte)
                val pdfGenerator = PdfGenerator(this@ReportesActivity)
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
        val nombreArchivo = File(rutaPdf).name

        MaterialAlertDialogBuilder(this)
            .setTitle("✅ Reporte Generado Exitosamente")
            .setMessage("El reporte se ha generado y guardado en:\n\n📁 Carpeta: Descargas\n📄 Archivo: $nombreArchivo")
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
            Toast.makeText(
                this,
                "❌ No se encontró una app para abrir PDFs",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun mostrarError(mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("❌ Error")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }
}