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

/**
 * ViewModel que gestiona el flujo de creación de grupos en 3 pasos:
 * - Paso 1: Nombre y descripción del grupo
 * - Paso 2: Selección de etiquetas para el grupo
 * - Paso 3: Gestión de miembros y sus roles
 * Coordina la creación final del grupo, vinculación de miembros y asociación de etiquetas.
 */
class CreateGroupViewModel : ViewModel() {
    
    // Datos del Paso 1
    var groupName: String = ""
    var groupDescription: String = ""

    // Datos del Paso 2
    private val _selectedLabels = MutableLiveData<MutableList<Etiqueta>>(mutableListOf())
    val selectedLabels: LiveData<MutableList<Etiqueta>> = _selectedLabels

    // Datos del Paso 3
    private val _members = MutableLiveData<MutableList<GroupMember>>(mutableListOf())
    val members: LiveData<MutableList<GroupMember>> = _members

    private val _groupCreationResult = MutableLiveData<GroupCreationResult>()
    val groupCreationResult: LiveData<GroupCreationResult> = _groupCreationResult

    private val grupoDAO = GrupoDAOImpl()
    private val etiquetaDAO = EtiquetaDAOImpl()

    /**
     * Añade una etiqueta a la lista de etiquetas seleccionadas.
     * Evita duplicados por nombre.
     */
    fun addLabel(label: Etiqueta) {
        val currentList = _selectedLabels.value ?: mutableListOf()
        // Verificar duplicados por nombre
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

    /**
     * Gestión de Miembros
     * Añade un miembro a la lista. Evita duplicados por email.
     */
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

    /**
     * Actualiza el rol de un miembro existente.
     */
    fun updateMemberRole(email: String, newRole: String) {
        val currentList = _members.value ?: return
        val index = currentList.indexOfFirst { it.email.equals(email, ignoreCase = true) }
        if (index != -1) {
            currentList[index] = currentList[index].copy(role = newRole)
            _members.value = currentList
        }
    }

    /**
     * Creación del Grupo
     * Ejecuta el proceso completo de creación:
     * 1. Crea el grupo en la BD
     * 2. Vincula al creador como admin
     * 3. Vincula a todos los miembros con sus roles
     * 4. Asocia las etiquetas seleccionadas al grupo
     */
    fun createGroup(creatorUserId: Int) {
        viewModelScope.launch {
            try {
                // 1. Crear grupo
                val grupoId = grupoDAO.createGrupo(groupName, groupDescription)
                if (grupoId == -1) {
                    _groupCreationResult.value = GroupCreationResult.Error("Error al crear el grupo")
                    return@launch
                }

                // 2. Vincular creador como admin
                grupoDAO.linkUsuarioToGrupo(grupoId, creatorUserId, "admin")

                // 3. Vincular todos los miembros
                val membersList = _members.value ?: emptyList()
                for (member in membersList) {
                    member.userId?.let { userId ->
                        grupoDAO.linkUsuarioToGrupo(grupoId, userId, member.role)
                    }
                }

                // 4. Asociar etiquetas al grupo
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

/**
 * Resultado de la creación de un grupo.
 */
sealed class GroupCreationResult {
    data class Success(val grupoId: Int) : GroupCreationResult()
    data class Error(val message: String) : GroupCreationResult()
}
