package ahjd.geekedCraft.mob.util

import ahjd.geekedCraft.mob.stats.MobStats
import ahjd.geekedCraft.mob.util.dummy.DummyCombatTracker
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.LivingEntity

object DummyDisplay {
    fun updateDummyDisplay(entity: LivingEntity, s: MobStats) {
        val dps = DummyCombatTracker.getDPS(s.uuid)
        val totalDmg = DummyCombatTracker.getTotalDamage(s.uuid)
        val inCombat = DummyCombatTracker.isInCombat(s.uuid)

        entity.customName(Component.text("[DUMMY]", NamedTextColor.GRAY)
            .append(Component.text(" 🗡 ", NamedTextColor.GOLD))
            .append(Component.text("%.1f DPS".format(dps), if (inCombat) NamedTextColor.GREEN else NamedTextColor.DARK_GRAY))
            .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
            .append(Component.text("❤ ", NamedTextColor.RED))
            .append(Component.text("$totalDmg", getDamageColor(totalDmg))))
        entity.isCustomNameVisible = true
    }

    private fun getDamageColor(dmg: Int) = when {
        dmg >= 10000 -> NamedTextColor.DARK_PURPLE
        dmg >= 5000 -> NamedTextColor.LIGHT_PURPLE
        dmg >= 1000 -> NamedTextColor.GOLD
        dmg >= 500 -> NamedTextColor.YELLOW
        else -> NamedTextColor.WHITE
    }
}