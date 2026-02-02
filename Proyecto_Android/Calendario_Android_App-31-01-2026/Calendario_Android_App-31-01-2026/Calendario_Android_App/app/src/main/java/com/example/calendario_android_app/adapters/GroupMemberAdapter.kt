package com.example.calendario_android_app.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.calendario_android_app.R
import com.example.calendario_android_app.model.GroupMember

/**
 * Adaptador para lista de miembros de grupo.
 * Muestra email, rol y opción de eliminar.
 */
class GroupMemberAdapter(
    private var members: List<GroupMember>,
    private val onRemoveClick: (GroupMember) -> Unit
) : RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitial: TextView = view.findViewById(R.id.tv_member_initial)
        val tvEmail: TextView = view.findViewById(R.id.tv_member_email)
        val tvRole: TextView = view.findViewById(R.id.tv_member_role)
        val btnRemove: ImageView = view.findViewById(R.id.btn_remove_member)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        
        // Set initial
        val initial = member.userName?.firstOrNull()?.toString()?.uppercase() 
            ?: member.email.firstOrNull()?.toString()?.uppercase() 
            ?: "?"
        holder.tvInitial.text = initial
        
        // Set email
        holder.tvEmail.text = member.email
        
        // Set role with color
        val roleText = when(member.role) {
            "admin" -> "Admin"
            "editor" -> "Editor"
            "miembro" -> "Miembro"
            else -> member.role
        }
        holder.tvRole.text = roleText
        
        // Remove button
        holder.btnRemove.setOnClickListener {
            onRemoveClick(member)
        }
    }

    override fun getItemCount() = members.size

    fun updateMembers(newMembers: List<GroupMember>) {
        members = newMembers
        notifyDataSetChanged()
    }
}
