package com.example.calendario_android_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendario_android_app.model.Etiqueta
import com.example.calendario_android_app.model.GroupMember
import com.example.calendario_android_app.dao.impl.GrupoDAOImpl
import com.example.calendario_android_app.dao.impl.EtiquetaDAOImpl
import kotlinx.coroutines.launch

class CreateGroupViewModel : ViewModel() {
    
    // Step 1 Data
    var groupName: String = ""
    var groupDescription: String = ""

    // Step 2 Data
    private val _selectedLabels = MutableLiveData<MutableList<Etiqueta>>(mutableListOf())
    val selectedLabels: LiveData<MutableList<Etiqueta>> = _selectedLabels

    // Step 3 Data
    private val _members = MutableLiveData<MutableList<GroupMember>>(mutableListOf())
    val members: LiveData<MutableList<GroupMember>> = _members

    private val _groupCreationResult = MutableLiveData<GroupCreationResult>()
    val groupCreationResult: LiveData<GroupCreationResult> = _groupCreationResult

    private val grupoDAO = GrupoDAOImpl()
    private val etiquetaDAO = EtiquetaDAOImpl()

    fun addLabel(label: Etiqueta) {
        val currentList = _selectedLabels.value ?: mutableListOf()
        // Check for duplicates by name to avoid confusion
        if (currentList.none { it.nombre.equals(label.nombre, ignoreCase = true) }) {
            currentList.add(label)
            _selectedLabels.value = currentList
        }
    }

    fun removeLabel(label: Etiqueta) {
        val currentList = _selectedLabels.value ?: return
        if (currentList.remove(label)) {
            _selectedLabels.value = currentList
        }
    }

    fun replaceAllLabels(newLabels: List<Etiqueta>) {
        _selectedLabels.value = newLabels.toMutableList()
    }

    // Member Management
    fun addMember(member: GroupMember) {
        val currentList = _members.value ?: mutableListOf()
        if (currentList.none { it.email.equals(member.email, ignoreCase = true) }) {
            currentList.add(member)
            _members.value = currentList
        }
    }

    fun removeMember(member: GroupMember) {
        val currentList = _members.value ?: return
        if (currentList.remove(member)) {
            _members.value = currentList
        }
    }

    fun updateMemberRole(email: String, newRole: String) {
        val currentList = _members.value ?: return
        val index = currentList.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (index != -1) {
            currentList[index] = currentList[index].copy(role = newRole)
            _members.value = currentList
        }
    }

    // Group Creation
    fun createGroup(creatorUserId: Int) {
        viewModelScope.launch {
            try {
                // 1. Create grupo
                val grupoId = grupoDAO.createGrupo(groupName, groupDescription)
                if (grupoId == -1) {
                    _groupCreationResult.value = GroupCreationResult.Error("Error al crear el grupo")
                    return@launch
                }

                // 2. Link creator as admin
                grupoDAO.linkUsuarioToGrupo(grupoId, creatorUserId, "admin")

                // 3. Link all members
                val membersList = _members.value ?: emptyList()
                for (member in membersList) {
                    member.userId?.let { userId ->
                        grupoDAO.linkUsuarioToGrupo(grupoId, userId, member.role)
                    }
                }

                // 4. Associate labels to group
                val labelsList = _selectedLabels.value ?: emptyList()
                for (label in labelsList) {
                    if (label.idEtiqueta > 0) {
                        etiquetaDAO.updateEtiquetaGrupo(label.idEtiqueta, grupoId)
                    }
                }

                _groupCreationResult.value = GroupCreationResult.Success(grupoId)
            } catch (e: Exception) {
                e.printStackTrace()
                _groupCreationResult.value = GroupCreationResult.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetCreationResult() {
        _groupCreationResult.value = null
    }
}

sealed class GroupCreationResult {
    data class Success(val grupoId: Int) : GroupCreationResult()
    data class Error(val message: String) : GroupCreationResult()
}
