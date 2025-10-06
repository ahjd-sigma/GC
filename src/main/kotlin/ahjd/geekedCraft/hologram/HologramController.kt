package ahjd.geekedCraft.hologram

import ahjd.geekedCraft.database.managers.HologramManager
import ahjd.geekedCraft.database.objects.HologramOBJ
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import java.util.*

object HologramController {

    // Track entity UUIDs for holograms (hologramUUID -> entityUUID)
    private val entityMap = mutableMapOf<UUID, UUID>()

    // Spawn a hologram in the world
    fun spawn(hologramOBJ: HologramOBJ): TextDisplay? {
        val location = hologramOBJ.location ?: return null
        val world = location.world ?: return null

        // Generate hologram UUID if not exists
        if (hologramOBJ.uuid == null) {
            hologramOBJ.uuid = UUID.randomUUID()
        }

        val hologramUUID = hologramOBJ.uuid!!

        // Spawn new entity
        val textDisplay = world.spawn(location, TextDisplay::class.java) { display ->
            display.text(hologramOBJ.text)
            display.alignment = hologramOBJ.alignment
            display.isShadowed = hologramOBJ.shadowed
            display.isSeeThrough = hologramOBJ.seeThrough
            display.backgroundColor = colorFromARGB(hologramOBJ.backgroundColor)
            display.billboard = hologramOBJ.billboard
            display.textOpacity = hologramOBJ.textOpacity
            display.lineWidth = hologramOBJ.lineWidth
            hologramOBJ.brightness?.let { display.brightness = Display.Brightness(it.first, it.second) }
            display.viewRange = hologramOBJ.viewRange
            display.shadowRadius = hologramOBJ.shadowRadius
            display.shadowStrength = hologramOBJ.shadowStrength

            // Set scale
            hologramOBJ.scale?.let { (x, y, z) ->
                display.transformation = display.transformation.apply {
                    scale.set(x.toDouble(), y.toDouble(), z.toDouble())
                }
            }
        }

        // Map hologram UUID to entity UUID
        entityMap[hologramUUID] = textDisplay.uniqueId

        // Save hologram
        HologramManager.saveHologram(hologramOBJ)

        return textDisplay
    }

    // Despawn a hologram
    fun despawn(hologramOBJ: HologramOBJ): Boolean {
        val hologramUUID = hologramOBJ.uuid ?: return false
        val entityUUID = entityMap[hologramUUID] ?: return false
        val entity = Bukkit.getEntity(entityUUID) as? TextDisplay ?: return false

        entity.remove()
        entityMap.remove(hologramUUID)
        return true
    }

    // Despawn entities within radius
    fun despawnInRadius(location: Location, radius: Double): Int {
        var count = 0
        location.world?.getNearbyEntities(location, radius, radius, radius)
            ?.filterIsInstance<TextDisplay>()
            ?.forEach {
                it.remove()
                // Remove from entity map
                entityMap.entries.removeIf { entry -> entry.value == it.uniqueId }
                count++
            }
        return count
    }

    // Move hologram and teleport entity
    fun move(hologramOBJ: HologramOBJ, newLocation: Location): Boolean {
        hologramOBJ.location = newLocation
        HologramManager.saveHologram(hologramOBJ)

        val hologramUUID = hologramOBJ.uuid ?: return false
        val entityUUID = entityMap[hologramUUID] ?: return false
        val entity = Bukkit.getEntity(entityUUID) as? TextDisplay ?: return false

        if (entity.isValid) {
            entity.teleport(newLocation)
            return true
        }
        return false
    }

    // Update spawned entity with hologram properties
    fun updateEntity(hologramOBJ: HologramOBJ, update: (TextDisplay) -> Unit): Boolean {
        val hologramUUID = hologramOBJ.uuid ?: return false
        val entityUUID = entityMap[hologramUUID] ?: return false
        val entity = Bukkit.getEntity(entityUUID) as? TextDisplay ?: return false

        if (entity.isValid) {
            update(entity)
            return true
        }
        return false
    }

    // Check if hologram is currently spawned
    fun isSpawned(hologramOBJ: HologramOBJ): Boolean {
        val hologramUUID = hologramOBJ.uuid ?: return false
        val entityUUID = entityMap[hologramUUID] ?: return false
        val entity = Bukkit.getEntity(entityUUID)
        return entity is TextDisplay && entity.isValid
    }

    private fun colorFromARGB(argb: Int?): Color? {
        if (argb == null) return null
        val rgb = argb and 0xFFFFFF  // mask out alpha
        return Color.fromRGB(rgb)
    }

    // Get the spawned entity
    fun getEntity(hologramOBJ: HologramOBJ): TextDisplay? {
        val hologramUUID = hologramOBJ.uuid ?: return null
        val entityUUID = entityMap[hologramUUID] ?: return null
        return Bukkit.getEntity(entityUUID) as? TextDisplay
    }

    // Spawn all holograms from HologramManager
    fun spawnAll(): Int {
        var count = 0
        HologramManager.getAllHolograms().values.forEach { hologram ->
            if (hologram.location != null && !isSpawned(hologram)) {
                if (spawn(hologram) != null) {
                    count++
                }
            }
        }
        return count
    }

    // Despawn all currently spawned holograms
    fun despawnAll(): Int {
        var count = 0
        HologramManager.getAllHolograms().values.forEach { hologram ->
            if (despawn(hologram)) {
                count++
            }
        }
        return count
    }
    // Clean up orphaned TextDisplay entities (not in database)
    // Call this on server start to remove leftover entities from crashes
    fun cleanupOrphanedEntities(): Int {
        var count = 0
        val validEntityUUIDs = entityMap.values.toSet()

        // Check all loaded worlds
        Bukkit.getWorlds().forEach { world ->
            world.entities.filterIsInstance<TextDisplay>().forEach { entity ->
                // If it's not tracked by us, remove it
                if (!validEntityUUIDs.contains(entity.uniqueId)) {
                    entity.remove()
                    count++
                }
            }
        }

        return count
    }
}