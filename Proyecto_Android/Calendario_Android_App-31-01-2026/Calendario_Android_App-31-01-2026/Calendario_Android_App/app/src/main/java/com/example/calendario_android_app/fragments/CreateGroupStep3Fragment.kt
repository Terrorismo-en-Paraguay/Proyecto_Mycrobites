package com.example.calendario_android_app.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.calendario_android_app.R
import com.example.calendario_android_app.adapters.GroupMemberAdapter
import com.example.calendario_android_app.dao.impl.UsuarioDAOImpl
import com.example.calendario_android_app.dao.impl.ClienteDAOImpl
import com.example.calendario_android_app.model.GroupMember
import com.example.calendario_android_app.utils.SessionManager
import com.example.calendario_android_app.viewmodel.CreateGroupViewModel
import com.example.calendario_android_app.service.NotificationManager
import com.example.calendario_android_app.viewmodel.EventViewModel
import com.example.calendario_android_app.viewmodel.GroupCreationResult
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Tercer y último paso del asistente de creación de grupos/calendarios compartidos.
 * En esta pantalla se gestionan los miembros iniciales del grupo y sus roles.
 */
class CreateGroupStep3Fragment : Fragment() {

    private val viewModel: CreateGroupViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()
    
    // UI Elements
    private lateinit var etEmail: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var rvMembers: RecyclerView
    private lateinit var tvMembersHeader: TextView
    private lateinit var memberAdapter: GroupMemberAdapter
    
    // Acceso a datos
    private val usuarioDAO = UsuarioDAOImpl()
    private val clienteDAO = ClienteDAOImpl()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_group_step3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etEmail = view.findViewById(R.id.et_member_email)
        spinnerRole = view.findViewById(R.id.spinner_role)
        rvMembers = view.findViewById(R.id.rv_members)
        tvMembersHeader = view.findViewById(R.id.tv_members_header)

        setupRoleSpinner()
        setupMembersRecyclerView()
        setupListeners(view)
        observeViewModel()
    }

    /**
     * Configura el desplegable con los roles disponibles: Miembro, Editor, Administrador.
     */
    private fun setupRoleSpinner() {
        val roles = listOf(
            getString(R.string.role_member),
            getString(R.string.role_editor),
            getString(R.string.role_admin)
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter
    }

    /**
     * Configura la lista de miembros añadidos actualmente.
     */
    private fun setupMembersRecyclerView() {
        memberAdapter = GroupMemberAdapter(emptyList()) { member ->
            viewModel.removeMember(member)
        }
        rvMembers.layoutManager = LinearLayoutManager(context)
        rvMembers.adapter = memberAdapter
    }

    private fun setupListeners(view: View) {
        // Navegación hacia atrás
        view.findViewById<View>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }
        
        view.findViewById<View>(R.id.tv_back).setOnClickListener {
            findNavController().navigateUp()
        }

        // Añadir nuevo miembro por correo
        view.findViewById<MaterialButton>(R.id.btn_add_member).setOnClickListener {
            addMember()
        }

        // Finalizar y crear el grupo en la base de datos
        view.findViewById<MaterialButton>(R.id.btn_create_group).setOnClickListener {
            createGroup()
        }
    }

    /**
     * Observa los cambios en el ViewModel para actualizar la UI y manejar el resultado de la creación.
     */
    private fun observeViewModel() {
        // Actualizar lista de miembros visualmente
        viewModel.members.observe(viewLifecycleOwner) { members ->
            memberAdapter.updateMembers(members)
            tvMembersHeader.text = getString(R.string.current_members_section, members.size)
        }

        // Manejar el éxito o error tras intentar crear el grupo
        viewModel.groupCreationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is GroupCreationResult.Success -> {
                    Toast.makeText(context, R.string.group_created_success, Toast.LENGTH_SHORT).show()
                    
                    // Lógica de notificaciones por email al finalizar
                    context?.let { ctx ->
                        val currentUser = SessionManager.currentUser
                        currentUser?.let { user ->
                            val currentGroupName = if (viewModel.groupName.isNotBlank()) viewModel.groupName else "Nuevo Grupo"
                            val currentGroupDesc = if (viewModel.groupDescription.isNotBlank()) viewModel.groupDescription else null
                            
                            // 1. Notificar al creador (admin) que el grupo está listo
                            NotificationManager.notifyGroupCreated(
                                context = ctx,
                                idUsuario = user.id_usuario,
                                groupName = currentGroupName,
                                groupDescription = currentGroupDesc
                            )
                            
                            // 2. Notificar masivamente a todos los miembros invitados
                            val members = viewModel.members.value ?: emptyList()
                            val memberIds = members.mapNotNull { it.userId }
                            val memberRoles = members.mapNotNull { m -> m.userId?.let { it to m.role } }.toMap()
                            
                            if (memberIds.isNotEmpty()) {
                                NotificationManager.notifyUsersAddedToGroup(
                                    context = ctx,
                                    idUsuarioAddedList = memberIds,
                                    idUsuarioAdder = user.id_usuario,
                                    groupName = currentGroupName,
                                    roles = memberRoles
                                )
                            }
                        }
                    }
                    
                    // Recargar la lista de grupos en el calendario
                    val userId = SessionManager.currentUser?.id_usuario ?: return@observe
                    eventViewModel.loadGrupos(userId)
                    
                    // Volver a la pantalla principal
                    findNavController().navigate(R.id.calendarFragment)
                    viewModel.resetCreationResult()
                }
                is GroupCreationResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    viewModel.resetCreationResult()
                }
                null -> { /* Sin resultado aún */ }
            }
        }
    }

    /**
     * Busca un usuario en la base de datos por su correo y lo añade a la lista temporal de miembros.
     */
    private fun addMember() {
        val email = etEmail.text.toString().trim()
        
        // Validaciones básicas de correo
        if (email.isEmpty()) {
            Toast.makeText(context, R.string.error_invalid_email, Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, R.string.error_invalid_email, Toast.LENGTH_SHORT).show()
            return
        }

        // Evitar duplicados en la lista local
        val currentMembers = viewModel.members.value ?: emptyList()
        if (currentMembers.any { it.email.equals(email, ignoreCase = true) }) {
            Toast.makeText(context, R.string.error_member_already_added, Toast.LENGTH_SHORT).show()
            return
        }

        // Mapeo de rol seleccionado
        val roleIndex = spinnerRole.selectedItemPosition
        val role = when(roleIndex) {
            0 -> "miembro"
            1 -> "editor"
            2 -> "admin"
            else -> "miembro"
        }

        // Búsqueda asíncrona en el servidor
        lifecycleScope.launch {
            try {
                val usuario = usuarioDAO.getUserByEmail(email)
                if (usuario != null) {
                    // Si existe, recuperamos su nombre real de la tabla clientes
                    val clienteId = usuario.id_cliente.toIntOrNull()
                    val clienteName = clienteId?.let { clienteDAO.obtenerCliente(it)?.nombre }
                    
                    val member = GroupMember(
                        email = email,
                        userId = usuario.id_usuario,
                        userName = clienteName,
                        role = role
                    )
                    viewModel.addMember(member)
                    etEmail.text?.clear()
                } else {
                    // El usuario debe estar registrado en la app para ser añadido a un grupo
                    Toast.makeText(context, R.string.error_user_not_found, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al buscar usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Ordena al ViewModel la persistencia del grupo y sus vínculos de membresía.
     */
    private fun createGroup() {
        val userId = SessionManager.currentUser?.id_usuario
        if (userId == null) {
            Toast.makeText(context, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.createGroup(userId)
    }
}
