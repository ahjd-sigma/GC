package ahjd.geekedCraft.commands.database

import ahjd.geekedCraft.commands.SubCommand
import net.kyori.adventure.text.minimessage.MiniMessage
import ahjd.geekedCraft.database.managers.PlayerManager
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class StatsSBCMD : SubCommand {
    override val name = "stats"
    override val description = "Shows a player's stats"
    override val permission = "geekedcraft.db.stats"

    override fun execute(sender: CommandSender, args: Array<String>) {
        val player = sender as? Player ?: return  // Only players can run this

        val targetPlayer = if (args.isNotEmpty()) {
            Bukkit.getPlayerExact(args[0]) ?: run {
                player.sendMessage("Player not found or not online.")
                return
            }
        } else {
            player
        }

        val playerObj = PlayerManager.getPlayer(targetPlayer.uniqueId.toString())
        val mm = MiniMessage.miniMessage()

        // Format playtime from milliseconds to hh:mm:ss
        val playtimeStr = formatPlaytime(playerObj.playtime)

        player.sendMessage(mm.deserialize("""
        <gold>${playerObj.name}'s Stats</gold>
        <red>Deaths:</red> <white>${playerObj.deaths}</white>
        <green>Logins:</green> <white>${playerObj.logins}</white>
        <aqua>Jumps:</aqua> <white>${playerObj.jumps}</white>
        <yellow>Playtime:</yellow> <white>$playtimeStr</white>
    """.trimIndent()))
    }

    // Converts milliseconds to hh:mm:ss
    private fun formatPlaytime(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return if (args.size == 1) {
            Bukkit.getOnlinePlayers().map { it.name }
                .filter { it.startsWith(args[0], ignoreCase = true) }
        } else emptyList()
    }
}