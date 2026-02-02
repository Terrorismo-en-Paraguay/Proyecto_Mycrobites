package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Cliente

/**
 * Interfaz que define las operaciones de acceso a datos para la entidad Cliente.
 * Los clientes representan personas físicas que pueden tener cuentas de usuario.
 */
interface ClienteDAO {
    /**
     * Obtiene un cliente por su identificador.
     * @param id_cliente ID del cliente a buscar
     * @return Cliente encontrado o null si no existe
     */
    fun obtenerCliente(id_cliente: Int): Cliente?
    
    /**
     * Crea un nuevo cliente en la base de datos.
     * @param nombre Nombre del cliente
     * @param apellidos Apellidos del cliente
     * @return ID del cliente creado o null si falla la operación
     */
    fun crearCliente(nombre: String, apellidos: String): Int?
}