package com.example.calendario_android_app.dao.impl

import com.example.calendario_android_app.dao.FestivoDAO
import com.example.calendario_android_app.model.Festivo
import com.example.calendario_android_app.model.TipoFestivo
import com.example.calendario_android_app.util.DatabaseConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet

/**
 * Implementación de FestivoDAO que gestiona operaciones de base de datos para festivos.
 * Todas las operaciones son asíncronas usando coroutines para evitar bloquear el hilo principal.
 */
class FestivoDAOImpl : FestivoDAO {

    /**
     * Busca un festivo específico por mes y día.
     * Ejecuta la consulta en un hilo de I/O para no bloquear la UI.
     */
    override suspend fun getFestivoByMesYDia(mes: Int, dia: Int): Festivo? = withContext(Dispatchers.IO) {
        var festivo: Festivo? = null
        val connection = DatabaseConnection.getConnection()
        
        try {
            connection?.let { conn ->
                val sql = "SELECT * FROM festivos WHERE mes = ? AND dia = ? LIMIT 1"
                
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, mes)
                statement.setInt(2, dia)
                
                val resultSet = statement.executeQuery()
                
                if (resultSet.next()) {
                    festivo = mapResultSetToFestivo(resultSet)
                }
                
                resultSet.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        festivo
    }

    /**
     * Obtiene todos los festivos de un mes específico.
     * Los resultados se ordenan por día ascendente.
     */
    override suspend fun getFestivosByMes(month: Int): List<Festivo> = withContext(Dispatchers.IO) {
        val festivos = mutableListOf<Festivo>()
        val connection = DatabaseConnection.getConnection()
        
        try {
            connection?.let { conn ->
                val sql = """
                    SELECT * FROM festivos 
                    WHERE mes = ?
                    ORDER BY dia ASC
                """
                
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, month)
                
                val resultSet = statement.executeQuery()
                
                while (resultSet.next()) {
                    festivos.add(mapResultSetToFestivo(resultSet))
                }
                
                resultSet.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        festivos
    }

    /**
     * Obtiene solo los números de día que son festivos en un mes.
     * Útil para marcar visualmente los días festivos en el calendario.
     */
    override suspend fun getDiasFestivos(month: Int): List<Int> = withContext(Dispatchers.IO) {
        val dias = mutableListOf<Int>()
        val connection = DatabaseConnection.getConnection()
        
        try {
            connection?.let { conn ->
                val sql = """
                    SELECT DISTINCT dia
                    FROM festivos
                    WHERE mes = ?
                    ORDER BY dia ASC
                """
                
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, month)
                
                val resultSet = statement.executeQuery()
                
                while (resultSet.next()) {
                    dias.add(resultSet.getInt("dia"))
                }
                
                resultSet.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        dias
    }

    /**
     * Mapea un ResultSet de la base de datos a un objeto Festivo.
     * Convierte el tipo de string a enum usando TipoFestivo.fromString.
     */
    private fun mapResultSetToFestivo(rs: ResultSet): Festivo {
        return Festivo(
            idFestivo = rs.getInt("id_festivo"),
            nombre = rs.getString("nombre"),
            mes = rs.getInt("mes"),
            dia = rs.getInt("dia"),
            tipo = TipoFestivo.fromString(rs.getString("tipo")),
            descripcion = rs.getString("descripcion"),
            esFijo = rs.getBoolean("es_fijo")
        )
    }
}
