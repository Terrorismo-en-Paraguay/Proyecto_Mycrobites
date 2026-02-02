package com.example.calendario_android_app.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calendario_android_app.R

import androidx.fragment.app.viewModels
import com.example.calendario_android_app.viewmodel.AuthViewModel
import com.example.calendario_android_app.viewmodel.LoginResult
import com.example.calendario_android_app.service.NotificationManager
import com.google.android.material.snackbar.Snackbar

/**
 * Fragmento de inicio de sesión.
 * Permite al usuario autenticarse con correo y contraseña.
 * Funcionalidades:
 * - Toggle de visibilidad de contraseña
 * - Validación de campos
 * - Guardado de sesión en SharedPreferences
 * - Notificación por email al iniciar sesión
 * - Navegación a pantalla de registro
 */
class LoginFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()
    private var isPasswordVisible = false




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnEye = view.findViewById<ImageView>(R.id.btnEye)
        val btnLogin = view.findViewById<View>(R.id.btnLogin)
        val btnGoogle = view.findViewById<View>(R.id.btnGoogle)
        val tvRegister = view.findViewById<TextView>(R.id.tvRegister)
        val tvForgotPassword = view.findViewById<TextView>(R.id.tvForgotPassword)

        btnEye.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnEye.alpha = 1.0f // Opaco para indicar activo
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnEye.alpha = 0.5f // Atenuado
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                 Snackbar.make(view, getString(R.string.error_fill_all_fields), Snackbar.LENGTH_SHORT).show()
            }
        }
        
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is LoginResult.Success -> {
                    val displayName = result.nombreCliente ?: result.usuario.correo
                    
                    // Guardar sesión
                    com.example.calendario_android_app.utils.SessionManager.currentUser = result.usuario
                    com.example.calendario_android_app.utils.SessionManager.currentClientName = displayName

                    val message = getString(R.string.welcome_message, displayName)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_calendarFragment)

                    // Guardar persistencia de sesión
                    val sharedPrefs = requireActivity().getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                    sharedPrefs.edit().apply {
                        putBoolean("is_logged_in", true)
                        putInt("userId", result.usuario.id_usuario)
                        putString("username", displayName)
                        apply()
                    }

                    // Notificar usuario por email
                    context?.let { ctx ->
                        NotificationManager.notifyLoginSuccessful(
                            context = ctx,
                            userEmail = result.usuario.correo,
                            userName = displayName
                        )
                    }
                }
                is LoginResult.Error -> {
                    Snackbar.make(view, getString(result.messageResId), Snackbar.LENGTH_LONG).show()
                }
            }
        }

        btnGoogle.setOnClickListener {
            Toast.makeText(context, getString(R.string.google_clicked), Toast.LENGTH_SHORT).show()
        }

        tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(context, getString(R.string.forgot_password_clicked), Toast.LENGTH_SHORT).show()
        }
    }
}