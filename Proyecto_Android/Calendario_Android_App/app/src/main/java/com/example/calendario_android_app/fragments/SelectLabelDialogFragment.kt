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

class SelectLabelDialogFragment : DialogFragment() {

    private var _binding: DialogSelectLabelBinding? = null
    private val binding get() = _binding!!

    // Callback for selection
    var onLabelSelected: ((Etiqueta) -> Unit)? = null

    // ViewModel (New instance or shared with parent/activity depending on scope needs. 
    // Here getting a new instance to load independent data or can share with Activity)
    private val eventViewModel: EventViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSelectLabelBinding.inflate(inflater, container, false)
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
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupListeners() {
        binding.btnCancelSelection.setOnClickListener {
            dismiss()
        }

        binding.btnCreateNewLabel.setOnClickListener {
            // Open Create Dialog
            val createDialog = CreateLabelDialogFragment()
            createDialog.onLabelCreated = {
                // Refresh list when a new label is created
                loadLabels() 
            }
            createDialog.show(parentFragmentManager, CreateLabelDialogFragment.TAG)
            // We don't dismiss this one, we wait for return? 
            // Or we can let the user see the new label in the list after creation.
        }
    }

    private fun loadLabels() {
        val user = SessionManager.currentUser
        val userId = user?.id_usuario ?: 1

        // Observe
        eventViewModel.etiquetas.observe(viewLifecycleOwner) { etiquetas ->
            populateLabelsList(etiquetas)
        }

        // Trigger load
        eventViewModel.loadEtiquetas(userId)
    }

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

    private fun createLabelItemView(etiqueta: Etiqueta): View {
        // Simple Horizontal Layout: [Color Circle] [Name]
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24) // Bottom margin
            }
            gravity = android.view.Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            setBackgroundResource(R.drawable.bg_input_field) // Reusing background for ripple effect/shape if compatible
            // Or just plain background with ripple
            background = ContextCompat.getDrawable(context, R.drawable.bg_input_field)
            setPadding(32, 24, 32, 24)
            
            setOnClickListener {
                onLabelSelected?.invoke(etiqueta)
                dismiss()
            }
        }

        // Color Circle
        val colorView = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(32, 32).apply {
                marginEnd = 32
            }
            background = ContextCompat.getDrawable(context, R.drawable.bg_color_circle)
            background.setTint(Color.parseColor(etiqueta.color))
        }

        // Name
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
