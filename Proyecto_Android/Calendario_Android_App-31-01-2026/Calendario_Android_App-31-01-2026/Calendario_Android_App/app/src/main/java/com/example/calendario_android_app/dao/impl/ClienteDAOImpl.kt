package com.example.calendario_android_app.dao.impl

import com.example.calendario_android_app.dao.ClienteDAO
import com.example.calendario_android_app.model.Cliente
import com.example.calendario_android_app.util.DatabaseConnection
import java.sql.SQLException

/**
 * Implementación de ClienteDAO que gestiona operaciones de base de datos para clientes.
 * Utiliza JDBC para interactuar con la base de datos SQL.
 */
class ClienteDAOImpl : ClienteDAO {
    /**
     * Obtiene un cliente de la base de datos por su ID.
     * Ejecuta una consulta SQL y mapea el resultado a un objeto Cliente.
     */
    override fun obtenerCliente(id_cliente: Int): Cliente? {
        val connection = DatabaseConnection.getConnection() ?: return null
        var cliente: Cliente? = null

        try {
            // Consulta SQL para obtener el cliente
            val query = "SELECT * FROM clientes WHERE id_cliente = ?"
            val statement = connection.prepareStatement(query)
            statement.setInt(1, id_cliente)
            
            val resultSet = statement.executeQuery()
            
            // Mapeo del resultado a objeto Cliente
            if (resultSet.next()) {
                cliente = Cliente(
                    id_cliente = resultSet.getInt("id_cliente"),
                    nombre = resultSet.getString("nombre"),
                    apellidos = resultSet.getString("apellidos"),
                    creado_el = resultSet.getTimestamp("creado_el"),
                    actualizado_el = resultSet.getTimestamp("actualizado_el")
                )
            }
            resultSet.close()
            statement.close()
            connection.close()
        } catch (e: SQLException) {
            android.util.Log.e("ClienteDAOImpl", "SQL Error", e)
            e.printStackTrace()
        }
        return cliente
    }

    /**
     * Crea un nuevo cliente en la base de datos.
     * Utiliza NOW() para establecer automáticamente las fechas de creación y actualización.
     * @return ID del cliente recién creado o null si falla
     */
    override fun crearCliente(nombre: String, apellidos: String): Int? {
        val connection = DatabaseConnection.getConnection() ?: return null
        var newId: Int? = null

        try {
            // Inserción con timestamps automáticos
            val query = "INSERT INTO clientes (nombre, apellidos, creado_el, actualizado_el) VALUES (?, ?, NOW(), NOW())"
            val statement = connection.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)
            statement.setString(1, nombre)
            statement.setString(2, apellidos)
            
            val affectedRows = statement.executeUpdate()
            
            // Recuperación del ID generado automáticamente
            if (affectedRows > 0) {
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    newId = generatedKeys.getInt(1)
                }
                generatedKeys.close()
            }
            statement.close()
            connection.close()
        } catch (e: SQLException) {
            android.util.Log.e("ClienteDAOImpl", "SQL Error", e)
            e.printStackTrace()
        }
        return newId
    }
}