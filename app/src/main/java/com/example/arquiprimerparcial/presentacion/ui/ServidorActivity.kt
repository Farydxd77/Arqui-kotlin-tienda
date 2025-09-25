package com.example.arquiprimerparcial.presentacion.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arquiprimerparcial.api.ServidorApi
import com.example.arquiprimerparcial.databinding.ActivityServidorBinding
import java.net.Inet4Address
import java.net.NetworkInterface

class ServidorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServidorBinding
    private val servidor = ServidorApi()
    private var servidorIniciado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServidorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        initListeners()
    }

    private fun initUI() {
        val ip = obtenerIPLocal()
        val puerto = 8080

        binding.tvIpLocal.text = "http://$ip:$puerto"
        binding.tvEstado.text = "Servidor detenido"
        binding.btnIniciar.text = "Iniciar Servidor"

        // Mostrar endpoints
        binding.tvEndpoints.text = """
            📋 Endpoints disponibles:
            
            • GET /api/productos
            • GET /api/productos/{id}
            • GET /api/productos/categoria/{id}
            • GET /api/categorias
            • GET /api/categorias/{id}
            • GET /health
        """.trimIndent()
    }

    private fun initListeners() {
        binding.btnIniciar.setOnClickListener {
            if (servidorIniciado) {
                detenerServidor()
            } else {
                iniciarServidor()
            }
        }

        binding.btnCopiarUrl.setOnClickListener {
            copiarAlPortapapeles(binding.tvIpLocal.text.toString())
        }

        binding.btnAbrirNav.setOnClickListener {
            val url = binding.tvIpLocal.text.toString()
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse(url)
            }
            startActivity(intent)
        }
    }

    private fun iniciarServidor() {
        try {
            servidor.iniciar(8080)
            servidorIniciado = true

            binding.tvEstado.text = "✅ Servidor funcionando"
            binding.tvEstado.setTextColor(getColor(android.R.color.holo_green_dark))
            binding.btnIniciar.text = "Detener Servidor"
            binding.cardInfo.setCardBackgroundColor(getColor(com.example.arquiprimerparcial.R.color.primaryLight))

            Toast.makeText(this, "Servidor iniciado en puerto 8080", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun detenerServidor() {
        servidor.detener()
        servidorIniciado = false

        binding.tvEstado.text = "🛑 Servidor detenido"
        binding.tvEstado.setTextColor(getColor(android.R.color.holo_red_dark))
        binding.btnIniciar.text = "Iniciar Servidor"
        binding.cardInfo.setCardBackgroundColor(getColor(com.example.arquiprimerparcial.R.color.grey))

        Toast.makeText(this, "Servidor detenido", Toast.LENGTH_SHORT).show()
    }

    private fun obtenerIPLocal(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val interfaz = interfaces.nextElement()
                val addresses = interfaz.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: "localhost"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "localhost"
    }

    private fun copiarAlPortapapeles(texto: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("URL Servidor", texto)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "URL copiada al portapapeles", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (servidorIniciado) {
            servidor.detener()
        }
    }
}