package ahjd.geekedCraft.human.util

import ahjd.geekedCraft.human.HumanStatManager
import ahjd.geekedCraft.main.GeekedCraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object HumanHealthDisplay {
    private const val HEART = "\u2764"
    private var taskId: Int? = null

    private fun updateActionBar(player: Player) {
        val stats = HumanStatManager.get(player.uniqueId.toString())

        val healthText = Component.text()
            .append(Component.text("$HEART ", NamedTextColor.RED))
            .append(Component.text("${stats.health}/${stats.maxhealth}", NamedTextColor.RED))
            .build()

        player.sendActionBar(healthText)
    }

    fun startTicker(): Int {
        stop()
        val plugin = GeekedCraft.getInstance()

        taskId = plugin.scheduleRepeatingTask({
            Bukkit.getOnlinePlayers().forEach { player ->
                updateActionBar(player)
            }
        }, 0L, 5L)

        return taskId!!
    }

    fun stop() {
        taskId?.let { Bukkit.getScheduler().cancelTask(it) }
        taskId = null
    }
}