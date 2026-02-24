package com.example.calendario_android_app.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.calendario_android_app.R
import com.example.calendario_android_app.databinding.DialogCreateLabelBinding
import com.example.calendario_android_app.dao.impl.EtiquetaDAOImpl
import com.example.calendario_android_app.model.Etiqueta
import com.example.calendario_android_app.viewmodel.EventViewModel
import com.example.calendario_android_app.utils.SessionManager
import com.example.calendario_android_app.service.NotificationManager

class CreateLabelDialogFragment : DialogFragment() {

    private var _binding: DialogCreateLabelBinding? = null
    private val binding get() = _binding!!

    
    
    var onLabelCreated: (() -> Unit)? = null
    
    private val eventViewModel: EventViewModel by activityViewModels() 

    private var selectedColor: String = "#8A2BE2"
    private var selectedCalendarId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateLabelBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCalendarSpinner()
        setupListeners()
        setupColorSelection()
        updatePreview()
    }
    
    private fun setupCalendarSpinner() {
        val user = SessionManager.currentUser
        val userId = user?.id_usuario ?: return
        
        eventViewModel.loadGrupos(userId)
        eventViewModel.grupos.observe(viewLifecycleOwner) { grupos ->
            val calendarNames = mutableListOf<String>()
            calendarNames.add("Sin grupo")
            calendarNames.addAll(grupos.map { it.nombre })
            
            val adapter = android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                calendarNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCalendar.adapter = adapter
            
            val personalIndex = grupos.indexOfFirst { it.nombre == "Personal" }
            if (personalIndex != -1) {
                binding.spinnerCalendar.setSelection(personalIndex + 1)
                selectedCalendarId = grupos[personalIndex].idGrupo
            } else {
                binding.spinnerCalendar.setSelection(0)
                selectedCalendarId = null
            }
            
            binding.spinnerCalendar.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position == 0) {
                        selectedCalendarId = null
                    } else {
                        selectedCalendarId = grupos[position - 1].idGrupo
                    }
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
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

    private fun setupListeners() {
        binding.btnCloseDialog.setOnClickListener {
            dismiss()
        }

        binding.etLabelName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePreview()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSaveLabel.setOnClickListener {
            saveLabel()
        }
    }

    private fun setupColorSelection() {
        val colors = listOf(
            binding.colorPurple,
            binding.colorPink,
            binding.colorBlue,
            binding.colorTeal,
            binding.colorOrange,
            binding.colorGrey
        )

        updateColorSelectionUI(colors)

        for (colorView in colors) {
            colorView.setOnClickListener { view ->
                selectedColor = view.tag.toString()
                updatePreview()
                updateColorSelectionUI(colors)
            }
        }
    }

    private fun updateColorSelectionUI(colors: List<View>) {
        for (view in colors) {
            val colorCode = view.tag.toString()
            if (colorCode == selectedColor) {
                view.setBackgroundResource(R.drawable.bg_circle_selected)
                view.background.setTint(Color.parseColor(colorCode))
            } else {
                view.setBackgroundResource(R.drawable.bg_color_circle)
                view.background.setTint(Color.parseColor(colorCode))
            }
        }
    }

    private fun updatePreview() {
        binding.previewText.text = binding.etLabelName.text.toString().ifBlank { "Nombre" }
        binding.previewChipContainer.background.setTint(Color.parseColor(selectedColor))
    }

    private fun saveLabel() {
        val name = binding.etLabelName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(context, "Por favor escribe un nombre", Toast.LENGTH_SHORT).show()
            return
        }
        
        val user = com.example.calendario_android_app.utils.SessionManager.currentUser
        val userId = user?.id_usuario ?: 1

        val newLabel = Etiqueta(
            nombre = name,
            color = selectedColor
        )

        eventViewModel.createEtiqueta(newLabel, userId, selectedCalendarId)
        
        context?.let { ctx ->
            NotificationManager.notifyLabelCreated(
                context = ctx,
                idUsuario = userId,
                labelName = name,
                labelColor = selectedColor
            )
            
            selectedCalendarId?.let { groupId ->
                NotificationManager.notifyLabelCreatedToGroup(
                    context = ctx,
                    idGroup = groupId,
                    labelName = name,
                    labelColor = selectedColor
                )
            }
        }
        
        Toast.makeText(context, "Etiqueta creada", Toast.LENGTH_SHORT).show()
        onLabelCreated?.invoke()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CreateLabelDialog"
    }
}
