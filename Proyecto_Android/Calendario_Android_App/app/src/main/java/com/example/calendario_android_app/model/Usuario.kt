package com.example.calendario_android_app.model

data class Usuario(val id_usuario: Int,
                   val id_cliente: String,
                   val correo: String,
                   val password_hash: String,
                   val rol: String)
