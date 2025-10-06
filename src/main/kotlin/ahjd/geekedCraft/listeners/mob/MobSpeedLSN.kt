package ahjd.geekedCraft.listeners.mob

import ahjd.geekedCraft.events.mob.MobSpeedChangeEvent
import ahjd.geekedCraft.util.SpeedNormalizer
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MobSpeedLSN  : Listener {

    @EventHandler
    fun onMobSpeedChange(event: MobSpeedChangeEvent) {
        event.mob.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue = SpeedNormalizer.normalize(event.newSpeed).toDouble()
    }
}