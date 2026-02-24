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

        checkSession()

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
            
            val initialUser = com.example.calendario_android_app.model.Usuario(
                id_usuario = userId,
                id_cliente = "0", 
                correo = "",
                password_hash = "",
                rol = "user"
            )
            com.example.calendario_android_app.utils.SessionManager.currentUser = initialUser
            com.example.calendario_android_app.utils.SessionManager.currentClientName = username

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

            val viewModel = androidx.lifecycle.ViewModelProvider(this)[com.example.calendario_android_app.viewmodel.EventViewModel::class.java]
            viewModel.loadGrupos(userId)
            viewModel.loadEtiquetas(userId)

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
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                updateDrawerHeader()
            }
        })
        
        updateDrawerHeader()

        setupCollapsibleSection(R.id.section_calendars_header, R.id.container_calendars_list, R.id.iv_expand_calendars)
        setupCollapsibleSection(R.id.section_labels_header, R.id.container_labels_list, R.id.iv_expand_labels)

        findViewById<View>(R.id.btn_settings)?.setOnClickListener {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            navHostFragment?.navController?.navigate(R.id.action_global_settingsFragment)
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<View>(R.id.btn_help)?.setOnClickListener {
             Toast.makeText(this, "Ayuda: Próximamente", Toast.LENGTH_SHORT).show()
             drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<View>(R.id.btn_logout)?.setOnClickListener {
            com.example.calendario_android_app.utils.SessionManager.clearSession()
            
            val sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
            sharedPrefs.edit().clear().apply()
            
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            drawerLayout.closeDrawer(GravityCompat.START)
            
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            navHostFragment?.navController?.navigate(R.id.loginFragment) 
                ?: run {
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
                icon?.setImageResource(R.drawable.ic_arrow_drop_down)
            } else {
                content?.visibility = View.VISIBLE
                icon?.setImageResource(R.drawable.ic_expand_less)
            }
        }
    }
    
    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun setupDrawerObservers() {
        val user = com.example.calendario_android_app.utils.SessionManager.currentUser
        
        val viewModel = androidx.lifecycle.ViewModelProvider(this)[com.example.calendario_android_app.viewmodel.EventViewModel::class.java]

        viewModel.grupos.observe(this) { grupos ->
             val containerCalendars = findViewById<LinearLayout>(R.id.container_calendars_list)
             containerCalendars?.removeAllViews()
             
             for (grupo in grupos) {
                 val itemView = layoutInflater.inflate(R.layout.item_calendar_drawer, containerCalendars, false)
                 val tvName = itemView.findViewById<TextView>(R.id.tv_calendar_name)
                 val checkBox = itemView.findViewById<android.widget.CheckBox>(R.id.cb_calendar_visibility)

                 tvName.text = grupo.nombre
                 
                 val color = if (grupo.nombre == "Personal") {
                     android.graphics.Color.parseColor("#9C27B0")
                 } else {
                     androidx.core.content.ContextCompat.getColor(this, R.color.primary_purple)
                 }

                 checkBox.buttonTintList = android.content.res.ColorStateList.valueOf(color)
                 
                 checkBox.setOnCheckedChangeListener(null)
                 checkBox.isChecked = viewModel.isGroupVisible(grupo.idGrupo)
                 
                 val toggleAction = { isChecked: Boolean ->
                     viewModel.toggleGroupVisibility(grupo.idGrupo, isChecked)
                 }

                 checkBox.setOnCheckedChangeListener { _, isChecked ->
                     toggleAction(isChecked)
                 }

                 itemView.setOnClickListener {
                     checkBox.isChecked = !checkBox.isChecked
                 }
                 
                 tvName.isClickable = false
                 tvName.isFocusable = false
                 
                 containerCalendars?.addView(itemView)
             }
        }
        
        viewModel.etiquetas.observe(this) { etiquetas ->
             val containerLabels = findViewById<LinearLayout>(R.id.container_labels_list)
             containerLabels?.removeAllViews()

             for (etiqueta in etiquetas) {
                 val checkBox = android.widget.CheckBox(this)
                 checkBox.layoutParams = android.widget.LinearLayout.LayoutParams(
                     android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                     android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics).toInt()
                 )
                 checkBox.text = etiqueta.nombre
                 checkBox.setTextColor(android.graphics.Color.WHITE)
                 
                 val color = try {
                     android.graphics.Color.parseColor(etiqueta.color)
                 } catch (e: Exception) {
                     android.graphics.Color.GRAY
                 }
                 
                 checkBox.buttonDrawable = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.selector_checkbox_tick)
                 checkBox.buttonTintList = android.content.res.ColorStateList.valueOf(color)
                 
                 checkBox.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
                 checkBox.setPadding(
                     android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics).toInt(), 0, 0, 0
                 )
                 
                 checkBox.setOnCheckedChangeListener(null)
                 val isVisible = viewModel.isLabelVisible(etiqueta.idEtiqueta)
                 checkBox.isChecked = isVisible

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
        
    }
}