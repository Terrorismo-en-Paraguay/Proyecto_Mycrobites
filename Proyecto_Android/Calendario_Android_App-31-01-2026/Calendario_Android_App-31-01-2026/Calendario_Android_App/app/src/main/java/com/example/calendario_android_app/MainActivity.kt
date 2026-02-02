package com.example.calendario_android_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import com.example.calendario_android_app.databinding.ActivityMainBinding
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.calendario_android_app.worker.DailyReminderWorker
import java.util.concurrent.TimeUnit
import java.util.Calendar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/**
 * Actividad principal de la aplicación.
 * Funcionalidades:
 * - Gestión del Navigation Drawer con filtros de calendarios y etiquetas
 * - Verificación y restauración de sesión desde SharedPreferences
 * - Programación de recordatorios diarios mediante WorkManager
 * - Navegación entre fragmentos
 * - Actualización dinámica del header del drawer con datos del usuario
 */
class MainActivity : AppCompatActivity() {
    
    // Binding para la actividad incluyendo el drawer
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupDrawerLogic()
        setupDrawerObservers()

        // Check Session
        checkSession()

        // Schedule Daily Reminder
        scheduleDailyReminder()
    }

    private fun scheduleDailyReminder() {
        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun calculateInitialDelay(): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        // Schedule for 8:00 AM
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return calendar.timeInMillis - now
    }

    private fun checkSession() {
        val sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)
        
        if (isLoggedIn) {
            val userId = sharedPrefs.getInt("userId", 0)
            val username = sharedPrefs.getString("username", "Usuario")
            
            // Re-instantiate basic session info immediately for UI
            val initialUser = com.example.calendario_android_app.model.Usuario(
                id_usuario = userId,
                id_cliente = "0", 
                correo = "",
                password_hash = "",
                rol = "user"
            )
            com.example.calendario_android_app.utils.SessionManager.currentUser = initialUser
            com.example.calendario_android_app.utils.SessionManager.currentClientName = username

            // Use a coroutine to fetch FULL user data from DB
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val usuarioDAO = com.example.calendario_android_app.dao.impl.UsuarioDAOImpl()
                    val fullUser = usuarioDAO.getUserById(userId)
                    val userInfo = usuarioDAO.getUserInfo(userId)
                    
                    withContext(Dispatchers.Main) {
                        if (fullUser != null) {
                            com.example.calendario_android_app.utils.SessionManager.currentUser = fullUser
                            if (userInfo != null) {
                                com.example.calendario_android_app.utils.SessionManager.currentClientName = userInfo.second
                            }
                            updateDrawerHeader()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Preload Drawer Data (Calendars & Labels)
            val viewModel = androidx.lifecycle.ViewModelProvider(this)[com.example.calendario_android_app.viewmodel.EventViewModel::class.java]
            viewModel.loadGrupos(userId)
            viewModel.loadEtiquetas(userId)

            // Post to ensure NavHost is ready
             binding.root.post {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                val navController = navHostFragment?.navController
                if (navController?.currentDestination?.id == R.id.loginFragment) {
                    navController.navigate(R.id.action_loginFragment_to_calendarFragment)
                }
            }
        }
    }
    
    private fun setupDrawerLogic() {
        val drawerLayout = binding.drawerLayout
        // Drawer Listener for dynamic updates
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                updateDrawerHeader()
                // Data is now preloaded in checkSession()
            }
        })
        
        // Initial populate
        updateDrawerHeader()

        // Handle Collapsible Sections
        setupCollapsibleSection(R.id.section_calendars_header, R.id.container_calendars_list, R.id.iv_expand_calendars)
        setupCollapsibleSection(R.id.section_labels_header, R.id.container_labels_list, R.id.iv_expand_labels)

        // Footer Actions
        findViewById<View>(R.id.btn_settings)?.setOnClickListener {
            Toast.makeText(this, "Configuración: Próximamente", Toast.LENGTH_SHORT).show()
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<View>(R.id.btn_help)?.setOnClickListener {
             Toast.makeText(this, "Ayuda: Próximamente", Toast.LENGTH_SHORT).show()
             drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<View>(R.id.btn_logout)?.setOnClickListener {
            // Logout Logic
            com.example.calendario_android_app.utils.SessionManager.clearSession()
            
            // Clear Prefs
            val sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()
            
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            drawerLayout.closeDrawer(GravityCompat.START)
            
            // Navigate to Login
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            navHostFragment?.navController?.navigate(R.id.loginFragment) 
                ?: run {
                      // Fallback
                      val navController = androidx.navigation.Navigation.findNavController(this, R.id.nav_host_fragment)
                      navController.navigate(R.id.loginFragment)
                }
        }
    }
    
    private fun setupCollapsibleSection(headerId: Int, contentId: Int, iconId: Int) {
        val header = findViewById<View>(headerId)
        val content = findViewById<View>(contentId)
        val icon = findViewById<ImageView>(iconId)

        header?.setOnClickListener {
            if (content?.visibility == View.VISIBLE) {
                content.visibility = View.GONE
                icon?.setImageResource(R.drawable.ic_arrow_drop_down) // Or rotate
            } else {
                content?.visibility = View.VISIBLE
                icon?.setImageResource(R.drawable.ic_expand_less)
            }
        }
    }
    
    // Public method to allow fragments to open drawer
    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun setupDrawerObservers() {
        val user = com.example.calendario_android_app.utils.SessionManager.currentUser
        // We need a way to get the ViewModel. 
        // Note: onCreate runs once, but user might be null initially if not logged in.
        // However, ViewModelProvider(this) gives the Activity's ViewModel.
        // We should observe always, but the data might be empty if not logged in.
        
        val viewModel = androidx.lifecycle.ViewModelProvider(this)[com.example.calendario_android_app.viewmodel.EventViewModel::class.java]

        // Observe Groups (Calendars)
        viewModel.grupos.observe(this) { grupos ->
             val containerCalendars = findViewById<LinearLayout>(R.id.container_calendars_list)
             containerCalendars?.removeAllViews()
             
             for (grupo in grupos) {
                 val itemView = layoutInflater.inflate(R.layout.item_calendar_drawer, containerCalendars, false)
                 val tvName = itemView.findViewById<TextView>(R.id.tv_calendar_name)
                 val checkBox = itemView.findViewById<android.widget.CheckBox>(R.id.cb_calendar_visibility)

                 tvName.text = grupo.nombre
                 
                 // Color Logic for Groups
                 val color = if (grupo.nombre == "Personal") {
                     android.graphics.Color.parseColor("#9C27B0") // Purple for Personal
                 } else {
                     androidx.core.content.ContextCompat.getColor(this, R.color.primary_purple)
                 }

                 checkBox.buttonTintList = android.content.res.ColorStateList.valueOf(color)
                 
                 // Sync visibility state
                 checkBox.setOnCheckedChangeListener(null)
                 checkBox.isChecked = viewModel.isGroupVisible(grupo.idGrupo)
                 
                 // Toggle Action: clicking anywhere on the item (including the tick) toggles visibility
                 val toggleAction = { isChecked: Boolean ->
                     viewModel.toggleGroupVisibility(grupo.idGrupo, isChecked)
                 }

                 checkBox.setOnCheckedChangeListener { _, isChecked ->
                     toggleAction(isChecked)
                 }

                 itemView.setOnClickListener {
                     checkBox.isChecked = !checkBox.isChecked
                 }
                 
                 // Ensure tvName doesn't block clicks to itemView and doesn't navigate
                 tvName.isClickable = false
                 tvName.isFocusable = false
                 
                 containerCalendars?.addView(itemView)
             }
        }
        
        // Observe Labels
        viewModel.etiquetas.observe(this) { etiquetas ->
             val containerLabels = findViewById<LinearLayout>(R.id.container_labels_list)
             containerLabels?.removeAllViews()

             // Show all available labels in the drawer
             for (etiqueta in etiquetas) {
                 val checkBox = android.widget.CheckBox(this)
                 checkBox.layoutParams = android.widget.LinearLayout.LayoutParams(
                     android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                     android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics).toInt()
                 )
                 checkBox.text = etiqueta.nombre
                 checkBox.setTextColor(android.graphics.Color.WHITE)
                 
                 // Color Logic for Labels
                 val color = try {
                     android.graphics.Color.parseColor(etiqueta.color)
                 } catch (e: Exception) {
                     android.graphics.Color.GRAY // Fallback
                 }
                 
                 checkBox.buttonDrawable = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.selector_checkbox_tick)
                 checkBox.buttonTintList = android.content.res.ColorStateList.valueOf(color)
                 
                 checkBox.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
                 checkBox.setPadding(
                     android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt(), 0, 0, 0
                 )
                 
                 // Sync state
                 checkBox.setOnCheckedChangeListener(null)
                 val isVisible = viewModel.isLabelVisible(etiqueta.idEtiqueta)
                 checkBox.isChecked = isVisible

                 // Listener
                 checkBox.setOnCheckedChangeListener { _, isChecked ->
                     viewModel.toggleLabelVisibility(etiqueta.idEtiqueta, isChecked)
                 }
                 
                 containerLabels?.addView(checkBox)
             }
        }
    }

    private fun updateDrawerHeader() {
        val user = com.example.calendario_android_app.utils.SessionManager.currentUser
        val userName = com.example.calendario_android_app.utils.SessionManager.currentClientName ?: "Usuario"
        val userEmail = user?.correo ?: "usuario@email.com"
        val initial = userName.firstOrNull()?.toString()?.uppercase() ?: "U"

        findViewById<TextView>(R.id.tv_user_name)?.text = userName
        findViewById<TextView>(R.id.tv_user_email)?.text = userEmail
        findViewById<TextView>(R.id.tv_avatar_initial)?.text = initial
        
        // Note: Observers are now set up in setupDrawerObservers called in onCreate.
        // Here we can trigger data load if needed, or rely on caching.
        // Triggering load happens in onDrawerOpened listener.
    }
}