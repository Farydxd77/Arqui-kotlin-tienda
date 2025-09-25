package com.example.arquiprimerparcial.api

import android.util.Log
import com.example.arquiprimerparcial.negocio.servicio.CategoriaServicio
import com.example.arquiprimerparcial.negocio.servicio.ProductoServicio
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class ServidorApi {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val gson = Gson()
    private val TAG = "ServidorApi"

    fun iniciar(puerto: Int = 8081) {  // Cambiado default de 8080 a 8081
        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = ServerSocket(puerto)
                isRunning = true
                Log.d(TAG, "✅ Servidor iniciado en puerto $puerto")

                while (isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept()
                        clientSocket?.let { handleClient(it) }
                    } catch (e: Exception) {
                        if (isRunning) {
                            Log.e(TAG, "Error aceptando cliente: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error iniciando servidor: ${e.message}")
            }
        }
    }

    fun detener() {
        isRunning = false
        serverSocket?.close()
        Log.d(TAG, "🛑 Servidor detenido")
    }

    private fun handleClient(socket: Socket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                val output = PrintWriter(socket.getOutputStream(), true)

                // Leer la primera línea de la petición HTTP
                val requestLine = input.readLine() ?: return@launch
                val parts = requestLine.split(" ")
                if (parts.size < 2) return@launch

                val method = parts[0]
                val path = parts[1]

                // Leer headers (necesario para completar la petición)
                while (input.readLine()?.isNotEmpty() == true) {
                    // Consumir headers
                }

                // Procesar la petición
                if (method == "GET") {
                    val response = procesarGet(path)
                    output.println("HTTP/1.1 200 OK")
                    output.println("Content-Type: application/json; charset=UTF-8")
                    output.println("Access-Control-Allow-Origin: *")
                    output.println("Connection: close")
                    output.println()
                    output.println(response)
                }

                output.flush()
                socket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error manejando cliente: ${e.message}")
            }
        }
    }

    private fun procesarGet(path: String): String {
        return try {
            when {
                path == "/" -> {
                    """
                    {
                        "message": "🚀 API REST - Catálogo de Productos",
                        "endpoints": [
                            "/api/productos",
                            "/api/productos/{id}",
                            "/api/productos/categoria/{id}",
                            "/api/categorias",
                            "/api/categorias/{id}",
                            "/health"
                        ]
                    }
                    """.trimIndent()
                }

                path == "/health" -> {
                    gson.toJson(mapOf(
                        "status" to "OK",
                        "timestamp" to System.currentTimeMillis()
                    ))
                }

                path == "/api/productos" -> {
                    val productos = ProductoServicio.obtenerProductos()
                    gson.toJson(mapOf(
                        "success" to true,
                        "data" to productos.map { producto ->
                            mapOf(
                                "id" to producto.id,
                                "nombre" to producto.nombre,
                                "descripcion" to producto.descripcion,
                                "url" to producto.url,
                                "precio" to producto.precio,
                                "stock" to producto.stock,
                                "activo" to producto.activo,
                                "id_categoria" to producto.idCategoria,
                                "categoria_nombre" to producto.nombreCategoria
                            )
                        }
                    ))
                }

                path.startsWith("/api/productos/categoria/") -> {
                    val id = path.substringAfterLast("/").toIntOrNull()
                    if (id != null) {
                        val productos = ProductoServicio.obtenerProductosPorCategoria(id)
                        gson.toJson(mapOf(
                            "success" to true,
                            "data" to productos.map { producto ->
                                mapOf(
                                    "id" to producto.id,
                                    "nombre" to producto.nombre,
                                    "descripcion" to producto.descripcion,
                                    "url" to producto.url,
                                    "precio" to producto.precio,
                                    "stock" to producto.stock,
                                    "activo" to producto.activo,
                                    "id_categoria" to producto.idCategoria,
                                    "categoria_nombre" to producto.nombreCategoria
                                )
                            }
                        ))
                    } else {
                        gson.toJson(mapOf("success" to false, "message" to "ID inválido"))
                    }
                }

                path.startsWith("/api/productos/") -> {
                    val id = path.substringAfterLast("/").toIntOrNull()
                    if (id != null) {
                        val producto = ProductoServicio.obtenerProductoPorId(id)
                        if (producto != null) {
                            gson.toJson(mapOf(
                                "success" to true,
                                "data" to mapOf(
                                    "id" to producto.id,
                                    "nombre" to producto.nombre,
                                    "descripcion" to producto.descripcion,
                                    "url" to producto.url,
                                    "precio" to producto.precio,
                                    "stock" to producto.stock,
                                    "activo" to producto.activo,
                                    "id_categoria" to producto.idCategoria,
                                    "categoria_nombre" to producto.nombreCategoria
                                )
                            ))
                        } else {
                            gson.toJson(mapOf("success" to false, "message" to "Producto no encontrado"))
                        }
                    } else {
                        gson.toJson(mapOf("success" to false, "message" to "ID inválido"))
                    }
                }

                path == "/api/categorias" -> {
                    val categorias = CategoriaServicio.obtenerCategorias()
                    gson.toJson(mapOf(
                        "success" to true,
                        "data" to categorias.map { categoria ->
                            mapOf(
                                "id" to categoria.id,
                                "nombre" to categoria.nombre,
                                "descripcion" to categoria.descripcion
                            )
                        }
                    ))
                }

                path.startsWith("/api/categorias/") -> {
                    val id = path.substringAfterLast("/").toIntOrNull()
                    if (id != null) {
                        val categoria = CategoriaServicio.obtenerCategoriaPorId(id)
                        if (categoria != null) {
                            gson.toJson(mapOf(
                                "success" to true,
                                "data" to mapOf(
                                    "id" to categoria.id,
                                    "nombre" to categoria.nombre,
                                    "descripcion" to categoria.descripcion
                                )
                            ))
                        } else {
                            gson.toJson(mapOf("success" to false, "message" to "Categoría no encontrada"))
                        }
                    } else {
                        gson.toJson(mapOf("success" to false, "message" to "ID inválido"))
                    }
                }

                else -> {
                    gson.toJson(mapOf("success" to false, "message" to "Endpoint no encontrado"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando petición: ${e.message}")
            gson.toJson(mapOf("success" to false, "message" to "Error: ${e.message}"))
        }
    }
}