package com.example.calendario_android_app.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.calendario_android_app.R
import com.example.calendario_android_app.models.DayUI
import java.time.LocalDate

/**
 * Adaptador para la vista de calendario en grid.
 * Muestra días del mes con indicadores de eventos y selección.
 */
class CalendarAdapter(
    private val days: List<DayUI>,
    private val onDayClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tv_day_number)
        val viewDot: View = view.findViewById(R.id.view_event_dot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        
        holder.tvDay.text = day.date.dayOfMonth.toString()
        holder.itemView.setOnClickListener { onDayClick(day.date) }

        // Styling based on state
        val context = holder.itemView.context
        
        // Transparency for non-current month days
        holder.tvDay.alpha = if (day.isCurrentMonth) 1.0f else 0.3f

        // Selection highlight
        if (day.isSelected) {
            holder.tvDay.setTextColor(Color.WHITE)
            holder.tvDay.setBackgroundResource(R.drawable.bg_day_selected)
        } else {
             holder.tvDay.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
             holder.tvDay.background = null
        }

        // Event dot
        if (day.hasEvent) {
             holder.viewDot.visibility = View.VISIBLE
             holder.viewDot.backgroundTintList = ContextCompat.getColorStateList(
                 context, 
                 R.color.primary_200
             )
        } else {
            holder.viewDot.visibility = View.GONE
        }
    }

    override fun getItemCount() = days.size
}
