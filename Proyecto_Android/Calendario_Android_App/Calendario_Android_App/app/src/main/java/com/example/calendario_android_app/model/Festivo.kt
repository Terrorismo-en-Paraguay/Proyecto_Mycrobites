package com.example.calendario_android_app.model

data class Festivo(
    val idFestivo: Int = 0,
    val nombre: String,
    val mes: Int,
    val dia: Int,
    val tipo: TipoFestivo,
    val descripcion: String?,
    val esFijo: Boolean = true
)

enum class TipoFestivo(val valor: String) {
    /** Festivo de ámbito nacional */
    NACIONAL("nacional"),
    
    /** Festivo de carácter religioso */
    RELIGIOSO("religioso"),
    
    /** Festivo de ámbito regional o autonómico */
    REGIONAL("regional");
    
    companion object {
        fun fromString(valor: String): TipoFestivo {
            return values().find { it.valor == valor } ?: NACIONAL
        }
    }
}
