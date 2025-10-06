package ahjd.geekedCraft.mob.misc

import ahjd.geekedCraft.mob.stats.MobStats
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.LivingEntity

object MobHealthDisplay {
    fun updateHealthDisplay(entity: LivingEntity, stats: MobStats) {
        entity.customName(Component.text(stats.name, NamedTextColor.RED)
            .append(Component.text(" ❤ ", NamedTextColor.RED))
            .append(Component.text("${stats.health}/${stats.maxhealth}", getHealthColor(stats.health, stats.maxhealth))))
        entity.isCustomNameVisible = true
    }

    private fun getHealthColor(current: Int, max: Int): TextColor {
        val pct = (current.toDouble() / max * 100).toInt()
        return when {
            pct > 75 -> NamedTextColor.GREEN
            pct > 50 -> NamedTextColor.YELLOW
            pct > 25 -> NamedTextColor.GOLD
            else -> NamedTextColor.RED
        }
    }
}