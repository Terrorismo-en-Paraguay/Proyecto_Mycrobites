package com.example.calendario_android_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendario_android_app.dao.impl.EventoDAOImpl
import com.example.calendario_android_app.dao.impl.FestivoDAOImpl
import com.example.calendario_android_app.model.Evento
import com.example.calendario_android_app.model.EstadoEvento
import com.example.calendario_android_app.model.Festivo
import com.example.calendario_android_app.model.Etiqueta
import com.example.calendario_android_app.dao.impl.EtiquetaDAOImpl
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

import com.example.calendario_android_app.dao.impl.GrupoDAOImpl
import com.example.calendario_android_app.model.Grupo

class EventViewModel : ViewModel() {
    
    private val eventoDAO = EventoDAOImpl()
    private val festivoDAO = FestivoDAOImpl()
    private val etiquetaDAO = EtiquetaDAOImpl()
    private val grupoDAO = GrupoDAOImpl()
    
    // Displayed events (filtered)
    private val _eventos = MutableLiveData<List<Evento>>()
    val eventos: LiveData<List<Evento>> = _eventos
    
    // Displayed days with events (filtered)
    private val _diasConEventos = MutableLiveData<List<Int>>()
    val diasConEventos: LiveData<List<Int>> = _diasConEventos
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _etiquetas = MutableLiveData<List<Etiqueta>>()
    val etiquetas: LiveData<List<Etiqueta>> = _etiquetas
    
    private val _grupos = MutableLiveData<List<Grupo>>()
    val grupos: LiveData<List<Grupo>> = _grupos

    // Filtering State
    private val hiddenLabelIds = mutableSetOf<Int>()
    private val hiddenGroupIds = mutableSetOf<Int>()
    private var searchQuery: String = ""
    
    // Raw Data cache
    private var allEventsForList: MutableList<Evento> = mutableListOf()
    private var allEventsForMonth: MutableList<Evento> = mutableListOf()
    
    // Aux: Current range for month loads to help refreshing
    private var currentMonthLoadParams: Triple<Int, Int, Int>? = null // idUsuario, year, month

    fun toggleLabelVisibility(labelId: Int, isVisible: Boolean) {
        if (isVisible) {
            hiddenLabelIds.remove(labelId)
        } else {
            hiddenLabelIds.add(labelId)
        }
        applyFilters()
    }

    fun toggleGroupVisibility(groupId: Int, isVisible: Boolean) {
        if (isVisible) {
            hiddenGroupIds.remove(groupId)
        } else {
            hiddenGroupIds.add(groupId)
        }
        applyFilters()
        // Trigger generic update to refresh dependent views (like labels check state in UI if observing)
        _etiquetas.value = _etiquetas.value 
    }
    
