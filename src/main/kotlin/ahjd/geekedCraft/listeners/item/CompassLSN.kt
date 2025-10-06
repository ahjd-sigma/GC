package ahjd.geekedCraft.listeners.item

import ahjd.geekedCraft.guis.compass.CompassGUI
import ahjd.geekedCraft.guis.compass.CompassGUI.hasOpenGUI
import ahjd.geekedCraft.guis.compass.CompassGUI.isStatCompass
import ahjd.geekedCraft.item.itemstacks.StatCompass
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.*
import org.bukkit.plugin.java.JavaPlugin

class CompassLSN(private val plugin: JavaPlugin) : Listener {
    companion object {
        const val COMPASS_SLOT = 8
        private val playersInStatsGUI = mutableSetOf<java.util.UUID>()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        ensureCompassInSlot(player)
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            plugin,
            Runnable { ensureCompassInSlot(player) },
            1L
        )
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        if (playersInStatsGUI.contains(player.uniqueId)) {
            event.isCancelled = true
            return
        }

        if (isStatCompass(event.currentItem)) {
            event.isCancelled = true
            if (event.click.isLeftClick && !hasOpenGUI(player)) {
                openStatsGUIWithTracking(player)
            }
        }
    }


    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        playersInStatsGUI.remove(player.uniqueId)
    }

    @EventHandler
    fun onSwapItem(event: PlayerSwapHandItemsEvent) {
        if (isStatCompass(event.offHandItem)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action !in listOf(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK,
                Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK)) return

        if (isStatCompass(event.player.inventory.itemInMainHand)) {
            event.isCancelled = true
            if (!hasOpenGUI(event.player)) {
                openStatsGUIWithTracking(event.player)
            }
        }
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        if (isStatCompass(event.itemDrop.itemStack)) {
            event.isCancelled = true
            if (!hasOpenGUI(event.player)) {
                openStatsGUIWithTracking(event.player)
            }
        }
    }

    private fun openStatsGUIWithTracking(player: Player) {
        playersInStatsGUI.add(player.uniqueId)
        CompassGUI.openStatsGUI(player)
    }

    private fun ensureCompassInSlot(player: Player) {
        // Clear any existing compasses
        clearCompassesFromInventory(player)

        // Place compass in slot 8
        player.inventory.setItem(COMPASS_SLOT, StatCompass.createCompass())
    }

    private fun clearCompassesFromInventory(player: Player) {
        for (i in player.inventory.contents.indices) {
            if (i == COMPASS_SLOT) continue // Don't clear slot 8 yet

            val item = player.inventory.getItem(i)
            if (isStatCompass(item)) {
                player.inventory.setItem(i, null)
            }
        }
    }
}