package com.example.calendario_android_app.model

import java.sql.Timestamp

/**
 * Modelo de datos que representa un cliente en el sistema.
 * Los clientes son personas físicas que pueden tener una cuenta de usuario asociada.
 * 
 * @property id_cliente Identificador único del cliente
 * @property nombre Nombre del cliente
 * @property apellidos Apellidos del cliente
 * @property creado_el Fecha y hora de creación del registro
 * @property actualizado_el Fecha y hora de la última actualización
 */
data class Cliente(
    val id_cliente: Int,
    val nombre: String,
    val apellidos: String,
    val creado_el: Timestamp,
    val actualizado_el: Timestamp
)
