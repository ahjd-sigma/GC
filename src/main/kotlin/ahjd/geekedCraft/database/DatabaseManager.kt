package ahjd.geekedCraft.database

import ahjd.geekedCraft.util.MSG
import java.sql.Connection
import java.sql.DriverManager

object DatabaseManager {
    private var connection: Connection? = null
    private const val AUTO_REMOVE_EXTRA_COLUMNS = true

    fun start() {
        try {
            Class.forName("org.mariadb.jdbc.Driver")
            MSG.info("[GeekedCraft] MariaDB driver found.")
        } catch (e: ClassNotFoundException) {
            MSG.info("[GeekedCraft] MariaDB driver not found on classpath – cannot connect.")
            return
        }

        try {
            connection = DriverManager.getConnection(
                "jdbc:mariadb://localhost:3306/minecraft?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                "root",
                "6741"
            )
            MSG.info("[GeekedCraft] MariaDB connection established.")

            ensurePlayersTable()
            ensureHologramsTable()
        } catch (e: Exception) {
            MSG.info("[GeekedCraft] Failed to connect to MariaDB:")
            e.printStackTrace()
            connection = null
        }
    }

    private fun ensurePlayersTable() {
        val createSQL = """
            CREATE TABLE IF NOT EXISTS players (
                uuid VARCHAR(36) PRIMARY KEY,
                name VARCHAR(32) NOT NULL,
                playtime BIGINT DEFAULT 0,
                jumps INT DEFAULT 0,
                deaths INT DEFAULT 0,
                logins INT DEFAULT 1,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """.trimIndent()

        connection?.createStatement()?.use { stmt ->
            stmt.execute(createSQL)
            MSG.info("[GeekedCraft] Players table ensured.")
        }

        val columnDefinitions = mapOf(
            "uuid" to "VARCHAR(36) PRIMARY KEY",
            "name" to "VARCHAR(32) NOT NULL",
            "playtime" to "BIGINT DEFAULT 0",
            "jumps" to "INT DEFAULT 0",
            "deaths" to "INT DEFAULT 0",
            "logins" to "INT DEFAULT 1",
            "created_at" to "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
            "updated_at" to "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
        )
        syncTableColumns("players", columnDefinitions)
    }


    private fun ensureHologramsTable() {
        val createSQL = """
        CREATE TABLE IF NOT EXISTS holograms (
            uuid VARCHAR(36) PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            text TEXT NOT NULL,
            world VARCHAR(255),
            x DOUBLE DEFAULT 0.0,
            y DOUBLE DEFAULT 0.0,
            z DOUBLE DEFAULT 0.0,
            alignment VARCHAR(50) DEFAULT 'CENTER',
            shadowed BOOLEAN DEFAULT FALSE,
            see_through BOOLEAN DEFAULT FALSE,
            bg_color INT,
            billboard VARCHAR(50) DEFAULT 'FIXED',
            text_opacity TINYINT DEFAULT -1,
            line_width INT DEFAULT 200,
            brightness_block INT,
            brightness_sky INT,
            view_range FLOAT DEFAULT 1.0,
            shadow_radius FLOAT DEFAULT 0.0,
            shadow_strength FLOAT DEFAULT 1.0,
            scale_x FLOAT DEFAULT 1.0,
            scale_y FLOAT DEFAULT 1.0,
            scale_z FLOAT DEFAULT 1.0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )
    """.trimIndent()

        connection?.createStatement()?.use { stmt ->
            stmt.execute(createSQL)
            MSG.info("[GeekedCraft] Holograms table ensured.")
        }

        val columnDefinitions = mapOf(
            "uuid" to "VARCHAR(36) PRIMARY KEY",
            "name" to "VARCHAR(255) NOT NULL",
            "text" to "TEXT NOT NULL",
            "world" to "VARCHAR(255)",
            "x" to "DOUBLE DEFAULT 0.0",
            "y" to "DOUBLE DEFAULT 0.0",
            "z" to "DOUBLE DEFAULT 0.0",
            "alignment" to "VARCHAR(50) DEFAULT 'CENTER'",
            "shadowed" to "BOOLEAN DEFAULT FALSE",
            "see_through" to "BOOLEAN DEFAULT FALSE",
            "bg_color" to "INT",
            "billboard" to "VARCHAR(50) DEFAULT 'FIXED'",
            "text_opacity" to "TINYINT DEFAULT -1",
            "line_width" to "INT DEFAULT 200",
            "brightness_block" to "INT",
            "brightness_sky" to "INT",
            "view_range" to "FLOAT DEFAULT 1.0",
            "shadow_radius" to "FLOAT DEFAULT 0.0",
            "shadow_strength" to "FLOAT DEFAULT 1.0",
            "scale_x" to "FLOAT DEFAULT 1.0",
            "scale_y" to "FLOAT DEFAULT 1.0",
            "scale_z" to "FLOAT DEFAULT 1.0",
            "created_at" to "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
            "updated_at" to "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
        )
        syncTableColumns("holograms", columnDefinitions)
    }

