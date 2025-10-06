package ahjd.geekedCraft.database.managers

import ahjd.geekedCraft.database.DatabaseManager
import ahjd.geekedCraft.database.objects.HologramOBJ
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import java.sql.ResultSet
import java.util.*

object HologramManager {

    private val cache = mutableMapOf<UUID, HologramOBJ>()
    private val nameCache = mutableMapOf<String, UUID>()
    private val gsonSerializer = GsonComponentSerializer.gson()

    // Get a HologramOBJ by UUID
    private fun getHologram(uuid: UUID): HologramOBJ? {
        return cache[uuid] ?: loadFromDB(uuid)?.also { hologram ->
            hologram.uuid?.let {
                cache[it] = hologram
                nameCache[hologram.name.lowercase()] = it
            }
        }
    }

    // Get a HologramOBJ by name
    fun getHologramByName(name: String): HologramOBJ? {
        val uuid = nameCache[name.lowercase()]
        return if (uuid != null) {
            getHologram(uuid)
        } else {
            // Search in cache
            cache.values.find { it.name.equals(name, ignoreCase = true) }
                ?: loadFromDBByName(name)?.also { hologram ->
                    hologram.uuid?.let {
                        cache[it] = hologram
                        nameCache[hologram.name.lowercase()] = it
                    }
                }
        }
    }

    // Get all holograms
    fun getAllHolograms(): Map<UUID, HologramOBJ> {
        return cache.toMap()
    }

    fun saveHologram(hologramOBJ: HologramOBJ) {
        val uuid = hologramOBJ.uuid ?: UUID.randomUUID().also { hologramOBJ.uuid = it }
        val conn = DatabaseManager.getConnection() ?: return

        val sql = """
        INSERT INTO holograms (
            uuid, name, text, world, x, y, z, alignment, shadowed, see_through,
            bg_color, billboard, text_opacity, line_width,
            brightness_block, brightness_sky, view_range, shadow_radius,
            shadow_strength, scale_x, scale_y, scale_z
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON DUPLICATE KEY UPDATE
            name = ?, text = ?, world = ?, x = ?, y = ?, z = ?, alignment = ?,
            shadowed = ?, see_through = ?, bg_color = ?,
            billboard = ?, text_opacity = ?, line_width = ?,
            brightness_block = ?, brightness_sky = ?, view_range = ?,
            shadow_radius = ?, shadow_strength = ?, scale_x = ?, scale_y = ?, scale_z = ?
    """.trimIndent()

        conn.prepareStatement(sql).use { stmt ->
            val text = gsonSerializer.serialize(hologramOBJ.text)
            val world = hologramOBJ.location?.world?.name
            val x = hologramOBJ.location?.x ?: 0.0
            val y = hologramOBJ.location?.y ?: 0.0
            val z = hologramOBJ.location?.z ?: 0.0

            // INSERT
            stmt.setString(1, uuid.toString())
            stmt.setString(2, hologramOBJ.name)
            stmt.setString(3, text)
            stmt.setString(4, world)
            stmt.setDouble(5, x)
            stmt.setDouble(6, y)
            stmt.setDouble(7, z)
            stmt.setString(8, hologramOBJ.alignment.name)
            stmt.setBoolean(9, hologramOBJ.shadowed)
            stmt.setBoolean(10, hologramOBJ.seeThrough)
            stmt.setObject(11, hologramOBJ.backgroundColor)
            stmt.setString(12, hologramOBJ.billboard.name)
            stmt.setByte(13, hologramOBJ.textOpacity)
            stmt.setInt(14, hologramOBJ.lineWidth)
            stmt.setObject(15, hologramOBJ.brightness?.first)
            stmt.setObject(16, hologramOBJ.brightness?.second)
            stmt.setFloat(17, hologramOBJ.viewRange)
            stmt.setFloat(18, hologramOBJ.shadowRadius)
            stmt.setFloat(19, hologramOBJ.shadowStrength)
            stmt.setFloat(20, hologramOBJ.scale?.first ?: 1.0f)
            stmt.setFloat(21, hologramOBJ.scale?.second ?: 1.0f)
            stmt.setFloat(22, hologramOBJ.scale?.third ?: 1.0f)

            // UPDATE
            stmt.setString(23, hologramOBJ.name)
            stmt.setString(24, text)
            stmt.setString(25, world)
            stmt.setDouble(26, x)
            stmt.setDouble(27, y)
            stmt.setDouble(28, z)
            stmt.setString(29, hologramOBJ.alignment.name)
            stmt.setBoolean(30, hologramOBJ.shadowed)
            stmt.setBoolean(31, hologramOBJ.seeThrough)
            stmt.setObject(32, hologramOBJ.backgroundColor)
            stmt.setString(33, hologramOBJ.billboard.name)
            stmt.setByte(34, hologramOBJ.textOpacity)
            stmt.setInt(35, hologramOBJ.lineWidth)
            stmt.setObject(36, hologramOBJ.brightness?.first)
            stmt.setObject(37, hologramOBJ.brightness?.second)
            stmt.setFloat(38, hologramOBJ.viewRange)
            stmt.setFloat(39, hologramOBJ.shadowRadius)
            stmt.setFloat(40, hologramOBJ.shadowStrength)
            stmt.setFloat(41, hologramOBJ.scale?.first ?: 1.0f)
            stmt.setFloat(42, hologramOBJ.scale?.second ?: 1.0f)
            stmt.setFloat(43, hologramOBJ.scale?.third ?: 1.0f)

            stmt.executeUpdate()
        }

        cache[uuid] = hologramOBJ
        nameCache[hologramOBJ.name.lowercase()] = uuid
    }

