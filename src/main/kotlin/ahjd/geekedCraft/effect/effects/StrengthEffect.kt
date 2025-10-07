package ahjd.geekedCraft.effect.effects

import ahjd.geekedCraft.effect.Effect
import ahjd.geekedCraft.human.HumanStats

class StrengthEffect(
    override val durationTicks: Long,
    private val damageBonus: Int
) : Effect {
    override val id = "strength"
    override val displayName = "§6⚔ Strength"
    override val description = "Increased damage by $damageBonus"
    override val periodic = false
    override val tickInterval = 1L  // Ignored when periodic = false

    private var appliedBonus = 0

    override fun onApply(stats: HumanStats) {
        appliedBonus = damageBonus
        stats.modifyBaseValue("damageRaw", damageBonus)
    }

    override fun onRemove(stats: HumanStats) {
        stats.modifyBaseValue("damageRaw", -appliedBonus)
    }
}
