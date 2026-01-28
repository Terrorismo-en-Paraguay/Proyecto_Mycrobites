package com.example.calendario_android_app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calendario_android_app.MainActivity
import com.example.calendario_android_app.databinding.FragmentCalendarBinding
import com.example.calendario_android_app.R
import com.example.calendario_android_app.adapters.CalendarAdapter
import com.example.calendario_android_app.adapters.TimeGridAdapter
import com.example.calendario_android_app.adapters.EventAdapter
import com.example.calendario_android_app.models.DayUI
import com.example.calendario_android_app.model.Evento
import com.example.calendario_android_app.viewmodel.EventViewModel
import java.time.LocalDate
import java.time.YearMonth

import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("NewApi") // Desugaring habilitado - soporta Android 7.0+
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    
    private val eventViewModel: EventViewModel by activityViewModels()
    private lateinit var adaptadorEventos: EventAdapter
    private lateinit var searchResultsAdapter: EventAdapter
    private lateinit var adaptadorCalendario: CalendarAdapter
    private var timeGridAdapter: TimeGridAdapter? = null
    
    private var mesAnioActual = YearMonth.now()
    private var fechaSeleccionada: LocalDate = LocalDate.now()
    private var idUsuario: Int = 0 // Will be set from session
    private var diasConEventos: List<Int> = emptyList()
    
    enum class CalendarViewMode {
        MONTH, WEEK, DAY
    }
    
    private var currentViewMode = CalendarViewMode.MONTH
    
    private val formateadorMes = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-ES"))
    private val formateadorEncabezado = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("es-ES"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get user ID from session
        idUsuario = com.example.calendario_android_app.utils.SessionManager.currentUser?.id_usuario ?: 0
        
        if (idUsuario == 0) {
            // No valid session, navigate back to login
            findNavController().navigate(R.id.loginFragment)
            return
        }
        
        // Set user initial in avatar
        val userName = com.example.calendario_android_app.utils.SessionManager.currentClientName ?: "Usuario"
        val initial = userName.firstOrNull()?.toString()?.uppercase() ?: "U"
        binding.tvUserInitial.text = initial
        
        configurarBottomNavigation()
        configurarCalendario()
        configurarEventos()
        configurarListeners()
        observarViewModel()
        
        // Load user data (groups, labels, events)
        eventViewModel.loadGrupos(idUsuario)
        eventViewModel.loadEtiquetas(idUsuario)
        cargarEventos()
    }
    
    private fun configurarBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_month -> {
                    cambiarModoVista(CalendarViewMode.MONTH)
                    true
                }
                R.id.nav_week -> {
                    cambiarModoVista(CalendarViewMode.WEEK)
                    true
                }
                R.id.nav_day -> {
                    cambiarModoVista(CalendarViewMode.DAY)
                    true
                }
                R.id.nav_settings -> {
                    findNavController().navigate(R.id.action_global_settingsFragment)
                    true
                }
                else -> false
            }
        }
        // Restore selection based on current view mode vs settings? 
        // Logic: If we are in CalendarFragment, we are in one of the views.
        // If we navigate AWAY to Settings, we are leaving this fragment, so we don't need to handle "Settings Selected" state HERE.
        // However, if we come BACK, we might want to ensure the right item is selected.
        binding.bottomNavigation.selectedItemId = when(currentViewMode) {
            CalendarViewMode.MONTH -> R.id.nav_month
            CalendarViewMode.WEEK -> R.id.nav_week
            CalendarViewMode.DAY -> R.id.nav_day
        }
    }
    
    private fun cambiarModoVista(modo: CalendarViewMode) {
        if (currentViewMode != modo) {
            currentViewMode = modo
            actualizarVisualizacionMes()
            configurarCalendario()
            cargarEventos() // Reload events when switching views
        }
    }

    private fun configurarCalendario() {
        actualizarVisualizacionMes()

        if (currentViewMode == CalendarViewMode.MONTH) {
             val listaDias = generarDias()
             adaptadorCalendario = CalendarAdapter(
                days = listaDias,
                onDayClick = { dia -> alSeleccionarDia(dia) }
             )
             binding.calendarRecyclerView.layoutManager = GridLayoutManager(context, 7)
             binding.calendarRecyclerView.adapter = adaptadorCalendario
             
             // Show bottom elements for Month view
             binding.layoutWeekdays.visibility = View.VISIBLE
             binding.divider.visibility = View.VISIBLE
             // binding.tvScheduleHeader.visibility = View.VISIBLE // Will be handled by update logic but good to reset
             binding.eventsRecyclerView.visibility = View.VISIBLE
             
             binding.calendarRecyclerView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
             binding.calendarRecyclerView.requestLayout()

             updateWeekdaysHeader()
             timeGridAdapter = null
        } else {
             binding.calendarRecyclerView.layoutManager = LinearLayoutManager(context)

             // 0 implies MATCH_CONSTRAINT in ConstraintLayout, allowing it to fill space between constraints
             binding.calendarRecyclerView.layoutParams.height = 0 
             
             // Hide bottom elements for Day/Week view
             binding.divider.visibility = View.GONE
             binding.tvScheduleHeader.visibility = View.GONE
             binding.eventsRecyclerView.visibility = View.GONE
             
             binding.calendarRecyclerView.requestLayout()

             val hoursList = (0..23).map { String.format(Locale.getDefault(), "%02d:00", it) }
             val isWeek = currentViewMode == CalendarViewMode.WEEK

             val weekStart = if (isWeek) {
                 val diaSemana = fechaSeleccionada.dayOfWeek.value
                 fechaSeleccionada.minusDays((diaSemana - 1).toLong())
             } else {
                 fechaSeleccionada
             }

             // Get current events from ViewModel
             val currentEvents = eventViewModel.eventos.value ?: emptyList()
             timeGridAdapter = TimeGridAdapter(hoursList, isWeek, currentEvents, weekStart) { event ->
                 navegarADetalleEvento(event)
             }
             binding.calendarRecyclerView.adapter = timeGridAdapter

             updateWeekdaysHeader()
        }
    }

    private fun updateWeekdaysHeader() {
         val basePadding = (8 * resources.displayMetrics.density).toInt()
         
         if (currentViewMode == CalendarViewMode.MONTH) {
             binding.layoutWeekdays.setPadding(basePadding, 0, basePadding, 0)
             binding.layoutWeekdays.visibility = View.VISIBLE

             for (i in 0 until binding.layoutWeekdays.childCount) {
                 binding.layoutWeekdays.getChildAt(i).visibility = View.VISIBLE
                 (binding.layoutWeekdays.getChildAt(i) as? TextView)?.text = getDayName(i + 1) // 1=LU
             }
         } else if (currentViewMode == CalendarViewMode.WEEK) {
             // Align with grid: Time col (50dp) + margin (8dp) + base padding (8dp) = 66dp
             val leftPadding = ((50 + 8 + 8) * resources.displayMetrics.density).toInt()
             binding.layoutWeekdays.setPadding(leftPadding, 0, basePadding, 0)
             
             binding.layoutWeekdays.visibility = View.VISIBLE
             val diaSemana = fechaSeleccionada.dayOfWeek.value
             val inicioSemana = fechaSeleccionada.minusDays((diaSemana - 1).toLong())

             for (i in 0 until binding.layoutWeekdays.childCount) {
                 val view = binding.layoutWeekdays.getChildAt(i) as? TextView
                 val date = inicioSemana.plusDays(i.toLong())
                 view?.text = "${getDayName(i+1)} ${date.dayOfMonth}"
                 view?.visibility = View.VISIBLE
             }
         } else { // DAY
             val leftPadding = ((50 + 8 + 8) * resources.displayMetrics.density).toInt()
             binding.layoutWeekdays.setPadding(leftPadding, 0, basePadding, 0)

             for (i in 0 until binding.layoutWeekdays.childCount) {
                  binding.layoutWeekdays.getChildAt(i).visibility = View.GONE
             }
             val view = binding.layoutWeekdays.getChildAt(0) as? TextView
             view?.visibility = View.VISIBLE
             view?.text = fechaSeleccionada.format(DateTimeFormatter.ofPattern("EEEE d", Locale.forLanguageTag("es-ES"))).uppercase()
         }
    }

    private fun getDayName(idx: Int): String {
        return when(idx) {
            1 -> "LU"
            2 -> "MA"
            3 -> "MI"
            4 -> "JU"
            5 -> "VI"
            6 -> "SA"
            7 -> "DO"
            else -> ""
        }
    }

    private fun configurarEventos() {
        adaptadorEventos = EventAdapter(emptyList()) { event ->
            navegarADetalleEvento(event)
        }
        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.eventsRecyclerView.adapter = adaptadorEventos
        
        // Search results adapter
        searchResultsAdapter = EventAdapter(emptyList(), showTimeColumn = false) { event ->
            navegarADetalleEvento(event)
        }
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.searchResultsRecyclerView.adapter = searchResultsAdapter

        actualizarHeaderSchedule()
    }

    private fun configurarListeners() {
        binding.fabMain.setOnClickListener {
            toggleFab()
        }

        binding.fabOptionEvent.setOnClickListener {
            toggleFab()
            findNavController().navigate(R.id.action_calendarFragment_to_createEventFragment)
        }

        binding.fabOptionCalendar.setOnClickListener {
            toggleFab()
            findNavController().navigate(R.id.action_calendarFragment_to_createGroupStep1Fragment)
        }

        binding.fabOptionLabel.setOnClickListener {
            toggleFab()
            val dialog = CreateLabelDialogFragment()
            // Optional: Handle created label
            dialog.onLabelCreated = {
                // Refresh logic if needed
            }
            dialog.show(parentFragmentManager, CreateLabelDialogFragment.TAG)
        }
        
        // Drawer toggle
        binding.btnMenu.setOnClickListener {
            (requireActivity() as? MainActivity)?.openDrawer()
        }

        binding.btnToday.setOnClickListener {
            irAHoy()
        }

        binding.btnPrevMonth.setOnClickListener {
            cambiarFecha(-1)
        }

        binding.btnNextMonth.setOnClickListener {
            cambiarFecha(1)
        }

        binding.tvMonthYear.setOnClickListener { view ->
            mostrarMenuVistas(view)
        }
        
        // Search functionality
        binding.btnSearch.setOnClickListener {
            toggleSearchView()
        }
        
        binding.btnCloseSearch.setOnClickListener {
            toggleSearchView()
            binding.etSearch.text?.clear()
            eventViewModel.setSearchQuery("", idUsuario)
            cargarEventos() // Restore previous view events
        }
        
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                eventViewModel.setSearchQuery(query, idUsuario)
                
                // Show/hide search results view
                if (query.isNotBlank()) {
                    showSearchResults()
                } else {
                    hideSearchResults()
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }
    
    private fun showSearchResults() {
        binding.searchResultsRecyclerView.visibility = View.VISIBLE
        binding.layoutMonthNav.visibility = View.GONE
        binding.layoutWeekdays.visibility = View.GONE
        binding.calendarRecyclerView.visibility = View.GONE
        binding.divider.visibility = View.GONE
        binding.tvScheduleHeader.visibility = View.GONE
        binding.eventsRecyclerView.visibility = View.GONE
    }

    private fun hideSearchResults() {
        binding.searchResultsRecyclerView.visibility = View.GONE

        // Restore visibility of main layout elements
        binding.layoutMonthNav.visibility = View.VISIBLE
        binding.calendarRecyclerView.visibility = View.VISIBLE
        binding.layoutWeekdays.visibility = View.VISIBLE
        binding.tvScheduleHeader.visibility = View.VISIBLE

        // Restore visibility and state based on current view mode
        configurarCalendario()
    }

    private fun toggleSearchView() {
        if (binding.layoutSearch.visibility == View.VISIBLE) {
            binding.layoutSearch.visibility = View.GONE
        } else {
            binding.layoutSearch.visibility = View.VISIBLE
            binding.etSearch.requestFocus()
            // Show keyboard
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(binding.etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun observarViewModel() {
        eventViewModel.eventos.observe(viewLifecycleOwner) { eventos ->
            adaptadorEventos.updateEventos(eventos)
            searchResultsAdapter.updateEventos(eventos) // Update search results
            actualizarHeaderSchedule()

            // Update TimeGridAdapter if active
            if (currentViewMode != CalendarViewMode.MONTH) {
                val isWeek = currentViewMode == CalendarViewMode.WEEK
                val weekStart = if (isWeek) {
                    val diaSemana = fechaSeleccionada.dayOfWeek.value
                    fechaSeleccionada.minusDays((diaSemana - 1).toLong())
                } else {
                    fechaSeleccionada
                }
                timeGridAdapter?.updateEvents(eventos, weekStart)
            }
        }

        eventViewModel.diasConEventos.observe(viewLifecycleOwner) { dias ->
            diasConEventos = dias
            // Only update calendar (dots) if search is NOT active to prevent UI glitches (e.g. weekdays reappearing)
            if (binding.layoutSearch.visibility != View.VISIBLE) {
                configurarCalendario()
            }
        }
    }
    
    private fun alSeleccionarDia(dia: LocalDate) {
        fechaSeleccionada = dia
        if (YearMonth.from(fechaSeleccionada) != mesAnioActual) {
            mesAnioActual = YearMonth.from(fechaSeleccionada)
            actualizarVisualizacionMes()
        }
        
        configurarCalendario()
        
        eventViewModel.loadEventosForDate(idUsuario, fechaSeleccionada)
    }
    
    private fun irAHoy() {
        val hoy = LocalDate.now()
        mesAnioActual = YearMonth.from(hoy)
        fechaSeleccionada = hoy
        actualizarVisualizacionMes()
        configurarCalendario()
        cargarEventos()
    }
    
    private var isFabOpen = false
    
    private fun toggleFab() {
        if (isFabOpen) {
            binding.fabMain.animate().rotation(0f)
            binding.fabOptionEvent.visibility = View.GONE
            binding.tvOptionEvent.visibility = View.GONE
            binding.fabOptionCalendar.visibility = View.GONE
            binding.tvOptionCalendar.visibility = View.GONE
            binding.fabOptionLabel.visibility = View.GONE
            binding.tvOptionLabel.visibility = View.GONE
        } else {
            binding.fabMain.animate().rotation(45f)
            binding.fabOptionEvent.visibility = View.VISIBLE
            binding.tvOptionEvent.visibility = View.VISIBLE
            binding.fabOptionCalendar.visibility = View.VISIBLE
            binding.tvOptionCalendar.visibility = View.VISIBLE
            binding.fabOptionLabel.visibility = View.VISIBLE
            binding.tvOptionLabel.visibility = View.VISIBLE
        }
        isFabOpen = !isFabOpen
    }

    private fun cambiarFecha(desplazamiento: Int) {
        when (currentViewMode) {
            CalendarViewMode.MONTH -> {
                mesAnioActual = mesAnioActual.plusMonths(desplazamiento.toLong())

                cargarDiasConEventos()
            }
            CalendarViewMode.WEEK -> {
                fechaSeleccionada = fechaSeleccionada.plusWeeks(desplazamiento.toLong())
                mesAnioActual = YearMonth.from(fechaSeleccionada)
                cargarEventos()
            }
            CalendarViewMode.DAY -> {
                fechaSeleccionada = fechaSeleccionada.plusDays(desplazamiento.toLong())
                mesAnioActual = YearMonth.from(fechaSeleccionada)
                cargarEventos()
            }
        }
        actualizarVisualizacionMes()
        configurarCalendario()
    }
    
    private fun cargarEventos() {
        if (currentViewMode == CalendarViewMode.MONTH) {
            // Standard load for selected DATE (for bottom list) and dots for MONTH
            eventViewModel.loadEventosForDate(idUsuario, fechaSeleccionada)
            eventViewModel.loadDiasConEventos(idUsuario, mesAnioActual.year, mesAnioActual.monthValue)
        } else {
            // Load events for the visible RANGE (Week or Day)
            val (start, end) = getVisibleRange()
            eventViewModel.loadEventosForRange(idUsuario, start, end)
        }
    }


    
    private fun getVisibleRange(): Pair<LocalDate, LocalDate> {
        return if (currentViewMode == CalendarViewMode.WEEK) {
            val diaSemana = fechaSeleccionada.dayOfWeek.value
            val inicioSemana = fechaSeleccionada.minusDays((diaSemana - 1).toLong())
            val finSemana = inicioSemana.plusDays(6)
            Pair(inicioSemana, finSemana)
        } else {
            Pair(fechaSeleccionada, fechaSeleccionada)
        }
    }
    
    // Kept for Month view compatibility
    private fun cargarDiasConEventos() {
        if (currentViewMode == CalendarViewMode.MONTH) {
            eventViewModel.loadDiasConEventos(idUsuario, mesAnioActual.year, mesAnioActual.monthValue)
        }
    }
    
    private fun actualizarVisualizacionMes() {
        val titulo = when (currentViewMode) {
            CalendarViewMode.MONTH -> mesAnioActual.format(formateadorMes)
            CalendarViewMode.WEEK -> "Semana ${fechaSeleccionada.format(DateTimeFormatter.ofPattern("d MMM"))}"
            CalendarViewMode.DAY -> fechaSeleccionada.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
        }
        
        binding.tvMonthYear.text = titulo.replaceFirstChar { it.uppercase() }
    }
    
    private fun actualizarHeaderSchedule() {
        if (binding.layoutSearch.visibility == View.VISIBLE) {
            binding.tvScheduleHeader.visibility = View.GONE
            return
        }
        binding.tvScheduleHeader.visibility = View.VISIBLE
        binding.tvScheduleHeader.text = fechaSeleccionada.format(formateadorEncabezado).uppercase()
    }
    
    private fun generarDias(): List<DayUI> {
        return when (currentViewMode) {
            CalendarViewMode.MONTH -> generarDiasMes()
            CalendarViewMode.WEEK -> generarDiasSemana()
            CalendarViewMode.DAY -> generarDiasDia()
        }
    }
    
    private fun generarDiasMes(): List<DayUI> {
        val dias = mutableListOf<DayUI>()
        
        val primerDiaDelMes = mesAnioActual.atDay(1).dayOfWeek.value
        val diasEnMes = mesAnioActual.lengthOfMonth()
        
        val mesAnterior = mesAnioActual.minusMonths(1)
        val diasMesAnterior = mesAnterior.lengthOfMonth()
        
        for (i in 0 until (primerDiaDelMes - 1)) {
            val dia = diasMesAnterior - (primerDiaDelMes - 2 - i)
            val date = mesAnterior.atDay(dia)
            dias.add(DayUI(date, isCurrentMonth = false, hasEvent = tieneEvento(date), isSelected = esSeleccionado(date)))
        }
        
        for (i in 1..diasEnMes) {
            val date = mesAnioActual.atDay(i)
            dias.add(DayUI(date, isCurrentMonth = true, hasEvent = tieneEvento(date), isSelected = esSeleccionado(date)))
        }
        
        val totalCells = dias.size
        val remaining = 42 - totalCells
        val mesSiguiente = mesAnioActual.plusMonths(1)
        
        for (i in 1..remaining) {
            val date = mesSiguiente.atDay(i)
            dias.add(DayUI(date, isCurrentMonth = false, hasEvent = tieneEvento(date), isSelected = esSeleccionado(date)))
        }
        
        return dias
    }
    
    private fun generarDiasSemana(): List<DayUI> {
        val dias = mutableListOf<DayUI>()
        val diaSemana = fechaSeleccionada.dayOfWeek.value // 1 (Lun) - 7 (Dom)
        val inicioSemana = fechaSeleccionada.minusDays((diaSemana - 1).toLong())
        
        for (i in 0..6) {
            val date = inicioSemana.plusDays(i.toLong())
            val isCurrentMonth = date.month == mesAnioActual.month
            dias.add(DayUI(date, isCurrentMonth = isCurrentMonth, hasEvent = tieneEvento(date), isSelected = esSeleccionado(date)))
        }
        return dias
    }
    
    private fun generarDiasDia(): List<DayUI> {
        return listOf(DayUI(
            fechaSeleccionada, 
            isCurrentMonth = true, 
            hasEvent = tieneEvento(fechaSeleccionada), 
            isSelected = true
        ))
    }
    
    private fun tieneEvento(date: LocalDate): Boolean {
       return if (date.year == mesAnioActual.year && date.monthValue == mesAnioActual.monthValue) {
            diasConEventos.contains(date.dayOfMonth)
        } else {
            false
        }
    }
    
    private fun esSeleccionado(date: LocalDate): Boolean {
        return date.isEqual(fechaSeleccionada)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun mostrarMenuVistas(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_calendar_view, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_view_month -> {
                    cambiarModoVista(CalendarViewMode.MONTH)
                    true
                }
                R.id.menu_view_week -> {
                    cambiarModoVista(CalendarViewMode.WEEK)
                    true
                }
                R.id.menu_view_day -> {
                    cambiarModoVista(CalendarViewMode.DAY)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun navegarADetalleEvento(evento: Evento) {
        if (evento.idEvento > 0) { 
            val bundle = Bundle().apply {
                putInt("idEvento", evento.idEvento)
            }
            findNavController().navigate(R.id.action_calendarFragment_to_eventDetailFragment, bundle)
        }
    }
}
