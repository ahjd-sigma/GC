package ahjd.geekedCraft.effect.effects

import ahjd.geekedCraft.effect.Effect
import ahjd.geekedCraft.human.HumanStats

class SpeedBoostEffect(
    override val durationTicks: Long,
    private val speedBonus: Int
) : Effect {
    override val id = "speed_boost"
    override val displayName = "§b⚡ Speed Boost"
    override val description = "Movement speed increased by $speedBonus%"
    override val periodic = false  // Not periodic - just applies/removes stat
    override val tickInterval = 1L  // Ignored when periodic = false

    private var appliedBonus = 0

    override fun onApply(stats: HumanStats) {
        appliedBonus = speedBonus
        stats.modifyBaseValue("speed", speedBonus)
    }

    override fun onRemove(stats: HumanStats) {
        // Remove the speed bonus
        stats.modifyBaseValue("speed", -appliedBonus)
    }
}

