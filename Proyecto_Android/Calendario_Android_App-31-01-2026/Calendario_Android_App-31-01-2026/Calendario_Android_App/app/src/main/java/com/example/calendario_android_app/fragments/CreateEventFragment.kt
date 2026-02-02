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
import com.example.calendario_android_app.service.NotificationManager
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.calendario_android_app.service.EmailService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Fragmento encargado de la creación de nuevos eventos.
 * Permite definir título, descripción, ubicación, fecha, hora, etiquetas y gestionar invitados por correo.
 */
class CreateEventFragment : Fragment() {

    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!

    private val eventViewModel: EventViewModel by activityViewModels()

    // Variables para almacenar la temporalidad del evento
    private var selectedDate: LocalDate = LocalDate.now()
    private var startTime: LocalTime = LocalTime.of(10, 0)
    private var endTime: LocalTime = LocalTime.of(11, 0)

    // Formateadores para mostrar fechas y horas de forma amigable en español
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

    /**
     * Inicializa la interfaz de usuario con los valores por defecto.
     */
    private fun setupUI() {
        updateDateDisplay()
        updateTimeDisplay()
    }

    /**
     * Configura los escuchadores de eventos para los botones y campos de texto.
     */
    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }
        
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        // Abrir selector de fecha
        binding.tvEventDate.setOnClickListener {
            showDatePicker()
        }

        // Abrir selector de hora de inicio
        binding.tvEventStartTime.setOnClickListener {
            showTimePicker(true)
        }

        // Abrir selector de hora de fin
        binding.tvEventEndTime.setOnClickListener {
            showTimePicker(false)
        }

        // Abrir diálogo de selección de etiquetas (propias o de grupos)
        binding.tvSelectLabel.setOnClickListener {
            val dialog = SelectLabelDialogFragment()
            dialog.onLabelSelected = { etiqueta ->
                selectedLabel = etiqueta
                binding.tvSelectLabel.text = etiqueta.nombre
                binding.tvSelectLabel.setTextColor(android.graphics.Color.WHITE)
            }
            dialog.show(childFragmentManager, SelectLabelDialogFragment.TAG)
        }

        binding.btnSave.setOnClickListener {
            saveEvent()
        }
        
        // Gestión de entrada de emails de invitados
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

    /**
     * Valida y añade un correo electrónico a la lista de invitados.
     */
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

    /**
     * Crea visualmente un 'Chip' para representar a un invitado añadido.
     */
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

    /**
     * Muestra el DatePickerDialog de Android.
     */
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

    /**
     * Muestra el TimePickerDialog de Android.
     * @param isStartTime Indica si estamos editando la hora de inicio o de fin.
     */
    private fun showTimePicker(isStartTime: Boolean) {
        val initialTime = if (isStartTime) startTime else endTime
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val newTime = LocalTime.of(hourOfDay, minute)
                if (isStartTime) {
                    startTime = newTime
                    // Aseguramos que la hora de fin sea al menos 1h después de la de inicio automáticamente.
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
            false // Formato 12h AM/PM
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

    /**
     * Proceso principal de guardado del evento.
     * Recopila los datos, los envía al ViewModel y dispara las notificaciones por correo.
     */
    private fun saveEvent() {
        val title = binding.etEventTitle.text.toString()
        if (title.isBlank()) {
            Toast.makeText(context, "Por favor ingresa un título", Toast.LENGTH_SHORT).show()
            return
        }

        val description = binding.etEventDescription.text.toString()

        val startDateTime = LocalDateTime.of(selectedDate, startTime)
        var endDateTime = LocalDateTime.of(selectedDate, endTime)
        
        // Manejo de eventos que terminan al día siguiente
        if (endDateTime.isBefore(startDateTime)) {
             endDateTime = endDateTime.plusDays(1)
        }

        val location = binding.etEventLocation.text.toString()
        
        // Recuperamos el usuario actual de la sesión para asignarle el evento.
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
            colorEtiqueta = selectedLabel?.color
        )

        // Persistencia en DB a través del ViewModel
        eventViewModel.createEvent(newEvent, guestEmails, userId)
        
        // Lógica de notificaciones por email
        context?.let { ctx ->
            // 1. Notificar al creador
            NotificationManager.notifyEventCreated(
                context = ctx,
                evento = newEvent,
                idCreador = userId
            )
            
            // 2. Notificar a invitados externos si los hay
            if (guestEmails.isNotEmpty()) {
                NotificationManager.notifyEventGuests(
                    context = ctx,
                    evento = newEvent,
                    guestEmails = guestEmails
                )
            }

            // 3. Notificar a los miembros del grupo si la etiqueta pertenece a un grupo/calendario
            selectedLabel?.idGrupo?.let { groupId ->
                NotificationManager.notifyEventCreatedToGroup(
                    context = ctx,
                    evento = newEvent,
                    idGroup = groupId
                )
            }
        }
        
        Toast.makeText(context, "Evento creado", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
