package ahjd.geekedCraft.human.util

import ahjd.geekedCraft.human.HumanStatManager
import ahjd.geekedCraft.main.GeekedCraft
import org.bukkit.Bukkit

/**
 * Health regeneration ticker
 * Applies healthregen stat every 3 seconds (60 ticks)
 */
object HumanRegenTicker {

    fun start() {
        val plugin = GeekedCraft.getInstance()

        plugin.scheduleRepeatingTask({
            Bukkit.getOnlinePlayers().forEach { player ->
                val stats = HumanStatManager.get(player.uniqueId.toString())

                // Skip if player is dead or no regen
                if (player.isDead || stats.healthregen == 0) return@forEach

                // Skip if already at bounds
                if (stats.health >= stats.maxhealth && stats.healthregen > 0) return@forEach
                if (stats.health <= 0 && stats.healthregen < 0) return@forEach

                // Apply regen to base health (preserves equipment bonuses)
                stats.modifyBaseValue("health", stats.healthregen)
            }
        }, 0L, 100L)
    }
}