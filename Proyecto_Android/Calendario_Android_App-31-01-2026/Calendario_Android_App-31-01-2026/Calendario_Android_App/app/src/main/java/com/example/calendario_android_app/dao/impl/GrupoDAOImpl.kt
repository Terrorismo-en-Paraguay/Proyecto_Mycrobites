package com.example.calendario_android_app.dao.impl

import com.example.calendario_android_app.dao.GrupoDAO
import com.example.calendario_android_app.model.Grupo
import com.example.calendario_android_app.util.DatabaseConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime

/**
 * Implementación de la interfaz GrupoDAO para gestionar calendarios compartidos y grupos.
 * Se encarga de la creación de grupos, la gestión de miembros y la relación entre usuarios y calendarios.
 */
class GrupoDAOImpl : GrupoDAO {

    /**
     * Crea un nuevo registro de grupo en la base de datos.
     * @return El ID generado para el nuevo grupo o -1 si falla.
     */
    override suspend fun createGrupo(nombre: String, descripcion: String?): Int = withContext(Dispatchers.IO) {
        val connection = DatabaseConnection.getConnection()
        var generatedId = -1
        
        try {
            connection?.let { conn ->
                val sql = "INSERT INTO grupos (nombre, descripcion, creado_el, actualizado_el) VALUES (?, ?, ?, ?)"
                val statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                statement.setString(1, nombre)
                statement.setString(2, descripcion)
                val now = Timestamp(System.currentTimeMillis())
                statement.setTimestamp(3, now)
                statement.setTimestamp(4, now)
                
                val affectedRows = statement.executeUpdate()
                if (affectedRows > 0) {
                     val keys = statement.generatedKeys
                     if (keys.next()) {
                         generatedId = keys.getInt(1)
                     }
                     keys.close()
                }
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        generatedId
    }

    /**
     * Vincula a un usuario con un grupo asignándole un rol específico (admin o miembro).
     */
    override suspend fun linkUsuarioToGrupo(idGrupo: Int, idUsuario: Int, rol: String) {
        withContext(Dispatchers.IO) {
             val connection = DatabaseConnection.getConnection()
             try {
                 connection?.let { conn ->
                     val sql = "INSERT INTO grupos_usuarios (id_grupo, id_usuario, rol, fecha_union) VALUES (?, ?, ?, ?)"
                     val statement = conn.prepareStatement(sql)
                     statement.setInt(1, idGrupo)
                     statement.setInt(2, idUsuario)
                     statement.setString(3, rol)
                     statement.setTimestamp(4, Timestamp(System.currentTimeMillis()))
                     
                     statement.executeUpdate()
                     statement.close()
                     conn.close()
                 }
             } catch(e: Exception) {
                 e.printStackTrace()
             }
        }
    }

    /**
     * Recupera todos los grupos (calendarios) a los que pertenece un usuario.
     */
    override suspend fun getGruposByUsuario(idUsuario: Int): List<Grupo> = withContext(Dispatchers.IO) {
        val grupos = mutableListOf<Grupo>()
        val connection = DatabaseConnection.getConnection()
        
        try {
            connection?.let { conn ->
                val sql = """
                    SELECT g.* FROM grupos g
                    INNER JOIN grupos_usuarios gu ON g.id_grupo = gu.id_grupo
                    WHERE gu.id_usuario = ?
                """
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idUsuario)
                
                val rs = statement.executeQuery()
                while (rs.next()) {
                    grupos.add(Grupo(
                        idGrupo = rs.getInt("id_grupo"),
                        nombre = rs.getString("nombre"),
                        descripcion = rs.getString("descripcion"),
                        creadoEl = rs.getTimestamp("creado_el")?.toInstant()
                            ?.atZone(java.time.ZoneId.systemDefault())
                            ?.toLocalDateTime(),
                        actualizadoEl = rs.getTimestamp("actualizado_el")?.toInstant()
                            ?.atZone(java.time.ZoneId.systemDefault())
                            ?.toLocalDateTime()
                    ))
                }
                rs.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        grupos
    }

    /**
     * Crea un grupo "Personal" por defecto para nuevos usuarios si no existe.
     * Este grupo se utiliza como el calendario principal del usuario.
     */
    override suspend fun createDefaultPersonalGroup(idUsuario: Int): Boolean = withContext(Dispatchers.IO) {
        val connection = DatabaseConnection.getConnection()
        var success = false
        try {
            connection?.let { conn ->
                conn.autoCommit = false
                
                // Aseguramos que las tablas necesarias existan (por si es la primera vez).
                val createGrupos = """
                    CREATE TABLE IF NOT EXISTS grupos (
                        id_grupo INT AUTO_INCREMENT PRIMARY KEY,
                        nombre VARCHAR(100) NOT NULL,
                        descripcion TEXT,
                        creado_el DATETIME,
                        actualizado_el DATETIME
                    )
                """
                val createLink = """
                    CREATE TABLE IF NOT EXISTS grupos_usuarios (
                        id_grupo INT NOT NULL,
                        id_usuario INT NOT NULL,
                        rol VARCHAR(20) DEFAULT 'admin',
                        fecha_union DATETIME,
                        PRIMARY KEY (id_grupo, id_usuario),
                        FOREIGN KEY (id_grupo) REFERENCES grupos(id_grupo) ON DELETE CASCADE,
                        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
                    )
                """
                
                val stmtStruct = conn.createStatement()
                stmtStruct.execute(createGrupos)
                stmtStruct.execute(createLink)
                stmtStruct.close()
                
                // Comprobamos si el usuario ya tiene su grupo personal para no duplicar.
                val checkSql = "SELECT 1 FROM grupos g INNER JOIN grupos_usuarios gu ON g.id_grupo = gu.id_grupo WHERE gu.id_usuario = ? AND g.nombre = 'Personal' LIMIT 1"
                val checkStmt = conn.prepareStatement(checkSql)
                checkStmt.setInt(1, idUsuario)
                val checkRs = checkStmt.executeQuery()
                val exists = checkRs.next()
                checkRs.close()
                checkStmt.close()

                if (exists) {
                     conn.close()
                     return@withContext true
                }

                // 1. Insertamos el grupo
                val sqlGroup = "INSERT INTO grupos (nombre, creado_el, actualizado_el) VALUES (?, ?, ?)"
                val stmtGroup = conn.prepareStatement(sqlGroup, Statement.RETURN_GENERATED_KEYS)
                stmtGroup.setString(1, "Personal")
                val now = Timestamp(System.currentTimeMillis())
                stmtGroup.setTimestamp(2, now)
                stmtGroup.setTimestamp(3, now)
                
                stmtGroup.executeUpdate()
                val keys = stmtGroup.generatedKeys
                var groupId = -1
                if (keys.next()) {
                    groupId = keys.getInt(1)
                }
                keys.close()
                stmtGroup.close()
                
                if (groupId != -1) {
                    // 2. Vinculamos al usuario como administrador
                    val sqlLinkUser = "INSERT INTO grupos_usuarios (id_grupo, id_usuario, rol, fecha_union) VALUES (?, ?, ?, ?)"
                    val stmtLinkUser = conn.prepareStatement(sqlLinkUser)
                    stmtLinkUser.setInt(1, groupId)
                    stmtLinkUser.setInt(2, idUsuario)
                    stmtLinkUser.setString(3, "admin")
                    stmtLinkUser.setTimestamp(4, now)
                    stmtLinkUser.executeUpdate()
                    stmtLinkUser.close()
                    
                    conn.commit()
                    success = true
                } else {
                    conn.rollback()
                }
                
                conn.autoCommit = true
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        success
    }

    /**
     * Obtiene la lista completa de integrantes de un grupo concreto.
     * Recupera el Correo, ID, Nombre del cliente y Rol de cada miembro.
     */
    override suspend fun getIntegrantesGrupo(idGrupo: Int): List<com.example.calendario_android_app.model.GroupMember> = withContext(Dispatchers.IO) {
        val integrantes = mutableListOf<com.example.calendario_android_app.model.GroupMember>()
        val connection = DatabaseConnection.getConnection()
        try {
            connection?.let { conn ->
                val sql = """
                    SELECT u.id_usuario, u.correo, gu.rol, c.nombre as cliente_nombre
                    FROM usuarios u
                    INNER JOIN grupos_usuarios gu ON u.id_usuario = gu.id_usuario
                    LEFT JOIN clientes c ON u.id_cliente = c.id_cliente
                    WHERE gu.id_grupo = ?
                """
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idGrupo)
                val rs = statement.executeQuery()
                while (rs.next()) {
                    integrantes.add(com.example.calendario_android_app.model.GroupMember(
                        email = rs.getString("correo"),
                        userId = rs.getInt("id_usuario"),
                        userName = rs.getString("cliente_nombre"),
                        role = rs.getString("rol")
                    ))
                }
                rs.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        integrantes
    }

    /**
     * Verifica si un usuario tiene permisos de administrador en un grupo determinado.
     */
    override suspend fun isAdmin(idUsuario: Int, idGrupo: Int): Boolean = withContext(Dispatchers.IO) {
        var isAdmin = false
        val connection = DatabaseConnection.getConnection()
        try {
            connection?.let { conn ->
                val sql = "SELECT 1 FROM grupos_usuarios WHERE id_grupo = ? AND id_usuario = ? AND rol = 'admin'"
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idGrupo)
                statement.setInt(2, idUsuario)
                val rs = statement.executeQuery()
                isAdmin = rs.next()
                rs.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isAdmin
    }

    /**
     * Obtiene los detalles de un grupo por su ID único.
     */
    override suspend fun getGrupoById(idGrupo: Int): Grupo? = withContext(Dispatchers.IO) {
        var grupo: Grupo? = null
        val connection = DatabaseConnection.getConnection()
        try {
            connection?.let { conn ->
                val sql = "SELECT * FROM grupos WHERE id_grupo = ?"
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idGrupo)
                val rs = statement.executeQuery()
                if (rs.next()) {
                    grupo = Grupo(
                        idGrupo = rs.getInt("id_grupo"),
                        nombre = rs.getString("nombre"),
                        descripcion = rs.getString("descripcion"),
                        creadoEl = rs.getTimestamp("creado_el")?.toInstant()
                            ?.atZone(java.time.ZoneId.systemDefault())?.toLocalDateTime(),
                        actualizadoEl = rs.getTimestamp("actualizado_el")?.toInstant()
                            ?.atZone(java.time.ZoneId.systemDefault())?.toLocalDateTime()
                    )
                }
                rs.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        grupo
    }
}
