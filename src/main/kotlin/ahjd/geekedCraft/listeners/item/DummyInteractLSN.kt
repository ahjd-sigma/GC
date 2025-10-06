package ahjd.geekedCraft.listeners.item

import ahjd.geekedCraft.mob.MobManager
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

class DummyInteractLSN(private val guiListener: DummyGUILSN) : Listener {

    @EventHandler
    fun onDummyInteract(event: PlayerInteractEntityEvent) {
        val player = event.player
        val entity = event.rightClicked as? LivingEntity ?: return

        if (!player.isSneaking) return

        val uuid = entity.uniqueId.toString()
        if (!MobManager.has(uuid)) return

        val mobStats = MobManager.get(uuid)
        if (!mobStats.isDummy) return

        event.isCancelled = true

        // Open GUI and track the dummy UUID
        guiListener.openGUI(player, mobStats.uuid)
    }
}