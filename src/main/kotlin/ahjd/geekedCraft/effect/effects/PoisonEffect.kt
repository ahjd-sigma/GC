package ahjd.geekedCraft.effect.effects

import ahjd.geekedCraft.effect.Effect
import ahjd.geekedCraft.human.HumanStats

class PoisonEffect(
    override val durationTicks: Long,
    private val damagePerSecond: Int
) : Effect {
    override val id = "poison"
    override val displayName = "§2☠ Poison"
    override val description = "Taking $damagePerSecond damage per second"
    override val periodic = true
    override val tickInterval = 20L  // Damage every second (20 ticks = 1 second)

    override fun onApply(stats: HumanStats) {
        // Could send message, play sound, etc.
    }

    override fun onTick(stats: HumanStats) {
        // Damage the player every second
        stats.modifyBaseValue("health", -damagePerSecond)
    }

    override fun onRemove(stats: HumanStats) {
        // Cleanup when poison ends
    }
}