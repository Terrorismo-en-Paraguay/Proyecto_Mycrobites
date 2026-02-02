package com.example.calendario_android_app.model

/**
 * Modelo de datos que representa un día festivo o feriado.
 * Los festivos se muestran en el calendario para informar al usuario.
 * 
 * @property idFestivo Identificador único del festivo
 * @property nombre Nombre del festivo (ej: "Día de Reyes")
 * @property mes Mes del festivo (1-12)
 * @property dia Día del mes del festivo (1-31)
 * @property tipo Tipo de festivo (nacional, religioso, regional)
 * @property descripcion Descripción adicional del festivo (opcional)
 * @property esFijo Indica si la fecha es fija (true) o móvil como Semana Santa (false)
 */
data class Festivo(
    val idFestivo: Int = 0,
    val nombre: String,
    val mes: Int,
    val dia: Int,
    val tipo: TipoFestivo,
    val descripcion: String?,
    val esFijo: Boolean = true
)

/**
 * Enumeración que define los tipos de festivos disponibles.
 */
enum class TipoFestivo(val valor: String) {
    /** Festivo de ámbito nacional */
    NACIONAL("nacional"),
    
    /** Festivo de carácter religioso */
    RELIGIOSO("religioso"),
    
    /** Festivo de ámbito regional o autonómico */
    REGIONAL("regional");
    
    companion object {
        /**
         * Convierte un string del valor de la base de datos al enum correspondiente.
         * @param valor String con el tipo de festivo
         * @return Tipo de festivo, por defecto NACIONAL si no se encuentra
         */
        fun fromString(valor: String): TipoFestivo {
            return values().find { it.valor == valor } ?: NACIONAL
        }
    }
}
