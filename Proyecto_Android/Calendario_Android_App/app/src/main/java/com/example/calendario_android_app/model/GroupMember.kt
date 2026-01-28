package com.example.calendario_android_app.model

data class GroupMember(
    val email: String,
    val userId: Int? = null, // null if user not found yet
    val userName: String? = null, // Display name from Cliente
    val role: String = "miembro" // miembro, editor, admin
)