    fun setSearchQuery(query: String, idUsuario: Int) {
        searchQuery = query
        if (query.isNotBlank()) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val searchResults = eventoDAO.searchEventos(idUsuario, query)
                    allEventsForList = searchResults.toMutableList()
                    applyFilters()
                } catch(e: Exception) {
                    e.printStackTrace()
                    _eventos.value = emptyList()
                } finally {
                    _isLoading.value = false
                }
            }
        } else {
            applyFilters()
        }
    }

    fun isLabelVisible(labelId: Int): Boolean {
        if (hiddenLabelIds.contains(labelId)) return false
        
        // Find label to check its group
        val etiqueta = _etiquetas.value?.find { it.idEtiqueta == labelId }
        if (etiqueta?.idGrupo != null && hiddenGroupIds.contains(etiqueta.idGrupo)) {
            return false
        }
        
        return true
    }
    
    fun isGroupVisible(groupId: Int): Boolean {
        return !hiddenGroupIds.contains(groupId)
    }

    private fun isPersonalGroupHidden(): Boolean {
        val personalGroup = _grupos.value?.find { it.nombre == "Personal" }
        return personalGroup != null && hiddenGroupIds.contains(personalGroup.idGrupo)
    }
    
    private fun applyFilters() {
        val hideAllPersonal = isPersonalGroupHidden()

        // Filter List
        var filteredList = allEventsForList.filter { event ->
            // 1. Group check (if event belongs to a label, check that label's group)
            if (event.idEtiqueta != null) {
                val label = _etiquetas.value?.find { it.idEtiqueta == event.idEtiqueta }
                if (label?.idGrupo != null && hiddenGroupIds.contains(label.idGrupo)) {
                    return@filter false
                }
            } else if (hideAllPersonal) {
                // Events without labels are assumed to be "Personal" currently
                return@filter false
            }
            
            // 2. Label specific filter
            val labelMatch = event.idEtiqueta == null || !hiddenLabelIds.contains(event.idEtiqueta)
            
            // 3. Search filter
            val searchMatch = if (searchQuery.isBlank()) {
                true
            } else {
                event.titulo.contains(searchQuery, ignoreCase = true)
            }
            
            labelMatch && searchMatch
        }
        
        // Sort search results if query is active
        if (searchQuery.isNotBlank()) {
            val today = java.time.LocalDate.now()
            filteredList = filteredList.sortedWith(compareBy(
                // Primary: Relevance (exact match first, then starts with, then contains)
                { event ->
                    when {
                        event.titulo.equals(searchQuery, ignoreCase = true) -> 0
                        event.titulo.startsWith(searchQuery, ignoreCase = true) -> 1
                        else -> 2
                    }
                },
                // Secondary: Proximity to today (absolute days difference)
                { event ->
                    kotlin.math.abs(java.time.temporal.ChronoUnit.DAYS.between(today, event.fechaInicio.toLocalDate()))
                }
            ))
        }
        
        _eventos.value = filteredList
        
        // Filter Dots (Month)
        updateDiasConEventosFromCache()
    }

    private fun updateDiasConEventosFromCache() {
        val hideAllPersonal = isPersonalGroupHidden()
        
        // Filter events
        val filteredMonthEvents = allEventsForMonth.filter { event ->
            // 1. Group check
            if (event.idEtiqueta != null) {
                val label = _etiquetas.value?.find { it.idEtiqueta == event.idEtiqueta }
                if (label?.idGrupo != null && hiddenGroupIds.contains(label.idGrupo)) {
                    return@filter false
                }
            } else if (hideAllPersonal) {
                return@filter false
            }
            
            // 2. Label specific filter
            event.idEtiqueta == null || !hiddenLabelIds.contains(event.idEtiqueta)
        }
        
        val diasEventos = filteredMonthEvents.map { it.fechaInicio.dayOfMonth }.toMutableSet()
        
        // Add holidays (they don't have labels usually, or treat pseudolabel?)
        // Assuming holidays are not filtered by labels currently
        if (currentMonthLoadParams != null) {
            val (_, _, month) = currentMonthLoadParams!!
            viewModelScope.launch {
                try {
                     val diasFestivos = festivoDAO.getDiasFestivos(month)
                     diasEventos.addAll(diasFestivos)
                     _diasConEventos.value = diasEventos.sorted()
                } catch (e: Exception) {
                    _diasConEventos.value = diasEventos.sorted()
                }
            }
        } else {
             _diasConEventos.value = diasEventos.sorted()
        }
    }

    fun loadEventosForDate(idUsuario: Int, fecha: LocalDate) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load user events
                val eventosList = eventoDAO.getEventosByUsuarioAndFecha(idUsuario, fecha).toMutableList()
                
                // Load holiday for this date
                val festivo = festivoDAO.getFestivoByMesYDia(fecha.monthValue, fecha.dayOfMonth)
                festivo?.let {
                    val festivoEvento = Evento(
                        idEvento = 0,
                        idCreador = null,
                        titulo = it.nombre,
                        descripcion = it.descripcion,
                        fechaInicio = fecha.atStartOfDay(),
                        fechaFin = fecha.atTime(23, 59),
                        ubicacion = "España",
                        estado = EstadoEvento.PENDIENTE
                    )
                    eventosList.add(0, festivoEvento)
                }
                
                allEventsForList = eventosList
                applyFilters()
                
            } catch (e: Exception) {
                e.printStackTrace()
                _eventos.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadEventosForRange(idUsuario: Int, start: LocalDate, end: LocalDate) {
         viewModelScope.launch {
            _isLoading.value = true
            try {
                val eventosList = eventoDAO.getEventosByUsuarioAndRango(idUsuario, start, end).toMutableList()
                
                var currentDate = start
                while (!currentDate.isAfter(end)) {
                    val festivo = festivoDAO.getFestivoByMesYDia(currentDate.monthValue, currentDate.dayOfMonth)
                    festivo?.let {
                        val festivoEvento = Evento(
                            idEvento = -1 * currentDate.dayOfYear, 
                            idCreador = null,
                            titulo = it.nombre,
                            descripcion = it.descripcion,
                            fechaInicio = currentDate.atStartOfDay(),
                            fechaFin = currentDate.atTime(23, 59),
                            ubicacion = "España",
                            estado = EstadoEvento.PENDIENTE
                        )
                        eventosList.add(festivoEvento)
                    }
                    currentDate = currentDate.plusDays(1)
                }
                
                allEventsForList = eventosList
                applyFilters()
            } catch (e: Exception) {
                e.printStackTrace()
                _eventos.value = emptyList()
            } finally {
                _isLoading.value = false
            }
         }
    }
    
    fun loadDiasConEventos(idUsuario: Int, year: Int, month: Int) {
        currentMonthLoadParams = Triple(idUsuario, year, month)
        viewModelScope.launch {
            try {
                // Instead of getDiasConEventos, we fetch ALL events for the month to allow local filtering
                // Construct Date Range for the month
                val startOfMonth = LocalDate.of(year, month, 1)
                val endOfMonth = startOfMonth.plusMonths(1).minusDays(1)
                
                allEventsForMonth = eventoDAO.getEventosByUsuarioAndRango(idUsuario, startOfMonth, endOfMonth).toMutableList()
                
                updateDiasConEventosFromCache()
                
            } catch (e: Exception) {
                e.printStackTrace()
                _diasConEventos.value = emptyList()
            }
        }
    }
    
    fun createEvent(evento: Evento, invitados: List<String> = emptyList(), idUsuario: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val eventoToSave = if (evento.idCreador == null) evento.copy(idCreador = idUsuario) else evento
                
                val idEvento = eventoDAO.insertEventoReturnId(eventoToSave, idUsuario)
                if (idEvento != -1) {
                    if (invitados.isNotEmpty()) {
                        eventoDAO.insertInvitados(idEvento, invitados)
                    }
                    // Refresh current view (list)
                    loadEventosForDate(idUsuario, evento.fechaInicio.toLocalDate())
                    // Refresh dots if in same month
                    currentMonthLoadParams?.let { (uid, year, month) ->
                         if (evento.fechaInicio.year == year && evento.fechaInicio.monthValue == month) {
                             loadDiasConEventos(uid, year, month)
                         }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearEventos() {
        _eventos.value = emptyList()
    }

    fun loadEtiquetas(idUsuario: Int) {
        viewModelScope.launch {
            try {
                _etiquetas.value = etiquetaDAO.getEtiquetasByUsuario(idUsuario)
                // We don't reset hiddenLabelIds here to persist selection if reloaded
            } catch (e: Exception) {
                e.printStackTrace()
                _etiquetas.value = emptyList()
            }
        }
    }
    
    fun loadGrupos(idUsuario: Int) {
        viewModelScope.launch {
            try {
                val grupos = grupoDAO.getGruposByUsuario(idUsuario)
                if (grupos.isEmpty()) {
                    // Auto-fix: Create default "Personal" group if missing
                    val success = grupoDAO.createDefaultPersonalGroup(idUsuario)
                    if (success) {
                        _grupos.value = grupoDAO.getGruposByUsuario(idUsuario).distinctBy { it.nombre }
                    } else {
                        _grupos.value = emptyList()
                    }
                } else {
                    // Deduplicate to avoid showing multiple "Personal" entries in UI if DB has dupes
                    _grupos.value = grupos.distinctBy { it.nombre }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _grupos.value = emptyList()
            }
        }
    }

    fun createEtiqueta(etiqueta: Etiqueta, idUsuario: Int, idGrupo: Int?) {
        viewModelScope.launch {
            try {
                val success = etiquetaDAO.insertEtiqueta(etiqueta, idUsuario, idGrupo)
                if (success) {
                    // Reload labels to update UI
                   loadEtiquetas(idUsuario)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteEvent(idEvento: Int, idUsuario: Int, date: LocalDate) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = eventoDAO.deleteEvento(idEvento)
                if (success) {
                    // Refresh current view
                    loadEventosForDate(idUsuario, date)
                    // Refresh dots
                    currentMonthLoadParams?.let { (uid, year, month) ->
                        if (date.year == year && date.monthValue == month) {
                            loadDiasConEventos(uid, year, month)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkCanDelete(idUsuario: Int, idEvento: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val evento = eventoDAO.getEventoById(idEvento)
                if (evento == null) {
                    callback(false)
                    return@launch
                }

                // If no label, check if it's the creator
                if (evento.idEtiqueta == null) {
                    callback(evento.idCreador == idUsuario)
                    return@launch
                }

                // If label, find group and check admin
                val idGrupo = etiquetaDAO.getGrupoIdByEtiqueta(evento.idEtiqueta)
                if (idGrupo == null) {
                    // If no group, it's personal (creator check)
                    callback(evento.idCreador == idUsuario)
                } else {
                    val isAdmin = grupoDAO.isAdmin(idUsuario, idGrupo)
                    callback(isAdmin)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false)
            }
        }
    }

    fun loadParticipants(idEvento: Int, callback: (List<com.example.calendario_android_app.model.GroupMember>) -> Unit) {
        viewModelScope.launch {
            try {
                val evento = eventoDAO.getEventoById(idEvento)
                if (evento?.idEtiqueta != null) {
                    val idGrupo = etiquetaDAO.getGrupoIdByEtiqueta(evento.idEtiqueta)
                    if (idGrupo != null) {
                        val participants = grupoDAO.getIntegrantesGrupo(idGrupo)
                        callback(participants)
                    } else {
                        callback(emptyList())
                    }
                } else {
                    callback(emptyList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(emptyList())
            }
        }
    }
}
