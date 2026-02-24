package com.example.calendario_android_app.dao.impl

import com.example.calendario_android_app.dao.UsuarioDAO
import com.example.calendario_android_app.model.Usuario
import com.example.calendario_android_app.util.DatabaseConnection
import com.example.calendario_android_app.util.HashUtils
import java.sql.SQLException


class UsuarioDAOImpl : UsuarioDAO {
    
    override fun loguearUsuario(correo: String, contrasena: String): Usuario? {
        val connection = DatabaseConnection.getConnection() ?: throw SQLException("No se pudo conectar a la base de datos")
        
        return connection.use { conn ->
            val query = "SELECT * FROM usuarios WHERE correo = ? AND password_hash = ?"
            conn.prepareStatement(query).use { statement ->
                statement.setString(1, correo)
                statement.setString(2, HashUtils.hashPassword(contrasena))
                
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        Usuario(
                            id_usuario = resultSet.getInt("id_usuario"),
                            id_cliente = resultSet.getString("id_cliente"),
                            correo = resultSet.getString("correo"),
                            password_hash = resultSet.getString("password_hash"),
                            rol = resultSet.getString("rol")
                        )
                    } else {
                        null
                    }
                }
            }
        }
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
            android.util.Log.e("UsuarioDAOImpl", "Error al comprobar correo", e)
            e.printStackTrace()
        }
        return existe
    }

    override fun crearUsuario(correo: String, contrasena: String, idCliente: Int): String? {
        val connection = DatabaseConnection.getConnection() ?: return "Error de conexión"
        var errorMessage: String? = null
        try {
            // El rol por defecto es 'usuario'.
            val query = "INSERT INTO usuarios (id_cliente, correo, password_hash, rol) VALUES (?, ?, ?, 'usuario')"
            val statement = connection.prepareStatement(query)
            statement.setInt(1, idCliente)
            statement.setString(2, correo)
            statement.setString(3, HashUtils.hashPassword(contrasena))
            
            val affectedRows = statement.executeUpdate()
            if (affectedRows <= 0) {
                 errorMessage = "No se pudo insertar el usuario"
            }
            statement.close()
            connection.close()
        } catch (e: SQLException) {
            android.util.Log.e("UsuarioDAOImpl", "Error al crear usuario", e)
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
    
    override suspend fun getUserById(idUsuario: Int): Usuario? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val connection = DatabaseConnection.getConnection()
        var usuario: Usuario? = null
        try {
            connection?.let { conn ->
                val query = "SELECT * FROM usuarios WHERE id_usuario = ?"
                val statement = conn.prepareStatement(query)
                statement.setInt(1, idUsuario)
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

    override suspend fun getUserInfo(idUsuario: Int): Pair<String, String>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val connection = DatabaseConnection.getConnection()
        var userInfo: Pair<String, String>? = null
        try {
            connection?.let { conn ->
                val query = """
                    SELECT u.correo, c.nombre 
                    FROM usuarios u
                    LEFT JOIN clientes c ON u.id_cliente = c.id_cliente
                    WHERE u.id_usuario = ?
                """
                val statement = conn.prepareStatement(query)
                statement.setInt(1, idUsuario)
                val rs = statement.executeQuery()
                if (rs.next()) {
                    val email = rs.getString("correo")
                    val name = rs.getString("nombre") ?: "Usuario"
                    userInfo = Pair(email, name)
                }
                rs.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        userInfo
    }
    
    override suspend fun updatePassword(idUsuario: Int, newPassword: String): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val connection = DatabaseConnection.getConnection()
        var updated = false
        try {
            connection?.let { conn ->
                val query = "UPDATE usuarios SET password_hash = ? WHERE id_usuario = ?"
                val statement = conn.prepareStatement(query)
                statement.setString(1, HashUtils.hashPassword(newPassword))
                statement.setInt(2, idUsuario)
                val affectedRows = statement.executeUpdate()
                updated = affectedRows > 0
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updated
    }
}



