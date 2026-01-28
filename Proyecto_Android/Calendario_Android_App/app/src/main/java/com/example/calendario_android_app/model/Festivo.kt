package com.example.calendario_android_app.model

data class Festivo(
    val idFestivo: Int = 0,
    val nombre: String,
    val mes: Int,  // 1-12
    val dia: Int,  // 1-31
    val tipo: TipoFestivo,
    val descripcion: String?,
    val esFijo: Boolean = true  // true = fecha fija, false = móvil (Semana Santa)
)

enum class TipoFestivo(val valor: String) {
    NACIONAL("nacional"),
    RELIGIOSO("religioso"),
    REGIONAL("regional");
    
    companion object {
        fun fromString(valor: String): TipoFestivo {
            return values().find { it.valor == valor } ?: NACIONAL
        }
    }
}