    // Load a HologramOBJ from the database by UUID
    private fun loadFromDB(uuid: UUID): HologramOBJ? {
        val conn = DatabaseManager.getConnection() ?: return null

        val sql = "SELECT * FROM holograms WHERE uuid = ?"

        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, uuid.toString())
            val rs: ResultSet = stmt.executeQuery()
            if (rs.next()) {
                return parseHologramFromResultSet(rs)
            }
        }
        return null
    }

    // Load a HologramOBJ from the database by name
    private fun loadFromDBByName(name: String): HologramOBJ? {
        val conn = DatabaseManager.getConnection() ?: return null

        val sql = "SELECT * FROM holograms WHERE name = ?"

        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, name)
            val rs: ResultSet = stmt.executeQuery()
            if (rs.next()) {
                return parseHologramFromResultSet(rs)
            }
        }
        return null
    }

    // Load all holograms from the database
    fun loadAll() {
        val conn = DatabaseManager.getConnection() ?: return

        val sql = "SELECT * FROM holograms"

        conn.prepareStatement(sql).use { stmt ->
            val rs: ResultSet = stmt.executeQuery()
            while (rs.next()) {
                val hologram = parseHologramFromResultSet(rs)
                hologram.uuid?.let {
                    cache[it] = hologram
                    nameCache[hologram.name.lowercase()] = it
                }
            }
        }
    }

    // Parse HologramOBJ from ResultSet
    private fun parseHologramFromResultSet(rs: ResultSet): HologramOBJ {
        val worldName = rs.getString("world")
        val location = if (worldName != null) {
            val world = Bukkit.getWorld(worldName)
            world?.let { Location(it, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z")) }
        } else null

        val bgColor = rs.getObject("bg_color") as? Int
        val brightness = (rs.getObject("brightness_block") as? Int)?.let { block ->
            (rs.getObject("brightness_sky") as? Int)?.let { sky -> Pair(block, sky) }
        }

        // Parse scale
        val scaleX = rs.getFloat("scale_x")
        val scaleY = rs.getFloat("scale_y")
        val scaleZ = rs.getFloat("scale_z")
        val scale = if (scaleX == 1.0f && scaleY == 1.0f && scaleZ == 1.0f) {
            null // Default scale, don't store
        } else {
            Triple(scaleX, scaleY, scaleZ)
        }

        // Handle nullable UUID
        val uuidString = rs.getString("uuid")
        val uuid = if (uuidString != null) {
            try {
                UUID.fromString(uuidString)
            } catch (e: IllegalArgumentException) {
                null
            }
        } else null

        return HologramOBJ(
            uuid = uuid,
            name = rs.getString("name"),
            text = gsonSerializer.deserialize(rs.getString("text")) as? TextComponent ?: Component.text(""),
            location = location,
            alignment = TextDisplay.TextAlignment.valueOf(rs.getString("alignment")),
            shadowed = rs.getBoolean("shadowed"),
            seeThrough = rs.getBoolean("see_through"),
            backgroundColor = bgColor,
            billboard = Display.Billboard.valueOf(rs.getString("billboard")),
            textOpacity = rs.getByte("text_opacity"),
            lineWidth = rs.getInt("line_width"),
            brightness = brightness,
            viewRange = rs.getFloat("view_range"),
            shadowRadius = rs.getFloat("shadow_radius"),
            shadowStrength = rs.getFloat("shadow_strength"),
            scale = scale
        )
    }

    // Delete a hologram by UUID
    fun deleteHologram(uuid: UUID) {
        val conn = DatabaseManager.getConnection() ?: return

        val sql = "DELETE FROM holograms WHERE uuid = ?"

        conn.prepareStatement(sql).use { stmt ->
            stmt.setString(1, uuid.toString())
            stmt.executeUpdate()
        }

        val hologram = cache.remove(uuid)
        hologram?.let { nameCache.remove(it.name.lowercase()) }
    }

    // Save all cached holograms to the database
    fun saveAll() {
        cache.values.forEach { hologram -> saveHologram(hologram) }
    }
}