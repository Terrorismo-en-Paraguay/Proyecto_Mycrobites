package com.example.calendario_android_app.model

import java.sql.Timestamp

data class Cliente(val id_cliente: Int,
                   val nombre: String,
                   val apellidos: String,
                   val creado_el : Timestamp,
                   val actualizado_el : Timestamp)
