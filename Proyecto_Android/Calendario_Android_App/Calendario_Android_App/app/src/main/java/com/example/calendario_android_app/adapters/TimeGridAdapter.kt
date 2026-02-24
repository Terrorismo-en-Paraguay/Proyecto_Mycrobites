package com.example.calendario_android_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.calendario_android_app.R

import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import com.example.calendario_android_app.model.Evento
import java.time.format.DateTimeFormatter
import java.time.LocalDate

class TimeGridAdapter(
    private val hours: List<String>,
    private val isWeekView: Boolean = true,
    private var events: List<Evento> = emptyList(),
    private var weekStart: LocalDate = LocalDate.now(), // Referencia para calcular columna
    private val onEventClick: ((Evento) -> Unit)? = null
) : RecyclerView.Adapter<TimeGridAdapter.TimeRowViewHolder>() {

    fun updateEvents(newEvents: List<Evento>, newWeekStart: LocalDate) {
        this.events = newEvents
        this.weekStart = newWeekStart
        notifyDataSetChanged()
    }

    class TimeRowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val container: LinearLayout = view.findViewById(R.id.layout_events_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeRowViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_row, parent, false)
        return TimeRowViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeRowViewHolder, position: Int) {
        val timeLabel = hours[position]
        holder.tvTime.text = timeLabel
        
        holder.container.removeAllViews()
        
        val columns = if (isWeekView) 7 else 1
        holder.container.weightSum = columns.toFloat()
        
        for (i in 0 until columns) {
            val cell = android.widget.FrameLayout(holder.itemView.context)
            val params = LinearLayout.LayoutParams(
                0, 
                LinearLayout.LayoutParams.MATCH_PARENT, 
                1.0f
            )
            if (i > 0) {
                 params.setMargins(1, 0, 0, 0) 
            }
            cell.layoutParams = params
            
            
            holder.container.addView(cell)
            
            val cellDate = if (isWeekView) weekStart.plusDays(i.toLong()) else weekStart
            val hour = position
            
            val eventsInCell = events.filter { event ->
                val eventStart = event.fechaInicio
                val sameDay = eventStart.toLocalDate().isEqual(cellDate)
                val sameHour = eventStart.hour == hour
                sameDay && sameHour
            }
            
            eventsInCell.forEach { event ->
                val textView = TextView(holder.itemView.context)
                val params = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
                params.setMargins(2, 2, 2, 2)
                textView.layoutParams = params
                
                val shape = android.graphics.drawable.GradientDrawable()
                shape.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                shape.cornerRadius = 8f * holder.itemView.context.resources.displayMetrics.density
                
                val eventColor = try {
                    if (event.colorEtiqueta != null) {
                        Color.parseColor(event.colorEtiqueta)
                    } else {
                        Color.parseColor("#9C27B0")
                    }
                } catch (e: Exception) {
                    Color.parseColor("#9C27B0")
                }
                shape.setColor(eventColor)
                
                textView.background = shape
                
                textView.text = event.titulo
                textView.textSize = 10f
                textView.setTextColor(Color.WHITE)
                textView.setPadding(8, 4, 8, 4)
                textView.maxLines = 1
                textView.ellipsize = android.text.TextUtils.TruncateAt.END
                
                textView.setOnClickListener {
                    onEventClick?.invoke(event)
                }

                cell.addView(textView)
            }
        }
    }


    override fun getItemCount() = hours.size
}
