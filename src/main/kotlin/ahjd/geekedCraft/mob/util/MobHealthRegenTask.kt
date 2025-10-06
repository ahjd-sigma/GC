package ahjd.geekedCraft.mob.util

import ahjd.geekedCraft.main.GeekedCraft
import ahjd.geekedCraft.mob.MobManager
import org.bukkit.Bukkit

object MobHealthRegenTask {
    private var taskId: Int? = null

    fun start(plugin: GeekedCraft): Int {
        stop()
        taskId = plugin.scheduleRepeatingTask({
            MobManager.getAllMobs()
                .filter { it.healthregen > 0 }
                .forEach { it.setValue("health", (it.health + it.healthregen).coerceAtMost(it.maxhealth)) }
        }, 1L, 100L)
        return taskId!!
    }

    fun stop() {
        taskId?.let { Bukkit.getScheduler().cancelTask(it) }
        taskId = null
    }
}