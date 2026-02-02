package com.example.calendario_android_app.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.calendario_android_app.R
import com.example.calendario_android_app.dao.impl.UsuarioDAOImpl
import com.example.calendario_android_app.databinding.DialogChangePasswordBinding
import com.example.calendario_android_app.service.NotificationManager
import com.example.calendario_android_app.utils.SessionManager
import com.example.calendario_android_app.util.HashUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Diálogo que permite al usuario cambiar su contraseña actual.
 * Incluye validaciones de seguridad y notificaciones por correo tras el éxito.
 */
class ChangePasswordDialogFragment : DialogFragment() {

    private var _binding: DialogChangePasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChangePasswordBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        setupValidation()
    }

    private fun setupListeners() {
        binding.btnCloseDialog.setOnClickListener { dismiss() }

        // Configuración de los botones para ver/ocultar contraseñas
        setupPasswordToggle(binding.etCurrentPassword, binding.btnEyeCurrentPassword)
        setupPasswordToggle(binding.etNewPassword, binding.btnEyeNewPassword)
        setupPasswordToggle(binding.etConfirmPassword, binding.btnEyeConfirmPassword)

        binding.btnSavePassword.setOnClickListener {
            saveNewPassword()
        }
    }

    /**
     * Alterna la visibilidad del texto en los campos de contraseña.
     */
    private fun setupPasswordToggle(editText: EditText, toggleButton: ImageView) {
        toggleButton.setOnClickListener {
            val isCurrentlyVisible = editText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            if (!isCurrentlyVisible) {
                editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleButton.alpha = 1.0f
            } else {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleButton.alpha = 0.5f
            }
            // Mantenemos el cursor al final del texto tras el cambio
            editText.setSelection(editText.text.length)
        }
    }

    /**
     * Configura un escuchador de texto para validar en tiempo real los campos de entrada.
     */
    private fun setupValidation() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInputs()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etCurrentPassword.addTextChangedListener(textWatcher)
        binding.etNewPassword.addTextChangedListener(textWatcher)
        binding.etConfirmPassword.addTextChangedListener(textWatcher)

        // Validación inicial para desactivar el botón de guardado
        validateInputs()
    }

    /**
     * Comprueba si los datos introducidos cumplen los requisitos antes de permitir guardar.
     */
    private fun validateInputs() {
        val currentPass = binding.etCurrentPassword.text.toString()
        val newPass = binding.etNewPassword.text.toString()
        val confirmPass = binding.etConfirmPassword.text.toString()

        val isCurrentNotEmpty = currentPass.isNotEmpty()
        val isNewValidLength = newPass.length >= 4
        val isConfirmMatching = newPass == confirmPass && confirmPass.isNotEmpty()
        val isDifferentFromCurrent = newPass != currentPass

        val isValid = isCurrentNotEmpty && isNewValidLength && isConfirmMatching && isDifferentFromCurrent

        // Habilitar/Deshabilitar botón según validez
        binding.btnSavePassword.isEnabled = isValid
        binding.btnSavePassword.alpha = if (isValid) 1.0f else 0.5f
    }

    /**
     * Proceso de actualización de contraseña en el servidor.
     */
    private fun saveNewPassword() {
        val user = SessionManager.currentUser ?: return
        val currentPassAttempt = binding.etCurrentPassword.text.toString()
        val newPass = binding.etNewPassword.text.toString()

        // Verificamos localmente que la contraseña actual coincida antes de enviar al servidor
        if (user.password_hash != HashUtils.hashPassword(currentPassAttempt)) {
            Toast.makeText(context, "La contraseña actual es incorrecta", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val success = withContext(Dispatchers.IO) {
                UsuarioDAOImpl().updatePassword(user.id_usuario, newPass)
            }

            if (success) {
                // Actualizamos la sesión local con la nueva contraseña (hasheada)
                SessionManager.currentUser = SessionManager.currentUser?.copy(password_hash = HashUtils.hashPassword(newPass))
                
                // Disparamos notificación de seguridad por email
                context?.let { ctx ->
                    NotificationManager.notifyPasswordChanged(ctx, user.id_usuario)
                }

                Toast.makeText(context, "Contraseña actualizada correctamente", Toast.LENGTH_LONG).show()
                dismiss()
            } else {
                Toast.makeText(context, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ChangePasswordDialog"
    }
}

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ChangePasswordDialog"
    }
}
