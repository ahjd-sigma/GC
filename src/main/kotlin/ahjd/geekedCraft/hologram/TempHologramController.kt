package ahjd.geekedCraft.hologram

import ahjd.geekedCraft.util.MSG
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.TextDisplay
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object TempHologramController {

    // Track temporary entities
    private val tempEntities = mutableSetOf<UUID>()
    private const val MAX_TEMP_HOLOGRAMS = 500

    /**
     * Spawn a temporary hologram that auto-despawns after a duration
     * @param location Where to spawn
     * @param text Text to display
     * @param durationTicks How long to keep it alive (20 ticks = 1 second)
     * @param plugin Your plugin instance
     * @param config Optional configuration lambda
     * @return The spawned TextDisplay entity
     */
    fun spawnTemp(
        location: Location,
        text: Component,
        durationTicks: Long = 40L, // 2 seconds default
        plugin: Plugin,
        config: (TextDisplay.() -> Unit)? = null
    ): TextDisplay? {
        val world = location.world ?: return null

        if (tempEntities.size >= MAX_TEMP_HOLOGRAMS) {
            // Log warning if you're constantly hitting the limit
            if (tempEntities.size == MAX_TEMP_HOLOGRAMS) {
                MSG.warn("Temporary hologram limit reached (${getActiveCount()}/$MAX_TEMP_HOLOGRAMS)")
            }
            return null
        }

        val textDisplay = world.spawn(location, TextDisplay::class.java) { display ->
            display.text(text)
            // Defaults for damage indicators
            display.billboard = Display.Billboard.CENTER
            display.isShadowed = true
            display.alignment = TextDisplay.TextAlignment.CENTER
            display.viewRange = 1.0f

            // Apply custom config if provided
            config?.invoke(display)
        }

        // Track as temporary
        tempEntities.add(textDisplay.uniqueId)

        // Auto-remove after duration
        object : BukkitRunnable() {
            override fun run() {
                if (textDisplay.isValid) {
                    textDisplay.remove()
                }
                tempEntities.remove(textDisplay.uniqueId)
            }
        }.runTaskLater(plugin, durationTicks)

        return textDisplay
    }

    /**
     * Clean up all temporary holograms (call on plugin disable)
     */
    fun cleanupAll() {
        tempEntities.removeIf { uuid ->
            val entity = org.bukkit.Bukkit.getEntity(uuid)
            if (entity is TextDisplay && entity.isValid) {
                entity.remove()
            }
            true
        }
    }

    /**
     * Get count of active temporary holograms
     */
    private fun getActiveCount(): Int = tempEntities.size
}