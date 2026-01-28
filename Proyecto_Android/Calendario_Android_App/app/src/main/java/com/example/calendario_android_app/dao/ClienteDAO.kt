package com.example.calendario_android_app.dao

import com.example.calendario_android_app.model.Cliente

interface ClienteDAO {
    fun obtenerCliente(id_cliente: Int): Cliente?
    fun crearCliente(nombre: String, apellidos: String): Int?
}