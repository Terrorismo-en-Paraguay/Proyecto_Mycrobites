package com.example.calendario_android_app.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.calendario_android_app.R
import com.example.calendario_android_app.model.Etiqueta
import com.example.calendario_android_app.utils.SessionManager
import com.example.calendario_android_app.viewmodel.CreateGroupViewModel
import com.example.calendario_android_app.viewmodel.EventViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class CreateGroupStep2Fragment : Fragment() {

    private val viewModel: CreateGroupViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_group_step2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<View>(R.id.btn_back)
        val tvBack = view.findViewById<View>(R.id.tv_back)
        val btnNext = view.findViewById<MaterialButton>(R.id.btn_next)
        val btnAddNew = view.findViewById<View>(R.id.btn_add_new_label)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_labels)
        val layoutSelectLabels = view.findViewById<View>(R.id.layout_select_labels)

        
        layoutSelectLabels.setOnClickListener {
            val dialog = SelectMultiLabelDialogFragment()
            
            val currentSelection = viewModel.selectedLabels.value ?: emptyList()
            dialog.preSelectedIds = currentSelection.map { it.idEtiqueta }
            
            dialog.onLabelsSelected = { selectedList ->
                
                
                viewModel.replaceAllLabels(selectedList)
            }
            dialog.show(childFragmentManager, SelectMultiLabelDialogFragment.TAG)
        }

        viewModel.selectedLabels.observe(viewLifecycleOwner) { selected ->
            chipGroup.removeAllViews()
            for (label in selected) {
                val chip = Chip(context)
                chip.text = label.nombre
                chip.isCloseIconVisible = true
                
                chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(label.color))
                chip.chipStrokeWidth = 0f
                
                chip.setTextColor(Color.parseColor("#1a1a1a"))
                chip.setCloseIconTintResource(android.R.color.black)
                
                chip.setOnCloseIconClickListener {
                    viewModel.removeLabel(label)
                }
                
                chipGroup.addView(chip)
            }
        }

        btnAddNew.setOnClickListener {
            showCreateLabelDialog()
        }

        val backListener = View.OnClickListener { findNavController().navigateUp() }
        btnBack.setOnClickListener(backListener)
        tvBack.setOnClickListener(backListener)

        btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_createGroupStep2Fragment_to_createGroupStep3Fragment)
        }
    }

    private fun showCreateLabelDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_label, null)
        
        dialogView.findViewById<View>(R.id.spinner_calendar).visibility = View.GONE
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.et_label_name)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btn_save_label)
        val btnClose = dialogView.findViewById<View>(R.id.btn_close_dialog)
        val colorsLayout = dialogView.findViewById<android.widget.LinearLayout>(R.id.layout_colors)
        val previewContainer = dialogView.findViewById<View>(R.id.preview_chip_container)
        val previewText = dialogView.findViewById<TextView>(R.id.preview_text)
        
        var selectedColor = "#8A2BE2"

        fun updatePreview() {
            previewText.text = etName.text.toString().ifBlank { "Nombre" }
            previewContainer.background.setTint(Color.parseColor(selectedColor))
        }
        updatePreview()

        fun updateColorSelectionUI() {
            for (i in 0 until colorsLayout.childCount) {
                val colorView = colorsLayout.getChildAt(i)
                val colorCode = colorView.tag.toString()
                
                if (colorCode == selectedColor) {
                    colorView.setBackgroundResource(R.drawable.bg_circle_selected)
                } else {
                    colorView.setBackgroundResource(R.drawable.bg_color_circle)
                }
                colorView.background.setTint(Color.parseColor(colorCode))
            }
        }
        updateColorSelectionUI()

        for (i in 0 until colorsLayout.childCount) {
            val colorView = colorsLayout.getChildAt(i)
            colorView.setOnClickListener { v ->
                selectedColor = v.tag.toString()
                updateColorSelectionUI()
                updatePreview()
            }
        }

        etName.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePreview()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isNotEmpty()) {
                val newLabel = Etiqueta(nombre = name, color = selectedColor)
                viewModel.addLabel(newLabel)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
