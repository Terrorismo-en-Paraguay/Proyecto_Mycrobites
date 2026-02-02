package com.example.calendario_android_app.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calendario_android_app.R
import com.example.calendario_android_app.databinding.FragmentSettingsBinding
import com.example.calendario_android_app.utils.SessionManager

/**
 * Fragmento de configuración de usuario.
 * Permite al usuario:
 * - Ver su información de perfil
 * - Cambiar su contraseña
 * - Cerrar sesión
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserInfo()
        setupClickListeners()
    }

    /**
     * Carga la información del usuario desde SessionManager.
     */
    private fun setupUserInfo() {
        // Cargar datos desde SessionManager
        val userName = SessionManager.currentClientName ?: "Usuario"
        val userEmail = SessionManager.currentUser?.correo ?: "usuario@email.com"
        val initial = userName.firstOrNull()?.toString()?.uppercase() ?: "U"

        binding.tvUserName.text = userName
        binding.tvUserEmail.text = userEmail
        binding.tvAvatarInitial.text = initial
    }

    /**
     * Configura los listeners de los botones.
     */
    private fun setupClickListeners() {
        binding.btnChangePassword.setOnClickListener {
            val dialog = ChangePasswordDialogFragment()
            dialog.show(parentFragmentManager, ChangePasswordDialogFragment.TAG)
        }

        binding.btnLogout.setOnClickListener {
            performLogout()
        }

        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Cierra la sesión del usuario.
     * Limpia SessionManager y SharedPreferences, luego navega al login.
     */
    private fun performLogout() {
        // Limpiar lógica de sesión
        SessionManager.clearSession()
        
        // Limpiar SharedPreferences
        val sharedPrefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()

        // Navegar a Login Fragment
        // Idealmente deberíamos limpiar todo el backstack hasta Login
        findNavController().navigate(R.id.loginFragment) 
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
