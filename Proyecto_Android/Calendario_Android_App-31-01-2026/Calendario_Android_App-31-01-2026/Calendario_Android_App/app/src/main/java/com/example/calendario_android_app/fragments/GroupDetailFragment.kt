package com.example.calendario_android_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.calendario_android_app.R
import com.example.calendario_android_app.dao.impl.GrupoDAOImpl
import kotlinx.coroutines.launch

/**
 * Fragmento de detalle de grupo.
 * Muestra información del grupo y permite salir del mismo.
 */
class GroupDetailFragment : Fragment() {

    private var idGrupo: Int = -1
    private val grupoDAO = GrupoDAOImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idGrupo = arguments?.getInt("idGrupo") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_group_detail, container, false)

        val btnBack = view.findViewById<ImageView>(R.id.btn_back)
        val tvGroupName = view.findViewById<TextView>(R.id.tv_group_name)
        val tvGroupDescription = view.findViewById<TextView>(R.id.tv_group_description)
        val btnLeaveGroup = view.findViewById<View>(R.id.btn_leave_group)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        if (idGrupo != -1) {
            loadGroupDetails(tvGroupName, tvGroupDescription)
        }

        btnLeaveGroup.setOnClickListener {
            // Placeholder for leave group logic
            Toast.makeText(context, "Funcionalidad de salir del grupo próximamente", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun loadGroupDetails(tvName: TextView, tvDesc: TextView) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val grupo = grupoDAO.getGrupoById(idGrupo)
                grupo?.let {
                    tvName.text = it.nombre
                    tvDesc.text = it.descripcion ?: "Sin descripción"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
