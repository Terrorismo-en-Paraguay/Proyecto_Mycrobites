package com.example.calendario_android_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendario_android_app.dao.impl.ClienteDAOImpl
import com.example.calendario_android_app.dao.impl.UsuarioDAOImpl
import com.example.calendario_android_app.dao.impl.GrupoDAOImpl
import com.example.calendario_android_app.model.Usuario
import com.example.calendario_android_app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {

    private val usuarioDAO = UsuarioDAOImpl()
    private val clienteDAO = ClienteDAOImpl()
    private val grupoDAO = GrupoDAOImpl()

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(correo: String, contrasena: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                // Background thread for DB access
                val usuario = usuarioDAO.loguearUsuario(correo, contrasena)
                if (usuario != null) {
                    // Assuming id_cliente is stored as String in Usuario model but int in DB/Cliente model
                    // Need to handle conversion or ensure model consistency. 
                    // Based on Usuario.kt: val id_cliente: String
                    // Based on ClienteDAO.kt: obtenerCliente(id_cliente: Int)
                    // Trying to parse Int.
                    val clienteId = usuario.id_cliente.toIntOrNull()
                    if (clienteId != null) {
                        val cliente = clienteDAO.obtenerCliente(clienteId)
                        val nombreCompleto = if (cliente != null) "${cliente.nombre} ${cliente.apellidos}" else null
                        Pair(usuario, nombreCompleto)
                    } else {
                        Pair(usuario, null)
                    }
                } else {
                    null
                }
            }
            
            if (result != null) {
                val (usuario, nombreCliente) = result
                _loginResult.value = LoginResult.Success(usuario, nombreCliente)
            } else {
                _loginResult.value = LoginResult.Error(R.string.error_credentials)
            }
        }
    }

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    fun register(nombre: String, apellido: String, correo: String, contrasena: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                if (usuarioDAO.existeCorreo(correo)) {
                    // Need context for R.string? ViewModel shouldn't hold context. 
                    // To keep it simple for debugging, hardcoding or we'd need a ResourceProvider.
                    // For now, let's assume we can pass a raw string or handle resource logic separately.
                    // But wait, the previous code used resources. 
                    // Let's settle on returning Strings for everything now to support the dynamic SQL error.
                    RegisterResult.Error("El correo electrónico ya está registrado")
                } else {
                    val idCliente = clienteDAO.crearCliente(nombre, apellido)
                    if (idCliente != null) {
                        val error = usuarioDAO.crearUsuario(correo, contrasena, idCliente)
                        if (error == null) {
                            // Fetch ID of created user to create group
                            val userId = usuarioDAO.getUsuarioIdByEmail(correo)
                            if (userId != null) {
                                grupoDAO.createDefaultPersonalGroup(userId)
                            }
                            RegisterResult.Success
                        } else {
                            RegisterResult.Error(error) // Pass dynamic error string
                        }
                    } else {
                        RegisterResult.Error("Error al crear el cliente")
                    }
                }
            }
            _registerResult.value = result
        }
    }
}

sealed class LoginResult {
    data class Success(val usuario: Usuario, val nombreCliente: String?) : LoginResult()
    data class Error(val messageResId: Int) : LoginResult()
}

sealed class RegisterResult {
    object Success : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}