package ahjd.geekedCraft.item.ability.abilities

import ahjd.geekedCraft.item.ability.Ability
import ahjd.geekedCraft.item.ability.AbilityContext
import ahjd.geekedCraft.item.ability.AbilityTrigger
import org.bukkit.entity.Player

class SunsWrathAbility : Ability {

    override val id = "suns_wrath"
    override val displayName = "Sun's Wrath"
    override val description = "Your attacks burn enemies with solar fire"
    override val trigger = AbilityTrigger.ON_HIT
    override val cooldown = 0L

    override fun execute(player: Player, context: AbilityContext) {
        context.damageEvent?.let { event ->
            event.entity.fireTicks = 100 // 5 seconds of fire
        }
    }
}