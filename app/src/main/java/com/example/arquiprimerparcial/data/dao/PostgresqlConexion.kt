package com.example.arquiprimerparcial.data.dao
import android.util.Log
import java.sql.Connection
import java.sql.DriverManager

object PostgresqlConexion {
    fun getConexion(): Connection {
        try {
            Log.d("DB_CONNECTION", "Intentando conectar...")
            Class.forName("org.postgresql.Driver")
            val connection = DriverManager.getConnection(
                "jdbc:postgresql://10.0.2.2:5433/tienda-emprendedor",
                "postgres",
                "123456"
            )
            Log.d("DB_CONNECTION", "Conexión exitosa!")
            return connection
        } catch (e: Exception) {
            Log.e("DB_CONNECTION", "Error: ${e.message}")
            throw e
        }
    }
}