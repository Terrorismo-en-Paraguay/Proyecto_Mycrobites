package com.example.calendario_android_app.dao.impl

import com.example.calendario_android_app.dao.EventoDAO
import com.example.calendario_android_app.model.Evento
import com.example.calendario_android_app.model.EstadoEvento
import com.example.calendario_android_app.util.DatabaseConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime

class EventoDAOImpl : EventoDAO {

    override suspend fun getEventosByUsuarioAndFecha(idUsuario: Int, fecha: LocalDate): List<Evento> = withContext(Dispatchers.IO) {
        val eventos = mutableListOf<Evento>()
        val connection = DatabaseConnection.getConnection()
        
        try {
            connection?.let { conn ->
                val sql = """
                    SELECT e.*, et.color 
                    FROM eventos e
                    INNER JOIN eventos_usuarios eu ON e.id_evento = eu.id_evento
                    LEFT JOIN etiquetas et ON e.id_etiqueta = et.id_etiqueta
                    WHERE eu.id_usuario = ? 
                    AND DATE(e.fecha_inicio) = ?
                    ORDER BY e.fecha_inicio ASC
                """
                
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idUsuario)
                statement.setString(2, fecha.toString())
                
                val resultSet = statement.executeQuery()
                
                while (resultSet.next()) {
                    eventos.add(mapResultSetToEvento(resultSet))
                }
                
                resultSet.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        eventos
    }

    override suspend fun getEventosByUsuario(idUsuario: Int): List<Evento> = withContext(Dispatchers.IO) {
        val eventos = mutableListOf<Evento>()
        val connection = DatabaseConnection.getConnection()
        
        try {
            connection?.let { conn ->
                val sql = """
                    SELECT e.*, et.color 
                    FROM eventos e
                    INNER JOIN eventos_usuarios eu ON e.id_evento = eu.id_evento
                    LEFT JOIN etiquetas et ON e.id_etiqueta = et.id_etiqueta
                    WHERE eu.id_usuario = ?
                    ORDER BY e.fecha_inicio ASC
                """
                
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idUsuario)
                
                val resultSet = statement.executeQuery()
                
                while (resultSet.next()) {
                    eventos.add(mapResultSetToEvento(resultSet))
                }
                
                resultSet.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        eventos
    }

    override suspend fun insertEvento(evento: Evento, idUsuario: Int): Boolean {
        return insertEventoReturnId(evento, idUsuario) != -1
    }

    override suspend fun insertEventoReturnId(evento: Evento, idUsuario: Int): Int = withContext(Dispatchers.IO) {
        val connection = DatabaseConnection.getConnection()
        var generatedId = -1
        
        try {
            connection?.let { conn ->
                conn.autoCommit = false // Transaction start
                
                // Ensure tables
                 val createLinkTable = """
                    CREATE TABLE IF NOT EXISTS eventos_usuarios (
                        id_evento INT NOT NULL,
                        id_usuario INT NOT NULL,
                        PRIMARY KEY (id_evento, id_usuario),
                        FOREIGN KEY (id_evento) REFERENCES eventos(id_evento) ON DELETE CASCADE,
                        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
                    )
                """
                val createStmt = conn.createStatement()
                createStmt.execute(createLinkTable)
                // Add id_etiqueta column if not exists (Primitive migration)
                try {
                     createStmt.execute("ALTER TABLE eventos ADD COLUMN id_etiqueta INT NULL")
                     createStmt.execute("ALTER TABLE eventos ADD CONSTRAINT fk_evento_etiqueta FOREIGN KEY (id_etiqueta) REFERENCES etiquetas(id_etiqueta) ON DELETE SET NULL")
                } catch(e: Exception) {
                    // Ignore column already exists
                }
                createStmt.close()

                val sql = """
                    INSERT INTO eventos (id_creador, id_etiqueta, titulo, descripcion, fecha_inicio, fecha_fin, ubicacion, estado)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """
                
                val statement = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
                statement.setObject(1, evento.idCreador)
                statement.setObject(2, evento.idEtiqueta)
                statement.setString(3, evento.titulo)
                statement.setString(4, evento.descripcion)
                statement.setObject(5, evento.fechaInicio)
                statement.setObject(6, evento.fechaFin)
                statement.setString(7, evento.ubicacion)
                statement.setString(8, evento.estado.valor)
                
                val affectedRows = statement.executeUpdate()
                
                if (affectedRows > 0) {
                    val generatedKeys = statement.generatedKeys
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1)
                        
                        // Check if event has a label associated with a group
                        var grupoUsuarios = mutableListOf<Int>()
                        
                        if (evento.idEtiqueta != null) {
                            // Get the group ID for this label
                            val sqlGrupo = "SELECT id_grupo FROM etiquetas WHERE id_etiqueta = ?"
                            val stmtGrupo = conn.prepareStatement(sqlGrupo)
                            stmtGrupo.setInt(1, evento.idEtiqueta)
                            val rsGrupo = stmtGrupo.executeQuery()
                            
                            var idGrupo: Int? = null
                            if (rsGrupo.next()) {
                                val grupoId = rsGrupo.getInt("id_grupo")
                                if (!rsGrupo.wasNull()) {
                                    idGrupo = grupoId
                                }
                            }
                            rsGrupo.close()
                            stmtGrupo.close()
                            
                            // If label belongs to a group, get all group members
                            if (idGrupo != null) {
                                val sqlMembers = "SELECT id_usuario FROM grupos_usuarios WHERE id_grupo = ?"
                                val stmtMembers = conn.prepareStatement(sqlMembers)
                                stmtMembers.setInt(1, idGrupo)
                                val rsMembers = stmtMembers.executeQuery()
                                
                                while (rsMembers.next()) {
                                    grupoUsuarios.add(rsMembers.getInt("id_usuario"))
                                }
                                rsMembers.close()
                                stmtMembers.close()
                            }
                        }
                        
                        // Insert into eventos_usuarios
                        if (grupoUsuarios.isNotEmpty()) {
                            // Event has a group label - add all group members
                            val sqlLink = "INSERT INTO eventos_usuarios (id_evento, id_usuario) VALUES (?, ?)"
                            val stmtLink = conn.prepareStatement(sqlLink)
                            
                            for (userId in grupoUsuarios) {
                                stmtLink.setInt(1, generatedId)
                                stmtLink.setInt(2, userId)
                                stmtLink.addBatch()
                            }
                            
                            stmtLink.executeBatch()
                            stmtLink.close()
                        } else {
                            // No group label - add only the creator
                            val sqlLink = "INSERT INTO eventos_usuarios (id_evento, id_usuario) VALUES (?, ?)"
                            val stmtLink = conn.prepareStatement(sqlLink)
                            stmtLink.setInt(1, generatedId)
                            stmtLink.setInt(2, idUsuario)
                            stmtLink.executeUpdate()
                            stmtLink.close()
                        }
                        
                        conn.commit()
                    }
                    generatedKeys.close()
                } else {
                    conn.rollback()
                }
                
                statement.close()
                conn.autoCommit = true
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        generatedId
    }

    override suspend fun insertInvitados(idEvento: Int, emails: List<String>): Boolean = withContext(Dispatchers.IO) {
        val connection = DatabaseConnection.getConnection()
        var success = true
        
        try {
            connection?.let { conn ->
                // Ensure table exists
                val createTableSql = """
                    CREATE TABLE IF NOT EXISTS invitaciones (
                        id_invitacion INT AUTO_INCREMENT PRIMARY KEY,
                        id_evento INT NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        FOREIGN KEY (id_evento) REFERENCES eventos(id_evento) ON DELETE CASCADE
                    )
                """
                val createStmt = conn.createStatement()
                createStmt.execute(createTableSql)
                createStmt.close()
                
                // Insert guests
                val insertSql = "INSERT INTO invitaciones (id_evento, email) VALUES (?, ?)"
                val insertStmt = conn.prepareStatement(insertSql)
                
                for (email in emails) {
                    insertStmt.setInt(1, idEvento)
                    insertStmt.setString(2, email)
                    insertStmt.addBatch()
                }
                
                insertStmt.executeBatch()
                insertStmt.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            success = false
        }
        
        success
    }

    override suspend fun getDiasConEventos(idUsuario: Int, year: Int, month: Int): List<Int> = withContext(Dispatchers.IO) {
        val dias = mutableListOf<Int>()
        val connection = DatabaseConnection.getConnection()
        
        try {
            connection?.let { conn ->
                val sql = """
                    SELECT DISTINCT DAY(e.fecha_inicio) as dia
                    FROM eventos e
                    INNER JOIN eventos_usuarios eu ON e.id_evento = eu.id_evento
                    WHERE eu.id_usuario = ?
                    AND YEAR(e.fecha_inicio) = ?
                    AND MONTH(e.fecha_inicio) = ?
                    ORDER BY dia ASC
                """
                
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idUsuario)
                statement.setInt(2, year)
                statement.setInt(3, month)
                
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

    override suspend fun getEventosByUsuarioAndRango(idUsuario: Int, fechaInicio: LocalDate, fechaFin: LocalDate): List<Evento> = withContext(Dispatchers.IO) {
        val eventos = mutableListOf<Evento>()
        val connection = DatabaseConnection.getConnection()
        
        try {
            connection?.let { conn ->
                val sql = """
                    SELECT e.*, et.color 
                    FROM eventos e
                    INNER JOIN eventos_usuarios eu ON e.id_evento = eu.id_evento
                    LEFT JOIN etiquetas et ON e.id_etiqueta = et.id_etiqueta
                    WHERE eu.id_usuario = ? 
                    AND e.fecha_inicio >= ? AND e.fecha_inicio <= ?
                    ORDER BY e.fecha_inicio ASC
                """
                
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idUsuario)
                // Append time to invoke full day range coverage
                statement.setString(2, fechaInicio.atStartOfDay().toString())
                statement.setString(3, fechaFin.atTime(23, 59, 59).toString())
                
                val resultSet = statement.executeQuery()
                
                while (resultSet.next()) {
                    eventos.add(mapResultSetToEvento(resultSet))
                }
                
                resultSet.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        eventos
    }

    private fun mapResultSetToEvento(rs: ResultSet): Evento {
        // Handle potential missing columns if join didn't happen (defensive)
        var color: String? = null
        var idEtiqueta: Int? = null
        try {
            color = rs.getString("color")
            idEtiqueta = rs.getObject("id_etiqueta") as? Int
        } catch (e: Exception) {
            // Column might not exist in some queries if not updated? 
            // But we will update all queries.
        }

        return Evento(
            idEvento = rs.getInt("id_evento"),
            idCreador = rs.getObject("id_creador") as? Int,
            idEtiqueta = idEtiqueta,
            titulo = rs.getString("titulo"),
            descripcion = rs.getString("descripcion"),
            fechaInicio = rs.getTimestamp("fecha_inicio").toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime(),
            fechaFin = rs.getTimestamp("fecha_fin").toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime(),
            ubicacion = rs.getString("ubicacion"),
            estado = EstadoEvento.fromString(rs.getString("estado")),
            creadoEl = rs.getTimestamp("creado_el")?.toInstant()
                ?.atZone(java.time.ZoneId.systemDefault())
                ?.toLocalDateTime(),
            actualizadoEl = rs.getTimestamp("actualizado_el")?.toInstant()
                ?.atZone(java.time.ZoneId.systemDefault())
                ?.toLocalDateTime(),
            colorEtiqueta = color
        )
    }

    override suspend fun searchEventos(idUsuario: Int, query: String): List<Evento> = withContext(Dispatchers.IO) {
        val eventos = mutableListOf<Evento>()
        val connection = DatabaseConnection.getConnection()
        
        try {
            connection?.let { conn ->
                val sql = """
                    SELECT e.*, et.color 
                    FROM eventos e
                    INNER JOIN eventos_usuarios eu ON e.id_evento = eu.id_evento
                    LEFT JOIN etiquetas et ON e.id_etiqueta = et.id_etiqueta
                    WHERE eu.id_usuario = ? 
                    AND e.titulo LIKE ?
                    ORDER BY e.fecha_inicio ASC
                """
                
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idUsuario)
                statement.setString(2, "%$query%")
                
                val resultSet = statement.executeQuery()
                
                while (resultSet.next()) {
                    eventos.add(mapResultSetToEvento(resultSet))
                }
                
                resultSet.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        eventos
    }

    override suspend fun getEventoById(idEvento: Int): Evento? = withContext(Dispatchers.IO) {
        var evento: Evento? = null
        val connection = DatabaseConnection.getConnection()
        try {
            connection?.let { conn ->
                val sql = """
                    SELECT e.*, et.color 
                    FROM eventos e
                    LEFT JOIN etiquetas et ON e.id_etiqueta = et.id_etiqueta
                    WHERE e.id_evento = ?
                """
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idEvento)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    evento = mapResultSetToEvento(resultSet)
                }
                resultSet.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        evento
    }

    override suspend fun deleteEvento(idEvento: Int): Boolean = withContext(Dispatchers.IO) {
        var success = false
        val connection = DatabaseConnection.getConnection()
        try {
            connection?.let { conn ->
                val sql = "DELETE FROM eventos WHERE id_evento = ?"
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idEvento)
                val affectedRows = statement.executeUpdate()
                success = affectedRows > 0
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        success
    }
}
