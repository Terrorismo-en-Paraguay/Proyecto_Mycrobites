package com.example.calendario_android_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.calendario_android_app.R
import com.example.calendario_android_app.viewmodel.CreateGroupViewModel
import com.google.android.material.button.MaterialButton

/**
 * Paso 1 de creación de grupo: Nombre y descripción.
 * Permite al usuario ingresar el nombre y descripción del grupo.
 */
class CreateGroupStep1Fragment : Fragment() {

    private val viewModel: CreateGroupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_group_step1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val tvBack = view.findViewById<TextView>(R.id.tv_back)
        val etGroupName = view.findViewById<EditText>(R.id.et_group_name)
        val etGroupDesc = view.findViewById<EditText>(R.id.et_group_desc)
        val btnNext = view.findViewById<MaterialButton>(R.id.btn_next)

        // Back Navigation
        val backListener = View.OnClickListener {
            findNavController().navigateUp()
        }
        btnBack.setOnClickListener(backListener)
        tvBack.setOnClickListener(backListener)

        // Next Button
        btnNext.setOnClickListener {
            val name = etGroupName.text.toString().trim()
            val desc = etGroupDesc.text.toString().trim()

            if (name.isEmpty()) {
                etGroupName.error = getString(R.string.error_fill_all_fields)
                return@setOnClickListener
            }

            viewModel.groupName = name
            viewModel.groupDescription = desc

            findNavController().navigate(R.id.action_createGroupStep1Fragment_to_createGroupStep2Fragment)
        }
    }
}
