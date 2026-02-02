package com.example.calendario_android_app.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.calendario_android_app.R
import com.example.calendario_android_app.databinding.DialogSelectLabelBinding
import com.example.calendario_android_app.model.Etiqueta
import com.example.calendario_android_app.utils.SessionManager
import com.example.calendario_android_app.viewmodel.EventViewModel

/**
 * Diálogo flotante que permite al usuario seleccionar una etiqueta para un evento.
 * Muestra tanto las etiquetas personales como las de los grupos compartidos.
 */
class SelectLabelDialogFragment : DialogFragment() {

    private var _binding: DialogSelectLabelBinding? = null
    private val binding get() = _binding!!

    // Callback para devolver la etiqueta seleccionada al fragmento de origen.
    var onLabelSelected: ((Etiqueta) -> Unit)? = null

    private val eventViewModel: EventViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSelectLabelBinding.inflate(inflater, container, false)
        // Fondo transparente para que se aplique el redondeo del diseño personalizado
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        loadLabels()
    }

    override fun onStart() {
        super.onStart()
        // Ajuste de ancho completo para el diálogo
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupListeners() {
        binding.btnCancelSelection.setOnClickListener {
            dismiss()
        }

        // Acceso rápido para crear una etiqueta nueva desde el selector
        binding.btnCreateNewLabel.setOnClickListener {
            val createDialog = CreateLabelDialogFragment()
            createDialog.onLabelCreated = {
                // Al crear una nueva, recargamos la lista para mostrarla de inmediato.
                loadLabels() 
            }
            createDialog.show(parentFragmentManager, CreateLabelDialogFragment.TAG)
        }
    }

    /**
     * Inicia la carga de etiquetas desde la base de datos a través del ViewModel.
     */
    private fun loadLabels() {
        val user = SessionManager.currentUser
        val userId = user?.id_usuario ?: 1

        // Observamos los cambios en la lista de etiquetas
        eventViewModel.etiquetas.observe(viewLifecycleOwner) { etiquetas ->
            populateLabelsList(etiquetas)
        }

        // Petición de recarga al servidor/DB
        eventViewModel.loadEtiquetas(userId)
    }

    /**
     * Construye dinámicamente la lista visual de etiquetas en el contenedor.
     */
    private fun populateLabelsList(etiquetas: List<Etiqueta>) {
        binding.containerLabelsSelection.removeAllViews()

        if (etiquetas.isEmpty()) {
            val emptyView = TextView(context).apply {
                text = "No tienes etiquetas creadas"
                setTextColor(Color.parseColor("#AAAAAA"))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 32, 0, 32)
            }
            binding.containerLabelsSelection.addView(emptyView)
            return
        }

        for (etiqueta in etiquetas) {
            val itemView = createLabelItemView(etiqueta)
            binding.containerLabelsSelection.addView(itemView)
        }
    }

    /**
     * Crea programáticamente una vista (fila) para cada etiqueta.
     * Incluye un círculo de color y el nombre.
     */
    private fun createLabelItemView(etiqueta: Etiqueta): View {
        // Contenedor horizontal [Círculo Color] [Nombre]
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
            gravity = android.view.Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            background = ContextCompat.getDrawable(context, R.drawable.bg_input_field)
            setPadding(32, 24, 32, 24)
            
            setOnClickListener {
                onLabelSelected?.invoke(etiqueta) // Notificamos la selección
                dismiss() // Cerramos el selector
            }
        }

        // Indicador de color (Círculo)
        val colorView = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(32, 32).apply {
                marginEnd = 32
            }
            background = ContextCompat.getDrawable(context, R.drawable.bg_color_circle)
            background.setTint(Color.parseColor(etiqueta.color))
        }

        // Etiqueta de texto (Nombre)
        val nameView = TextView(context).apply {
            text = etiqueta.nombre
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        }

        layout.addView(colorView)
        layout.addView(nameView)

        return layout
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "SelectLabelDialog"
    }
}


