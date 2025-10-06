package ahjd.geekedCraft.listeners.mob

import ahjd.geekedCraft.events.mob.MobDeathEvent
import ahjd.geekedCraft.mob.MobManager
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class MobDeathLSN : Listener {

    @EventHandler
    fun onMobDeath(event: MobDeathEvent){
        val mob = event.mob
        mob.health = 0.0
        MobManager.remove(mob.uniqueId.toString())
    }

    @EventHandler
    fun onEntityMobDeath(event: EntityDeathEvent) {
        val entity = event.entity as? LivingEntity ?: return

        val uuid = entity.uniqueId.toString()

        if (MobManager.has(uuid)) {
            // beam the natural drops
            event.drops.clear()
            event.droppedExp = 0

            MobManager.remove(uuid)
        }
    }
}