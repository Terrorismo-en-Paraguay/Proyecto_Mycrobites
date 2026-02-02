package com.example.calendario_android_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.calendario_android_app.R
import com.example.calendario_android_app.databinding.FragmentEventDetailBinding
import com.example.calendario_android_app.model.EstadoEvento
import com.example.calendario_android_app.model.Evento
import com.example.calendario_android_app.model.GroupMember
import com.example.calendario_android_app.utils.SessionManager
import com.example.calendario_android_app.viewmodel.EventViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Fragmento de detalle de evento.
 * Muestra información completa del evento:
 * - Título, descripción, ubicación
 * - Fechas de inicio y fin
 * - Estado del evento
 * - Lista de participantes
 * - Opción de eliminar (solo para creadores/admins)
 */
class EventDetailFragment : Fragment() {

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!

    private val eventViewModel: EventViewModel by activityViewModels()
    private var idEvento: Int = 0
    private var eventoActual: Evento? = null

    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM, yyyy", Locale("es", "ES"))
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idEvento = arguments?.getInt("idEvento") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (idEvento == 0) {
            findNavController().popBackStack()
            return
        }

        setupListeners()
        loadEventDetails()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnViewParticipants.setOnClickListener {
            showParticipantsDialog()
        }

        binding.btnDeleteEvent.setOnClickListener {
            confirmDeletion()
        }
    }

    private fun loadEventDetails() {
        // We can find the event in the current list or fetch from DB if needed.
        // For simplicity and consistency, let's find it in ViewModel's current list first.
        val event = eventViewModel.eventos.value?.find { it.idEvento == idEvento }
        
        if (event != null) {
            displayEvent(event)
        } else {
            // TODO: Fetch from DAO if not found in current list (maybe search result or direct link)
            // For now, if not in list, we might have issues.
            Toast.makeText(context, "Evento no encontrado", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun displayEvent(event: Evento) {
        eventoActual = event
        binding.tvTitle.text = event.titulo
        binding.tvDate.text = event.fechaInicio.format(dateFormatter).replaceFirstChar { it.uppercase() }
        
        val timeRange = "${event.fechaInicio.format(timeFormatter)} - ${event.fechaFin.format(timeFormatter)}"
        binding.tvTime.text = timeRange
        
        binding.tvStatus.text = event.estado.valor.replaceFirstChar { it.uppercase() }
        when (event.estado) {
            EstadoEvento.PENDIENTE -> binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#FBC02D"))
            EstadoEvento.EN_PROGRESO -> binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CD964"))
            EstadoEvento.FINALIZADO -> binding.tvStatus.setTextColor(android.graphics.Color.GRAY)
        }

        binding.tvLocation.text = event.ubicacion ?: "Sin ubicación"
        binding.tvDescription.text = event.descripcion ?: "Sin descripción"

        // Check permissions for deletion
        val idUsuario = SessionManager.currentUser?.id_usuario ?: 0
        eventViewModel.checkCanDelete(idUsuario, idEvento) { canDelete ->
            if (_binding != null) {
                binding.btnDeleteEvent.visibility = if (canDelete) View.VISIBLE else View.GONE
            }
        }
        
        // Hide participants button if no label (personal)
        binding.btnViewParticipants.visibility = if (event.idEtiqueta != null) View.VISIBLE else View.GONE
    }

    private fun showParticipantsDialog() {
        eventViewModel.loadParticipants(idEvento) { participants ->
            if (_binding != null) {
                if (participants.isEmpty()) {
                    Toast.makeText(context, "No hay participantes asociados", Toast.LENGTH_SHORT).show()
                } else {
                    displayParticipants(participants)
                }
            }
        }
    }

    private fun displayParticipants(participants: List<GroupMember>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_multi_label, null)
        // Reusing a layout container if possible or creating a simple one. 
        // Better: create a simple dialog with a RecyclerView.
        
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
        
        val container = android.widget.LinearLayout(requireContext())
        container.orientation = android.widget.LinearLayout.VERTICAL
        container.setPadding(32, 32, 32, 32)
        
        val title = android.widget.TextView(requireContext())
        title.text = "Participantes"
        title.textSize = 20f
        title.setTextColor(android.graphics.Color.WHITE)
        title.setPadding(0, 0, 0, 32)
        container.addView(title)
        
        val recyclerView = RecyclerView(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        // Simple Adapter inline for participants
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_member, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val member = participants[position]
                holder.itemView.findViewById<android.widget.TextView>(R.id.tv_member_email).text = member.email
                holder.itemView.findViewById<android.widget.TextView>(R.id.tv_member_role).text = member.role
                holder.itemView.findViewById<android.widget.TextView>(R.id.tv_member_initial).text = 
                    (member.userName ?: member.email).firstOrNull()?.uppercase() ?: "U"
                holder.itemView.findViewById<View>(R.id.btn_remove_member).visibility = View.GONE
            }

            override fun getItemCount() = participants.size
        }
        
        container.addView(recyclerView)
        
        builder.setView(container)
        builder.setPositiveButton("Cerrar", null)
        builder.show()
    }

    private fun confirmDeletion() {
        AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setTitle("Eliminar Evento")
            .setMessage("¿Estás seguro de que deseas eliminar este evento? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                val idUsuario = SessionManager.currentUser?.id_usuario ?: 0
                val date = eventoActual?.fechaInicio?.toLocalDate() ?: java.time.LocalDate.now()
                eventViewModel.deleteEvent(idEvento, idUsuario, date)
                Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
