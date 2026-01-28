package com.example.calendario_android_app.fragments

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.calendario_android_app.R
import com.example.calendario_android_app.viewmodel.AuthViewModel
import com.example.calendario_android_app.viewmodel.RegisterResult
import com.google.android.material.snackbar.Snackbar

class RegisterFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    // UI Elements
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnEyePassword: ImageView
    private lateinit var btnEyeConfirmPassword: ImageView

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI Elements
        etName = view.findViewById(R.id.etName)
        etSurname = view.findViewById(R.id.etSurname)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        btnEyePassword = view.findViewById(R.id.btnEyePassword)
        btnEyeConfirmPassword = view.findViewById(R.id.btnEyeConfirmPassword)

        // Initially disable button
        btnRegister.isEnabled = false
        btnRegister.alpha = 0.5f

        // Setup TextWatchers for validation
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInputs()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etName.addTextChangedListener(textWatcher)
        etSurname.addTextChangedListener(textWatcher)
        etEmail.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)
        etConfirmPassword.addTextChangedListener(textWatcher)

        setupPasswordToggle(etPassword, btnEyePassword) { isVisible ->
            isPasswordVisible = isVisible
        }

        setupPasswordToggle(etConfirmPassword, btnEyeConfirmPassword) { isVisible ->
            isConfirmPasswordVisible = isVisible
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val surname = etSurname.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            
            viewModel.register(name, surname, email, password)
        }

        // Observe Registration Result
        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is RegisterResult.Success -> {
                    Snackbar.make(view, "Cuenta creada exitosamente", Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                }
                is RegisterResult.Error -> {
                    Snackbar.make(view, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateInputs() {
        val name = etName.text.toString()
        val surname = etSurname.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        val isNameValid = name.isNotBlank()
        val isSurnameValid = surname.isNotBlank()
        val isEmailValid = email.isNotBlank()
        val isPasswordValid = password.length >= 4
        val isConfirmPasswordValid = password == confirmPassword

        val isFormValid = isNameValid && isSurnameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid

        btnRegister.isEnabled = isFormValid
        btnRegister.alpha = if (isFormValid) 1.0f else 0.5f
    }

    private fun setupPasswordToggle(editText: EditText, toggleButton: ImageView, onVisibilityChanged: (Boolean) -> Unit) {
        // Since we are reusing this, keep it generic. Logic inside onViewCreated handles specific vars.
        // Actually, we can just use local state here or pass it in. 
        // For simplicity, reusing the existing robust toggle logic from the file I read.
        
        toggleButton.setOnClickListener {
             // We need to know current state. 
             // Simplification: Check input type or tag. safely toggling based on current input type.
             val isCurrentlyVisible = editText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
             
             if (!isCurrentlyVisible) { // Switch to visible
                editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleButton.alpha = 1.0f
                onVisibilityChanged(true)
             } else { // Switch to hidden
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleButton.alpha = 0.5f
                onVisibilityChanged(false)
             }
             editText.setSelection(editText.text.length)
        }
    }
}