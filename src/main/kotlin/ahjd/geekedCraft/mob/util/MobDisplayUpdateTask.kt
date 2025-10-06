package ahjd.geekedCraft.mob.util

import ahjd.geekedCraft.main.GeekedCraft
import ahjd.geekedCraft.mob.MobManager
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import java.util.*

object MobDisplayUpdateTask {
    private var taskId: Int? = null

    fun start(plugin: GeekedCraft): Int {
        stop()
        taskId = plugin.scheduleRepeatingTask({
            MobManager.getAllMobs().forEach { stats ->
                runCatching {
                    Bukkit.getEntity(UUID.fromString(stats.uuid)) as LivingEntity
                }.onSuccess { entity ->
                    if (stats.isDummy) DummyDisplay.updateDummyDisplay(entity, stats)
                    else MobHealthDisplay.updateHealthDisplay(entity, stats)
                }
            }
        }, 3L, 3L)
        return taskId!!
    }

    fun stop() {
        taskId?.let { Bukkit.getScheduler().cancelTask(it) }
        taskId = null
    }
}