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
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.calendario_android_app.R
import com.example.calendario_android_app.databinding.DialogCreateLabelBinding
import com.example.calendario_android_app.model.Etiqueta
import com.example.calendario_android_app.viewmodel.EventViewModel
import com.example.calendario_android_app.utils.SessionManager

class CreateLabelDialogFragment : DialogFragment() {

    private var _binding: DialogCreateLabelBinding? = null
    private val binding get() = _binding!!

    // Shared ViewModel might not be enough if it's attached to parent fragment scope, 
    // but typically viewModels({ requireParentFragment() }) works if nested. 
    // However, here we just want to access the same ViewModel as the activity or parent.
    // simpler to just use the one scoped to this fragment or activity if shared.
    // For now, let's assume we want to call the ViewModel of the CreateEventFragment. 
    // Ideally we pass it or use shared ViewModel. Let's use activityViewModels if we were sharing with activity,
    // but here we can just create a new instance or pass logic. 
    // Better: use callback or TargetFragment (deprecated).
    // Simple approach: Use parentFragment casting or shared ViewModel with Activity.
    // Let's use viewModels(ownerProducer = { requireParentFragment() }) if it's a child frgament, 
    // but this is a DialogFragment shown via show().
    // We'll use requireActivity() scope to share with CreateEventFragment if it uses activity scope, 
    // but CreateEventFragment uses 'by viewModels()' which is fragment scope.
    // Let's just re-instantiate or assume we can get it from Activity if we change CreateEventFragment to use activityViewModels.
    // To minimize changes, I will use 'by viewModels({ requireParentFragment() })' assuming it's called as child? 
    // No, standard show() attaches to FM. 
    // Let's use a callback interface or just strict ViewModel.
    
    // Changing strategy: CreateEventFragment should probably use activityViewModels or we just use a callback.
    // For simplicity in this agentic context: I'll use a functional callback.
    
    var onLabelCreated: (() -> Unit)? = null
    
    // Use activity-scoped ViewModel to share data with CalendarFragment
    private val eventViewModel: EventViewModel by activityViewModels() 

    private var selectedColor: String = "#8A2BE2" // Default Purple
    private var selectedCalendarId: Int? = null // null means Personal (default)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateLabelBinding.inflate(inflater, container, false)
        // Make dialog background transparent to show CardView corners
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
        
        // Load calendars
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
            
            // Set default to Personal
            val personalIndex = grupos.indexOfFirst { it.nombre == "Personal" }
            if (personalIndex != -1) {
                // +1 because of "Sin grupo" added at index 0
                binding.spinnerCalendar.setSelection(personalIndex + 1)
                selectedCalendarId = grupos[personalIndex].idGrupo
            } else {
                binding.spinnerCalendar.setSelection(0)
                selectedCalendarId = null
            }
            
            // Listen for selection changes
            binding.spinnerCalendar.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position == 0) {
                        selectedCalendarId = null
                    } else {
                        // -1 because of "Sin grupo" added at index 0
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

        // Initial selection highlight
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
        val userId = user?.id_usuario ?: 1 // Fallback to 1 if testing without login

        val newLabel = Etiqueta(
            nombre = name,
            color = selectedColor
        )

        eventViewModel.createEtiqueta(newLabel, userId, selectedCalendarId)
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
