package com.example.arquiprimerparcial.data.conexion
import android.util.Log
import java.sql.Connection
import java.sql.DriverManager

object PostgresqlConexion {
    private const val URL = "jdbc:postgresql://10.0.2.2:5433/tienda-emprendedor"
    private const val USER = "postgres"
    private const val PASSWORD = "123456"

    fun getConexion(): Connection {
        try {
            Log.d("DB_CONNECTION", "Intentando conectar...")
            Class.forName("org.postgresql.Driver")
            val connection = DriverManager.getConnection(URL, USER, PASSWORD)
            Log.d("DB_CONNECTION", "Conexión exitosa!")
            return connection
        } catch (e: Exception) {
            Log.e("DB_CONNECTION", "Error: ${e.message}")
            throw e
        }
    }
}