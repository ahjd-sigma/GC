package ahjd.geekedCraft.effect.effects

import ahjd.geekedCraft.effect.Effect
import ahjd.geekedCraft.human.HumanStats

class RegenerationEffect(
    override val durationTicks: Long,
    private val healPerSecond: Int
) : Effect {
    override val id = "regeneration"
    override val displayName = "§c♥ Regeneration"
    override val description = "Healing $healPerSecond health per second"
    override val periodic = true
    override val tickInterval = 20L  // Heal every second

    override fun onApply(stats: HumanStats) {}

    override fun onTick(stats: HumanStats) {
        // Heal the player every second (won't go over maxhealth)
        stats.modifyBaseValue("health", healPerSecond)
    }

    override fun onRemove(stats: HumanStats) {}
}
