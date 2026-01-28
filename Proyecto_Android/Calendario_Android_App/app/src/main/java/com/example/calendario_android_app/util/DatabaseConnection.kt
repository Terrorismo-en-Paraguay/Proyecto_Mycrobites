package com.example.calendario_android_app.util

import java.sql.Connection
import java.sql.DriverManager

object DatabaseConnection {
    // 10.0.2.2 is the special alias to your host loopback interface (i.e., localhost on your development machine)
    private const val URL = "jdbc:mariadb://vnlsjy.h.filess.io:3305/calendario_app_solutionso"
    private const val USER = "calendario_app_solutionso"
    private const val PASSWORD = "e13dac639f08cb4fed987a36e7b8e30e95cad171"

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