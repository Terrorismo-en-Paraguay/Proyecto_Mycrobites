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

    private fun setupUserInfo() {
        // Load data from SessionManager
        val userName = SessionManager.currentClientName ?: "Usuario"
        val userEmail = SessionManager.currentUser?.correo ?: "usuario@email.com"
        val initial = userName.firstOrNull()?.toString()?.uppercase() ?: "U"

        binding.tvUserName.text = userName
        binding.tvUserEmail.text = userEmail
        binding.tvAvatarInitial.text = initial
    }

    private fun setupClickListeners() {
        binding.btnChangePassword.setOnClickListener {
            Toast.makeText(context, "Cambiar contraseña: Próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            performLogout()
        }

        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun performLogout() {
        // Clear session logic
        SessionManager.clearSession()
        
        // Clear Shared Preferences
        val sharedPrefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()

        // Navigate to Login Fragment
        // Using global action or explicit navigation 
        // Ideally we should pop everything up to Login
        findNavController().navigate(R.id.loginFragment) 
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
