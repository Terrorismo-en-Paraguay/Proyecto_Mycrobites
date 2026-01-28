package com.example.calendario_android_app.dao.impl

import com.example.calendario_android_app.dao.UsuarioDAO
import com.example.calendario_android_app.model.Usuario
import com.example.calendario_android_app.util.DatabaseConnection
import java.sql.SQLException

class UsuarioDAOImpl : UsuarioDAO {
    override fun loguearUsuario(correo: String, contrasena: String): Usuario? {
        val connection = DatabaseConnection.getConnection() ?: return null
        
        var usuario: Usuario? = null
        try {
            // Note: In production passwords should be hashed. Keeping as string comp for now as per user request flow.
            // Assuming table is 'usuarios' or similar. 
            val query = "SELECT * FROM usuarios WHERE correo = ? AND password_hash = ?"
            val statement = connection.prepareStatement(query)
            statement.setString(1, correo)
            statement.setString(2, contrasena)
            
            val resultSet = statement.executeQuery()
            
            if (resultSet.next()) {
                usuario = Usuario(
                    id_usuario = resultSet.getInt("id_usuario"),
                    id_cliente = resultSet.getString("id_cliente"), // Assuming String based on Model, verify if Int in DB
                    correo = resultSet.getString("correo"),
                    password_hash = resultSet.getString("password_hash"),
                    rol = resultSet.getString("rol")
                )
            }
            resultSet.close()
            statement.close()
            connection.close()
        } catch (e: SQLException) {
            android.util.Log.e("UsuarioDAOImpl", "SQL Error", e)
            e.printStackTrace()
        }
        return usuario
    }

    override fun existeCorreo(correo: String): Boolean {
        val connection = DatabaseConnection.getConnection() ?: return false
        var existe = false
        try {
            val query = "SELECT 1 FROM usuarios WHERE correo = ?"
            val statement = connection.prepareStatement(query)
            statement.setString(1, correo)
            val resultSet = statement.executeQuery()
            if (resultSet.next()) {
                existe = true
            }
            resultSet.close()
            statement.close()
            connection.close()
        } catch (e: SQLException) {
            android.util.Log.e("UsuarioDAOImpl", "SQL Error checking email", e)
            e.printStackTrace()
            // In case of error, returning false might allow duplicate attempts, but safer to block? 
            // Standard is false or throw. Proceeding with false for now.
        }
        return existe
    }

    override fun crearUsuario(correo: String, contrasena: String, idCliente: Int): String? {
        val connection = DatabaseConnection.getConnection() ?: return "Error de conexión"
        var errorMessage: String? = null
        try {
            // Defaulting role to "usuario"
            // Ensure id_cliente is handled correctly. 
            val query = "INSERT INTO usuarios (id_cliente, correo, password_hash, rol) VALUES (?, ?, ?, 'usuario')"
            val statement = connection.prepareStatement(query)
            statement.setInt(1, idCliente)
            statement.setString(2, correo)
            statement.setString(3, contrasena)
            
            val affectedRows = statement.executeUpdate()
            if (affectedRows <= 0) {
                 errorMessage = "No rows affected"
            }
            statement.close()
            connection.close()
        } catch (e: SQLException) {
            android.util.Log.e("UsuarioDAOImpl", "SQL Error creating user", e)
            e.printStackTrace()
            errorMessage = e.message
        }
        return errorMessage
    }

    override fun getUsuarioIdByEmail(correo: String): Int? {
        val connection = DatabaseConnection.getConnection() ?: return null
        var id: Int? = null
        try {
            val query = "SELECT id_usuario FROM usuarios WHERE correo = ?"
            val statement = connection.prepareStatement(query)
            statement.setString(1, correo)
            val rs = statement.executeQuery()
            if (rs.next()) {
                id = rs.getInt("id_usuario")
            }
            rs.close()
            statement.close()
            connection.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return id
    }

    override suspend fun getUserByEmail(correo: String): Usuario? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val connection = DatabaseConnection.getConnection()
        var usuario: Usuario? = null
        try {
            connection?.let { conn ->
                val query = "SELECT * FROM usuarios WHERE correo = ?"
                val statement = conn.prepareStatement(query)
                statement.setString(1, correo)
                val rs = statement.executeQuery()
                if (rs.next()) {
                    usuario = Usuario(
                        id_usuario = rs.getInt("id_usuario"),
                        id_cliente = rs.getString("id_cliente"),
                        correo = rs.getString("correo"),
                        password_hash = rs.getString("password_hash"),
                        rol = rs.getString("rol")
                    )
                }
                rs.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        usuario
    }
}