    private fun syncTableColumns(tableName: String, columnDefinitions: Map<String, String>) {
        val existingColumns = getTableColumns(tableName)
        val expectedColumns = columnDefinitions.keys

        val missingColumns = expectedColumns - existingColumns
        if (missingColumns.isNotEmpty()) {
            MSG.info("[GeekedCraft] Adding missing columns to $tableName: ${missingColumns.joinToString()}")
            missingColumns.forEach { column ->
                addColumn(tableName, column, columnDefinitions[column]!!)
            }
        }

        val extraColumns = existingColumns - expectedColumns
        if (extraColumns.isNotEmpty()) {
            MSG.info("[GeekedCraft] Extra columns in $tableName: ${extraColumns.joinToString()}")
            if (AUTO_REMOVE_EXTRA_COLUMNS) {
                extraColumns.forEach { column ->
                    removeColumn(tableName, column)
                }
            } else {
                MSG.info("[GeekedCraft] Set AUTO_REMOVE_EXTRA_COLUMNS=true to automatically remove them.")
            }
        }
    }

    private fun getTableColumns(tableName: String): Set<String> {
        val columns = mutableSetOf<String>()
        val sql = """
            SELECT COLUMN_NAME 
            FROM information_schema.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = ?
        """.trimIndent()

        connection?.prepareStatement(sql)?.use { ps ->
            ps.setString(1, tableName)
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME"))
                }
            }
        }
        return columns
    }

    private fun addColumn(tableName: String, columnName: String, definition: String) {
        try {
            val sql = "ALTER TABLE $tableName ADD COLUMN $columnName $definition"
            connection?.createStatement()?.use { stmt ->
                stmt.execute(sql)
                MSG.info("[GeekedCraft] Added column '$columnName' to $tableName table.")
            }
        } catch (e: Exception) {
            MSG.info("[GeekedCraft] Failed to add column '$columnName' to $tableName:")
            e.printStackTrace()
        }
    }

    private fun removeColumn(tableName: String, columnName: String) {
        try {
            val sql = "ALTER TABLE $tableName DROP COLUMN $columnName"
            connection?.createStatement()?.use { stmt ->
                stmt.execute(sql)
                MSG.info("[GeekedCraft] Removed column '$columnName' from $tableName table.")
            }
        } catch (e: Exception) {
            MSG.info("[GeekedCraft] Failed to remove column '$columnName' from $tableName:")
            e.printStackTrace()
        }
    }

    fun shutdown() {
        try {
            connection?.close()
            MSG.info("[GeekedCraft] MariaDB connection closed.")
        } catch (e: Exception) {
            MSG.info("[GeekedCraft] Error while closing MariaDB connection:")
            e.printStackTrace()
        } finally {
            connection = null
        }
    }

    fun getConnection(): Connection? = connection
}