package com.example.calendario_android_app.model

data class GroupMember(
    val email: String,
    val userId: Int? = null,
    val userName: String? = null,
    val role: String = "miembro"
)
