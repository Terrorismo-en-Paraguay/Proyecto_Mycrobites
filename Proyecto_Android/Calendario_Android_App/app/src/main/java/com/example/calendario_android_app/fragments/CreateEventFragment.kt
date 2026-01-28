package com.example.calendario_android_app.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.activityViewModels
import com.example.calendario_android_app.databinding.FragmentCreateEventBinding
import com.example.calendario_android_app.model.EstadoEvento
import com.example.calendario_android_app.model.Evento
import com.example.calendario_android_app.model.Etiqueta
import com.example.calendario_android_app.viewmodel.EventViewModel
import com.example.calendario_android_app.utils.SessionManager
import com.google.android.material.chip.Chip
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class CreateEventFragment : Fragment() {

    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!

    private val eventViewModel: EventViewModel by activityViewModels()

    private var selectedDate: LocalDate = LocalDate.now()
    private var startTime: LocalTime = LocalTime.of(10, 0)
    private var endTime: LocalTime = LocalTime.of(11, 0)

    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM, yyyy", Locale.forLanguageTag("es-ES"))
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
    
    private val guestEmails = mutableListOf<String>()
    
    private var selectedLabel: Etiqueta? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        updateDateDisplay()
        updateTimeDisplay()
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }
        
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvEventDate.setOnClickListener {
            showDatePicker()
        }

        binding.tvEventStartTime.setOnClickListener {
            showTimePicker(true)
        }

        binding.tvEventEndTime.setOnClickListener {
            showTimePicker(false)
        }

        binding.tvSelectLabel.setOnClickListener {
            val dialog = SelectLabelDialogFragment()
            dialog.onLabelSelected = { etiqueta ->
                selectedLabel = etiqueta
                binding.tvSelectLabel.text = etiqueta.nombre
                binding.tvSelectLabel.setTextColor(android.graphics.Color.WHITE)
                // Optional: Show color indicator
                // For now just text update
            }
            dialog.show(childFragmentManager, SelectLabelDialogFragment.TAG)
        }

        binding.btnSave.setOnClickListener {
            saveEvent()
        }
        
        // Guest input logic
        binding.btnAddGuest.setOnClickListener {
            addGuestEmail()
        }
        
        binding.etGuestEmail.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                addGuestEmail()
                true
            } else {
                false
            }
        }
    }

    private fun addGuestEmail() {
        val email = binding.etGuestEmail.text.toString().trim()
        if (email.isNotEmpty()) {
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!guestEmails.contains(email)) {
                    guestEmails.add(email)
                    addChipToGroup(email)
                    binding.etGuestEmail.text?.clear()
                } else {
                    Toast.makeText(context, "Este email ya ha sido añadido", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Email inválido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addChipToGroup(email: String) {
        val chip = Chip(context)
        chip.text = email
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            binding.chipGroupGuests.removeView(chip)
            guestEmails.remove(email)
        }
        binding.chipGroupGuests.addView(chip)
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                updateDateDisplay()
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
        datePicker.show()
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val initialTime = if (isStartTime) startTime else endTime
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val newTime = LocalTime.of(hourOfDay, minute)
                if (isStartTime) {
                    startTime = newTime
                    // Ensure end time is after start time
                    if (endTime.isBefore(startTime)) {
                        endTime = startTime.plusHours(1)
                    }
                } else {
                    endTime = newTime
                }
                updateTimeDisplay()
            },
            initialTime.hour,
            initialTime.minute,
            false // AM/PM format
        )
        timePicker.show()
    }

    private fun updateDateDisplay() {
        binding.tvEventDate.text = selectedDate.format(dateFormatter).replaceFirstChar { it.uppercase() }
    }

    private fun updateTimeDisplay() {
        binding.tvEventStartTime.text = startTime.format(timeFormatter)
        binding.tvEventEndTime.text = endTime.format(timeFormatter)
    }

    private fun saveEvent() {
        val title = binding.etEventTitle.text.toString()
        if (title.isBlank()) {
            Toast.makeText(context, "Por favor ingresa un título", Toast.LENGTH_SHORT).show()
            return
        }

        val description = binding.etEventDescription.text.toString()

        val startDateTime = LocalDateTime.of(selectedDate, startTime)
        var endDateTime = LocalDateTime.of(selectedDate, endTime)
        
        if (endDateTime.isBefore(startDateTime)) {
             endDateTime = endDateTime.plusDays(1)
        }

        val location = binding.etEventLocation.text.toString()
        
        // Get User ID from Session
        val user = SessionManager.currentUser
        val userId = user?.id_usuario ?: 1

        val newEvent = Evento(
            idCreador = userId,
            idEtiqueta = selectedLabel?.idEtiqueta,
            titulo = title,
            descripcion = if (description.isBlank()) null else description,
            fechaInicio = startDateTime,
            fechaFin = endDateTime,
            ubicacion = if (location.isBlank()) null else location,
            estado = EstadoEvento.PENDIENTE,
            colorEtiqueta = selectedLabel?.color // Transient
        )

        eventViewModel.createEvent(newEvent, guestEmails, userId)
        Toast.makeText(context, "Evento creado", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
