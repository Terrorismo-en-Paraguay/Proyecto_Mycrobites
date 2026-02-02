package com.example.calendario_android_app.util

import java.sql.Connection
import java.sql.DriverManager

/**
 * Singleton que gestiona la conexión a la base de datos MariaDB.
 * Proporciona una conexión JDBC reutilizable para todos los DAOs.
 */
object DatabaseConnection {
    // Configuración de conexión a la base de datos remota
    private const val URL = "jdbc:mariadb://vnlsjy.h.filess.io:3305/calendario_app_solutionso"
    private const val USER = "calendario_app_solutionso"
    private const val PASSWORD = "e13dac639f08cb4fed987a36e7b8e30e95cad171"

    /**
     * Establece y retorna una conexión a la base de datos.
     * Carga el driver de MariaDB automáticamente.
     * @return Conexión activa o null si falla
     */
    fun getConnection(): Connection? {
        return try {
            Class.forName("org.mariadb.jdbc.Driver")
            DriverManager.getConnection(URL, USER, PASSWORD)
        } catch (e: Exception) {
            android.util.Log.e("DatabaseConnection", "Error connecting to DB", e)
            e.printStackTrace()
            null
        }
    }
}