package com.example.calendario_android_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.calendario_android_app.R
import com.example.calendario_android_app.model.Evento
import com.example.calendario_android_app.model.EstadoEvento
import java.time.format.DateTimeFormatter

/**
 * Adaptador para lista de eventos.
 * Muestra eventos con colores de etiqueta o estado.
 * Soporta modo con/sin columna de tiempo.
 */
class EventAdapter(
    private var eventos: List<Evento>, 
    private val showTimeColumn: Boolean = true,
    private val onEventClick: ((Evento) -> Unit)? = null
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tv_event_time)
        val tvTitle: TextView = view.findViewById(R.id.tv_event_title)
        val tvDetails: TextView = view.findViewById(R.id.tv_event_details)
        val cardEvent: FrameLayout = view.findViewById(R.id.card_event)
        val viewIndicator: View = view.findViewById(R.id.view_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val evento = eventos[position]
        
        // Format time
        val startTime = evento.fechaInicio.format(timeFormatter)
        holder.tvTime.text = startTime
        holder.tvTime.visibility = if (showTimeColumn) View.VISIBLE else View.GONE
        
        // Set title
        holder.tvTitle.text = evento.titulo
        
        // Set details (time range + location)
        val endTime = evento.fechaFin.format(timeFormatter)
        val details = buildString {
            append("$startTime - $endTime")
            if (!evento.ubicacion.isNullOrBlank()) {
                append(" • ${evento.ubicacion}")
            }
        }
        holder.tvDetails.text = details
        
        // Set colors
        if (!evento.colorEtiqueta.isNullOrEmpty()) {
            try {
                val color = android.graphics.Color.parseColor(evento.colorEtiqueta)
                val bgColor = androidx.core.graphics.ColorUtils.setAlphaComponent(color, 50) // ~20% alpha
                
                val shape = android.graphics.drawable.GradientDrawable()
                shape.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                shape.cornerRadius = 8f * holder.itemView.context.resources.displayMetrics.density
                shape.setColor(bgColor)
                
                holder.cardEvent.background = shape
                holder.viewIndicator.setBackgroundColor(color)
                holder.tvTitle.setTextColor(color)
            } catch (e: Exception) {
                // Fallback if parse fails
                applyStatusColor(holder, evento.estado)
            }
        } else {
            applyStatusColor(holder, evento.estado)
        }

        holder.itemView.setOnClickListener {
            onEventClick?.invoke(evento)
        }
    }
    
    private fun applyStatusColor(holder: EventViewHolder, estado: EstadoEvento) {
        when (estado) {
            EstadoEvento.EN_PROGRESO -> {
                setEventColor(holder, R.color.event_green, R.color.event_green_text)
            }
            EstadoEvento.PENDIENTE -> {
                setEventColor(holder, R.color.event_purple, R.color.event_purple_text)
            }
            EstadoEvento.FINALIZADO -> {
                setEventColor(holder, R.color.bg_300, R.color.text_secondary)
            }
        }
    }

    // Helper to set background from Resources
    private fun setEventColor(holder: EventViewHolder, bgColorRes: Int, textColorRes: Int) {
        val context = holder.itemView.context
        
        // Create rounded background programmatically
        val shape = android.graphics.drawable.GradientDrawable()
        shape.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        shape.cornerRadius = 8f * context.resources.displayMetrics.density
        shape.setColor(ContextCompat.getColor(context, bgColorRes))
        
        holder.cardEvent.background = shape
        
        holder.viewIndicator.setBackgroundColor(ContextCompat.getColor(context, textColorRes))
        holder.tvTitle.setTextColor(ContextCompat.getColor(context, textColorRes))
    }

    override fun getItemCount() = eventos.size
    
    fun updateEventos(newEventos: List<Evento>) {
        eventos = newEventos
        notifyDataSetChanged()
    }
}
