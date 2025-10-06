package ahjd.geekedCraft.human.util

import ahjd.geekedCraft.human.HumanStatManager
import ahjd.geekedCraft.main.GeekedCraft
import org.bukkit.Bukkit

/**
 * Health regeneration ticker
 * Applies healthregen stat every 3 seconds (60 ticks)
 */
object HumanRegenTicker {
    private var taskId: Int? = null

    fun start(): Int {
        stop()
        val plugin = GeekedCraft.getInstance()

        taskId = plugin.scheduleRepeatingTask({
            Bukkit.getOnlinePlayers().forEach { player ->
                val stats = HumanStatManager.get(player.uniqueId.toString())
                if (player.isDead || stats.healthregen == 0) return@forEach
                if (stats.health >= stats.maxhealth && stats.healthregen > 0) return@forEach
                if (stats.health <= 0 && stats.healthregen < 0) return@forEach
                stats.modifyBaseValue("health", stats.healthregen)
            }
        }, 0L, 100L)

        return taskId!!
    }

    fun stop() {
        taskId?.let { Bukkit.getScheduler().cancelTask(it) }
        taskId = null
    }
}