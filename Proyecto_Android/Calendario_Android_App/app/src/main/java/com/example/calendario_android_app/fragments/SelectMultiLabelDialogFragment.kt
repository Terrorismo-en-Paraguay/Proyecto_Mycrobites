package com.example.calendario_android_app.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.calendario_android_app.R
import com.example.calendario_android_app.model.Etiqueta
import com.example.calendario_android_app.utils.SessionManager
import com.example.calendario_android_app.viewmodel.EventViewModel

class SelectMultiLabelDialogFragment : DialogFragment() {

    private lateinit var container: LinearLayout
    private lateinit var btnCancel: View
    private lateinit var btnConfirm: View
    
    // Callback: Returns list of selected labels
    var onLabelsSelected: ((List<Etiqueta>) -> Unit)? = null
    
    // Pre-selected IDs to check them initially
    var preSelectedIds: List<Int> = emptyList()

    private val eventViewModel: EventViewModel by viewModels()
    private val selectedItems = mutableListOf<Etiqueta>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.dialog_select_multi_label, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        container = view.findViewById(R.id.container_labels_multi_selection)
        btnCancel = view.findViewById(R.id.btn_cancel_multi)
        btnConfirm = view.findViewById(R.id.btn_confirm_multi)
        
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
        btnCancel.setOnClickListener { dismiss() }
        
        btnConfirm.setOnClickListener {
            onLabelsSelected?.invoke(selectedItems)
            dismiss()
        }
    }

    private fun loadLabels() {
        val userId = SessionManager.currentUser?.id_usuario ?: 1
        eventViewModel.loadEtiquetas(userId)
        
        eventViewModel.etiquetas.observe(viewLifecycleOwner) { etiquetas ->
            populateList(etiquetas)
        }
    }

    private fun populateList(etiquetas: List<Etiqueta>) {
        container.removeAllViews()
        selectedItems.clear()
        
        // Handle pre-selection
        etiquetas.forEach { if (preSelectedIds.contains(it.idEtiqueta)) selectedItems.add(it) }

        if (etiquetas.isEmpty()) {
            val emptyView = TextView(context).apply {
                text = "No tienes etiquetas creadas"
                setTextColor(Color.parseColor("#AAAAAA"))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 32, 0, 32)
            }
            container.addView(emptyView)
            return
        }

        for (etiqueta in etiquetas) {
            val itemView = createItemView(etiqueta)
            container.addView(itemView)
        }
    }

    private fun createItemView(etiqueta: Etiqueta): View {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
            gravity = android.view.Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            background = ContextCompat.getDrawable(context, R.drawable.bg_input_field)
            setPadding(32, 24, 32, 24)
        }

        // Color Circle
        val colorView = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(32, 32).apply { marginEnd = 32 }
            background = ContextCompat.getDrawable(context, R.drawable.bg_color_circle)
            background.setTint(Color.parseColor(etiqueta.color))
        }

        // Name
        val nameView = TextView(context).apply {
            text = etiqueta.nombre
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        // Checkbox
        val checkBox = CheckBox(context).apply {
            buttonTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#8A2BE2"))
            isChecked = preSelectedIds.contains(etiqueta.idEtiqueta)
            isClickable = false // Let layout handle click
        }

        layout.addView(colorView)
        layout.addView(nameView)
        layout.addView(checkBox)
        
        layout.setOnClickListener {
            checkBox.isChecked = !checkBox.isChecked
            if (checkBox.isChecked) {
                if (!selectedItems.any { it.idEtiqueta == etiqueta.idEtiqueta }) {
                    selectedItems.add(etiqueta)
                }
            } else {
                selectedItems.removeAll { it.idEtiqueta == etiqueta.idEtiqueta }
            }
        }

        return layout
    }

    companion object {
        const val TAG = "SelectMultiLabelDialog"
    }
}
