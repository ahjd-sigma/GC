package ahjd.geekedCraft.listeners.human

import ahjd.geekedCraft.database.managers.PlayerManager
import ahjd.geekedCraft.human.HumanStatManager
import ahjd.geekedCraft.main.GeekedCraft
import ahjd.geekedCraft.util.MSG
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerLSN : Listener {

    // Helper: runs a block with both Player and UUID string
    private inline fun withPlayer(eventPlayer: Player, block: (player: Player, uuid: String) -> Unit) {
        block(eventPlayer, eventPlayer.uniqueId.toString())
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoin(event: PlayerJoinEvent) = withPlayer(event.player) { player, uuid ->
        MSG.info("SAVING PLAYER ${player.name}")

        // Persistent stats (database-backed)
        val playerOBJ = PlayerManager.getPlayer(uuid)
        playerOBJ.name = player.name
        playerOBJ.logins += 1
        PlayerManager.savePlayer(uuid, playerOBJ)

        // Temporary combat stats (memory only)
        HumanStatManager.get(uuid) // ensures a stats object exists
    }

    @EventHandler
    fun onPlayerJump(event: PlayerJumpEvent) = withPlayer(event.player) { _, uuid ->
        val playerOBJ = PlayerManager.getPlayer(uuid)
        playerOBJ.jumps += 1
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) = withPlayer(event.player) { _, uuid ->
        val playerOBJ = PlayerManager.getPlayer(uuid)
        playerOBJ.deaths += 1
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) = withPlayer(event.player) { _, uuid ->
        HumanStatManager.remove(uuid) // clear combat stats from memory

        // Save player data
        val playerOBJ = PlayerManager.getPlayer(uuid)
        PlayerManager.savePlayer(uuid, playerOBJ)
    }

    fun startPlaytimeTracking() {
        val plugin = GeekedCraft.getInstance()

        // Increment playtime every 60 seconds (60 * 1000 ms)
        plugin.scheduleRepeatingTask({
            for (player in plugin.server.onlinePlayers) {
                val uuid = player.uniqueId.toString()
                PlayerManager.addPlaytime(uuid, 60_000L) // add 1 minute = 60,000 ms
            }
        }, 0L, 20L * 60) // 20 ticks per second * 60 seconds = 1 minute
    }

    @EventHandler
    fun onPlayerHunger(event: FoodLevelChangeEvent){
        val player = event.entity
        if (player is Player) {
            // Cancel the event
            event.isCancelled = true

            // Just in case
            player.foodLevel = 20
            player.saturation = 20f
        }
    }
}
