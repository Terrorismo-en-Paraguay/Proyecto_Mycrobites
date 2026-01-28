package com.example.calendario_android_app.dao.impl

import com.example.calendario_android_app.dao.EtiquetaDAO
import com.example.calendario_android_app.model.Etiqueta
import com.example.calendario_android_app.util.DatabaseConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Statement

class EtiquetaDAOImpl : EtiquetaDAO {

    override suspend fun insertEtiqueta(etiqueta: Etiqueta, idUsuario: Int, idGrupo: Int?): Boolean = withContext(Dispatchers.IO) {
        val connection = DatabaseConnection.getConnection()
        var success = false
        
        try {
            connection?.let { conn ->
                conn.autoCommit = false // Start transaction

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
                
                // Schema Migration for existing tables: Add id_grupo if missing
                try {
                    val alterTable = "ALTER TABLE etiquetas ADD COLUMN IF NOT EXISTS id_grupo INT"
                    createStmt.execute(alterTable)
                    
                    // Add FK if possible. Checking for constraint existence is hard in standard SQL without querying info schema.
                    // We can try adding it and ignore failure, or better:
                    // If we successfully added the column (or it existed), we want the FK.
                    // However, re-adding FK might duplicate it or fail.
                    // Simple approach: Try adding FK with a specific name, and catch error.
                    // Note: 'IF NOT EXISTS' for constraints is available in newer MariaDB.
                    // Lets try safe approach: just the column first to fix the crash. 
                    // The FK is good for integrity but the app logic (DAO) handles the nulls. 
                    // I will add the FK attempt.
                    val alterFk = "ALTER TABLE etiquetas ADD CONSTRAINT fk_etiquetas_grupos FOREIGN KEY IF NOT EXISTS (id_grupo) REFERENCES grupos(id_grupo) ON DELETE SET NULL"
                    createStmt.execute(alterFk)
                } catch (e: Exception) {
                    // Ignore schema modification errors if column/constraint already exists or other non-critical issues
                    // e.printStackTrace() 
                }
                
                createStmt.close()
                
                // Insert Etiqueta
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
                        
                        // Insert Link
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

    override suspend fun getEtiquetasByUsuario(idUsuario: Int): List<Etiqueta> = withContext(Dispatchers.IO) {
        val etiquetas = mutableListOf<Etiqueta>()
        val connection = DatabaseConnection.getConnection()
        
        try {
            connection?.let { conn ->
                 // Join query via Groups
                val sql = """
                    SELECT e.id_etiqueta, e.nombre, e.color, e.id_grupo 
                    FROM etiquetas e
                    INNER JOIN grupos_usuarios gu ON e.id_grupo = gu.id_grupo
                    WHERE gu.id_usuario = ?
                """
                val statement = conn.prepareStatement(sql)
                statement.setInt(1, idUsuario)
                
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
