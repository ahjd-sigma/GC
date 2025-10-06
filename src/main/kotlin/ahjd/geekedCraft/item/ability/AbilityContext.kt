package ahjd.geekedCraft.item.ability

import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

data class AbilityContext(
    val damageEvent: EntityDamageByEntityEvent? = null,
    val interactEvent: PlayerInteractEvent? = null,
    val customData: Map<String, Any> = emptyMap()
)
