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
import com.example.calendario_android_app.viewmodel.EventViewModel
import com.example.calendario_android_app.viewmodel.GroupCreationResult
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class CreateGroupStep3Fragment : Fragment() {

    private val viewModel: CreateGroupViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()
    
    private lateinit var etEmail: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var rvMembers: RecyclerView
    private lateinit var tvMembersHeader: TextView
    private lateinit var memberAdapter: GroupMemberAdapter
    
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

    private fun setupMembersRecyclerView() {
        memberAdapter = GroupMemberAdapter(emptyList()) { member ->
            viewModel.removeMember(member)
        }
        rvMembers.layoutManager = LinearLayoutManager(context)
        rvMembers.adapter = memberAdapter
    }

    private fun setupListeners(view: View) {
        view.findViewById<View>(R.id.btn_back).setOnClickListener {
            findNavController().navigateUp()
        }
        
        view.findViewById<View>(R.id.tv_back).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<MaterialButton>(R.id.btn_add_member).setOnClickListener {
            addMember()
        }

        view.findViewById<MaterialButton>(R.id.btn_create_group).setOnClickListener {
            createGroup()
        }
    }

    private fun observeViewModel() {
        viewModel.members.observe(viewLifecycleOwner) { members ->
            memberAdapter.updateMembers(members)
            tvMembersHeader.text = getString(R.string.current_members_section, members.size)
        }

        viewModel.groupCreationResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is GroupCreationResult.Success -> {
                    Toast.makeText(context, R.string.group_created_success, Toast.LENGTH_SHORT).show()
                    // Reload grupos to show new group
                    val userId = SessionManager.currentUser?.id_usuario ?: return@observe
                    eventViewModel.loadGrupos(userId)
                    // Navigate back to calendar
                    findNavController().navigate(R.id.calendarFragment)
                    viewModel.resetCreationResult()
                }
                is GroupCreationResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    viewModel.resetCreationResult()
                }
                null -> { /* No result yet */ }
            }
        }
    }

    private fun addMember() {
        val email = etEmail.text.toString().trim()
        
        // Validate email
        if (email.isEmpty()) {
            Toast.makeText(context, R.string.error_invalid_email, Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, R.string.error_invalid_email, Toast.LENGTH_SHORT).show()
            return
        }

        // Check if already added
        val currentMembers = viewModel.members.value ?: emptyList()
        if (currentMembers.any { it.email.equals(email, ignoreCase = true) }) {
            Toast.makeText(context, R.string.error_member_already_added, Toast.LENGTH_SHORT).show()
            return
        }

        // Get selected role
        val roleIndex = spinnerRole.selectedItemPosition
        val role = when(roleIndex) {
            0 -> "miembro"
            1 -> "editor"
            2 -> "admin"
            else -> "miembro"
        }

        // Lookup user in database
        lifecycleScope.launch {
            try {
                val usuario = usuarioDAO.getUserByEmail(email)
                if (usuario != null) {
                    // Get client name
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
                    Toast.makeText(context, R.string.error_user_not_found, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al buscar usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createGroup() {
        val userId = SessionManager.currentUser?.id_usuario
        if (userId == null) {
            Toast.makeText(context, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.createGroup(userId)
    }
}
