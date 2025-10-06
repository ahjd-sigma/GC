package ahjd.geekedCraft.database.managers

import ahjd.geekedCraft.database.DatabaseManager
import ahjd.geekedCraft.database.objects.PlayerOBJ
import java.sql.ResultSet

object PlayerManager {

    private val cache = mutableMapOf<String, PlayerOBJ>()

    // Get a PlayerOBJ by UUID
    fun getPlayer(uuid: String): PlayerOBJ {
        return cache[uuid] ?: loadFromDB(uuid)?.also { cache[uuid] = it }
        ?: createNewPlayer(uuid).also { cache[uuid] = it }
    }

    // Save a PlayerOBJ to the database
    fun savePlayer(uuid: String, playerOBJ: PlayerOBJ) {
        val conn = DatabaseManager.getConnection() ?: return

        val sql = """
            INSERT INTO players (uuid, name, playtime, jumps, deaths, logins)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                playtime = VALUES(playtime),
                jumps = VALUES(jumps),
                deaths = VALUES(deaths),
                logins = VALUES(logins)
        """.trimIndent()

        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, uuid)
            stmt.setString(2, playerOBJ.name)
            stmt.setLong(3, playerOBJ.playtime)
            stmt.setInt(4, playerOBJ.jumps)
            stmt.setInt(5, playerOBJ.deaths)
            stmt.setInt(6, playerOBJ.logins)
            stmt.executeUpdate()
        }
    }

    // Load a PlayerOBJ from the database
    private fun loadFromDB(uuid: String): PlayerOBJ? {
        val conn = DatabaseManager.getConnection() ?: return null

        val sql = """
            SELECT name, playtime, jumps, deaths, logins
            FROM players WHERE uuid = ?
        """.trimIndent()

        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, uuid)
            val rs: ResultSet = stmt.executeQuery()
            if (rs.next()) {
                return PlayerOBJ(
                    name = rs.getString("name"),
                    playtime = rs.getLong("playtime"),
                    jumps = rs.getInt("jumps"),
                    deaths = rs.getInt("deaths"),
                    logins = rs.getInt("logins")
                )
            }
        }
        return null
    }

    // Create a new PlayerOBJ if it does not exist
    private fun createNewPlayer(uuid: String): PlayerOBJ {
        val newPlayer = PlayerOBJ()
        savePlayer(uuid, newPlayer)
        return newPlayer
    }

    // Save all cached PlayerOBJs to the database
    fun saveAll() {
        cache.forEach { (uuid, playerOBJ) -> savePlayer(uuid, playerOBJ) }
    }

    // Optional: Increment playtime for a player
    fun addPlaytime(uuid: String, millis: Long) {
        val player = getPlayer(uuid)
        player.playtime += millis
    }
}