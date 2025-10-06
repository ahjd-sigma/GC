package ahjd.geekedCraft.listeners.human

import ahjd.geekedCraft.events.human.HumanDeathEvent
import ahjd.geekedCraft.human.HumanStatManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class HumanDeathLSN : Listener {

    @EventHandler
     fun onHumanDeath(event: HumanDeathEvent){
        val player = event.player

        if (player.health > 0.0) {
            player.health = 0.0
        }

        val humanOBJ = HumanStatManager.get(player.uniqueId.toString())
        humanOBJ.setBaseValue("health", humanOBJ.maxhealth)
    }
}