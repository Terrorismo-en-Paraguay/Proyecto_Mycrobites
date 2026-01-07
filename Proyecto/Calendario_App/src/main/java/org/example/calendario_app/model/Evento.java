package org.example.calendario_app.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Evento {
    private int id_evento;
    private String titulo;
    private String descripcion;
    private LocalDateTime fecha_inicio;
    private LocalDateTime fecha_fin;
    private String ubicacion;
    private int id_creador;
    private Integer id_etiqueta; // Nullable

    public Evento() {
    }

    public Evento(int id_evento, String titulo, String descripcion, LocalDateTime fecha_inicio, LocalDateTime fecha_fin,
            String ubicacion, int id_creador, Integer id_etiqueta) {
        this.id_evento = id_evento;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha_inicio = fecha_inicio;
        this.fecha_fin = fecha_fin;
        this.ubicacion = ubicacion;
        this.id_creador = id_creador;
        this.id_etiqueta = id_etiqueta;
    }

    // Constructor for simple creation (app uses LocalDate only for now, defaulting
    // times)
    public Evento(String titulo, LocalDate date, String descripcion, int id_creador) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha_inicio = date.atStartOfDay();
        this.fecha_fin = date.atTime(23, 59, 59);
        this.id_creador = id_creador;
    }

    // Constructor for creating new events with all fields
    public Evento(String titulo, String descripcion, LocalDateTime fecha_inicio, LocalDateTime fecha_fin,
            String ubicacion, int id_creador, Integer id_etiqueta) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha_inicio = fecha_inicio;
        this.fecha_fin = fecha_fin;
        this.ubicacion = ubicacion;
        this.id_creador = id_creador;
        this.id_etiqueta = id_etiqueta;
    }

    // Getters and Setters
    public int getId() {
        return id_evento;
    }

    public void setId(int id_evento) {
        this.id_evento = id_evento;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFecha_inicio() {
        return fecha_inicio;
    }

    public void setFecha_inicio(LocalDateTime fecha_inicio) {
        this.fecha_inicio = fecha_inicio;
    }

    public LocalDateTime getFecha_fin() {
        return fecha_fin;
    }

    public void setFecha_fin(LocalDateTime fecha_fin) {
        this.fecha_fin = fecha_fin;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public int getId_creador() {
        return id_creador;
    }

    public void setId_creador(int id_creador) {
        this.id_creador = id_creador;
    }

    public Integer getId_etiqueta() {
        return id_etiqueta;
    }

    public void setId_etiqueta(Integer id_etiqueta) {
        this.id_etiqueta = id_etiqueta;
    }

    // Helper for legacy code that expects LocalDate
    public LocalDate getFecha() {
        return fecha_inicio != null ? fecha_inicio.toLocalDate() : null;
    }
}
