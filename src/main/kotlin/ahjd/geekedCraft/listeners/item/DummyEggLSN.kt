package ahjd.geekedCraft.listeners.item

import ahjd.geekedCraft.item.itemstacks.DummyEgg
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

class DummyEggLSN : Listener {

    @EventHandler
    fun onEggUse(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return

        val item = event.item ?: return
        if (!DummyEgg.isDummyEgg(item)) return

        event.isCancelled = true

        val player = event.player
        val location = player.location.clone().add(player.location.direction.multiply(2))
        location.y = player.location.y

        DummyEgg.spawnDummy(location)

        item.amount -= 1
        player.sendMessage("§aTraining dummy spawned!")
    }
}