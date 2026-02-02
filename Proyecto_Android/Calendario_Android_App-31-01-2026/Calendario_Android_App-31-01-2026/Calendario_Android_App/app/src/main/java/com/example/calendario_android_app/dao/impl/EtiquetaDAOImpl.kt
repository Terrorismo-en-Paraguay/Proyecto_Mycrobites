package com.example.calendario_android_app.dao.impl

import com.example.calendario_android_app.dao.EtiquetaDAO
import com.example.calendario_android_app.model.Etiqueta
import com.example.calendario_android_app.util.DatabaseConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Statement

/**
 * Implementación de la interfaz EtiquetaDAO para gestionar las etiquetas (categorías) del calendario.
 * Permite crear etiquetas personales o etiquetas vinculadas a un grupo específico.
 */
class EtiquetaDAOImpl : EtiquetaDAO {

    /**
     * Inserta una nueva etiqueta en la base de datos y la vincula al usuario.
     * @param etiqueta Objeto etiqueta con nombre y color.
     * @param idUsuario ID del usuario que crea/posee la etiqueta.
     * @param idGrupo ID del grupo si la etiqueta es para un calendario compartido (opcional).
     */
    override suspend fun insertEtiqueta(etiqueta: Etiqueta, idUsuario: Int, idGrupo: Int?): Boolean = withContext(Dispatchers.IO) {
        val connection = DatabaseConnection.getConnection()
        var success = false
        
        try {
            connection?.let { conn ->
                conn.autoCommit = false // Iniciamos transacción para asegurar consistencia

                // Create Tables if not exist
                // Ensure dependent tables exist for Foreign Keys
                val createUsuariosTable = """
                    CREATE TABLE IF NOT EXISTS usuarios (
                        id_usuario INT AUTO_INCREMENT PRIMARY KEY,
                        nombre VARCHAR(50) NOT NULL,
                        apellido VARCHAR(50) NOT NULL,
                        email VARCHAR(100) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        creado_el DATETIME
                    )
                """
                
                val createGruposTable = """
                    CREATE TABLE IF NOT EXISTS grupos (
                        id_grupo INT AUTO_INCREMENT PRIMARY KEY,
                        nombre VARCHAR(100) NOT NULL,
                        descripcion TEXT,
                        creado_el DATETIME,
                        actualizado_el DATETIME
                    )
                """
                
                val createEtiquetasTable = """
                    CREATE TABLE IF NOT EXISTS etiquetas (
                        id_etiqueta INT AUTO_INCREMENT PRIMARY KEY,
                        nombre VARCHAR(100) NOT NULL,
                        color VARCHAR(20) NOT NULL,
                        id_grupo INT,
                        FOREIGN KEY (id_grupo) REFERENCES grupos(id_grupo) ON DELETE SET NULL
                    )
                """
                val createLinkTable = """
                    CREATE TABLE IF NOT EXISTS etiquetas_usuarios (
                        id_etiqueta INT NOT NULL,
                        id_usuario INT NOT NULL,
                        PRIMARY KEY (id_etiqueta, id_usuario),
                        FOREIGN KEY (id_etiqueta) REFERENCES etiquetas(id_etiqueta) ON DELETE CASCADE,
                        FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
                    )
                """
                
                val createStmt = conn.createStatement()
                createStmt.execute(createUsuariosTable)
                createStmt.execute(createGruposTable)
                createStmt.execute(createEtiquetasTable)
                createStmt.execute(createLinkTable)
                
                // Mantenimiento del esquema: nos aseguramos de que la columna id_grupo exista.
                try {
                    val alterTable = "ALTER TABLE etiquetas ADD COLUMN IF NOT EXISTS id_grupo INT"
                    createStmt.execute(alterTable)
                    
                    val alterFk = "ALTER TABLE etiquetas ADD CONSTRAINT fk_etiquetas_grupos FOREIGN KEY IF NOT EXISTS (id_grupo) REFERENCES grupos(id_grupo) ON DELETE SET NULL"
                    createStmt.execute(alterFk)
                } catch (e: Exception) {
                    // Ignoramos errores de alteración si ya existen los elementos.
                }
                
                createStmt.close()
                
                // 1. Insertamos la etiqueta
                val sqlEtiqueta = "INSERT INTO etiquetas (nombre, color, id_grupo) VALUES (?, ?, ?)"
                val stmtEtiqueta = conn.prepareStatement(sqlEtiqueta, Statement.RETURN_GENERATED_KEYS)
                stmtEtiqueta.setString(1, etiqueta.nombre)
                stmtEtiqueta.setString(2, etiqueta.color)
                if (idGrupo != null) {
                    stmtEtiqueta.setInt(3, idGrupo)
                } else {
                    stmtEtiqueta.setNull(3, java.sql.Types.INTEGER)
                }
                
                val affectedRows = stmtEtiqueta.executeUpdate()
                
                if (affectedRows > 0) {
                    val generatedKeys = stmtEtiqueta.generatedKeys
                    if (generatedKeys.next()) {
                        val idEtiqueta = generatedKeys.getInt(1)
                        
                        // 2. Vinculamos la etiqueta al usuario creador
                        val sqlLink = "INSERT INTO etiquetas_usuarios (id_etiqueta, id_usuario) VALUES (?, ?)"
                        val stmtLink = conn.prepareStatement(sqlLink)
                        stmtLink.setInt(1, idEtiqueta)
                        stmtLink.setInt(2, idUsuario)
                        stmtLink.executeUpdate()
                        stmtLink.close()
                        
                        success = true
                    }
                    generatedKeys.close()
                }
                stmtEtiqueta.close()
                
                if (success) {
                    conn.commit()
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
     * Recupera todas las etiquetas visibles para un usuario.
     * Incluye etiquetas personales Y etiquetas de los grupos a los que pertenece.
     */
    override suspend fun getEtiquetasByUsuario(idUsuario: Int): List<Etiqueta> = withContext(Dispatchers.IO) {
        val etiquetas = mutableListOf<Etiqueta>()
        val connection = DatabaseConnection.getConnection()
        
        try {
            connection?.let { conn ->
                 // Consulta etiquetas vinculadas directamente al usuario O mediante sus grupos.
                val sql = """
                    SELECT DISTINCT e.id_etiqueta, e.nombre, e.color, e.id_grupo 
                    FROM etiquetas e
                    LEFT JOIN etiquetas_usuarios eu ON e.id_etiqueta = eu.id_etiqueta
                    LEFT JOIN grupos_usuarios gu ON e.id_grupo = gu.id_grupo
                    WHERE eu.id_usuario = ? OR gu.id_usuario = ?
                """
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idUsuario)
                statement.setInt(2, idUsuario)
                
                val resultSet = statement.executeQuery()
                
                while (resultSet.next()) {
                    val groupId = resultSet.getInt("id_grupo")
                    etiquetas.add(
                        Etiqueta(
                            idEtiqueta = resultSet.getInt("id_etiqueta"),
                            nombre = resultSet.getString("nombre"),
                            color = resultSet.getString("color"),
                            idGrupo = if (resultSet.wasNull()) null else groupId
                        )
                    )
                }
                
                resultSet.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        etiquetas
    }

    /**
     * Actualiza el grupo asociado a una etiqueta.
     */
    override suspend fun updateEtiquetaGrupo(idEtiqueta: Int, idGrupo: Int): Boolean = withContext(Dispatchers.IO) {
        val connection = DatabaseConnection.getConnection()
        var success = false
        try {
            connection?.let { conn ->
                val sql = "UPDATE etiquetas SET id_grupo = ? WHERE id_etiqueta = ?"
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idGrupo)
                statement.setInt(2, idEtiqueta)
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

    /**
     * Obtiene el ID del grupo asociado a una etiqueta específica.
     * @return ID del grupo o null si es una etiqueta personal.
     */
    override suspend fun getGrupoIdByEtiqueta(idEtiqueta: Int): Int? = withContext(Dispatchers.IO) {
        var idGrupo: Int? = null
        val connection = DatabaseConnection.getConnection()
        try {
            connection?.let { conn ->
                val sql = "SELECT id_grupo FROM etiquetas WHERE id_etiqueta = ?"
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idEtiqueta)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    val id = resultSet.getInt("id_grupo")
                    if (!resultSet.wasNull()) {
                        idGrupo = id
                    }
                }
                resultSet.close()
                statement.close()
                conn.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        idGrupo
    }
}
