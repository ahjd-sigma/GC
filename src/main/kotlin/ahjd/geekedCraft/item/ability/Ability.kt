package ahjd.geekedCraft.item.ability

import org.bukkit.entity.Player

interface Ability {
    val id: String
    val displayName: String
    val description: String
    val trigger: AbilityTrigger
    val cooldown: Long // in milliseconds

    fun execute(player: Player, context: AbilityContext)
}