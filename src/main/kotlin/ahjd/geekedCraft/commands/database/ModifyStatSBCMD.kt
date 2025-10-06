package ahjd.geekedCraft.commands.database

import ahjd.geekedCraft.commands.SubCommand
import ahjd.geekedCraft.database.managers.PlayerManager
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ModifyStatSBCMD : SubCommand {
    override val name = "modify"
    override val description = "Modifies a player's stats"
    override val permission = "geekedcraft.db.modify"

    override fun execute(sender: CommandSender, args: Array<String>) {
        val player = sender as? Player ?: run {
            sender.sendMessage("Only players can execute this command.")
            return
        }

        if (args.size < 2) {
            player.sendMessage("Usage: /db modify <player> <stat> <amount>")
            return
        }

        val targetPlayer = Bukkit.getPlayerExact(args[0]) ?: run {
            player.sendMessage("Player not found or not online.")
            return
        }

        val targetObj = PlayerManager.getPlayer(targetPlayer.uniqueId.toString())
        val stat = args[1].lowercase()
        val mm = MiniMessage.miniMessage()
        val amount = args.getOrNull(2)?.toIntOrNull() ?: run {
            player.sendMessage(mm.deserialize("<red>Invalid amount. Please enter a number.</red>"))
            return
        }

        when (stat) {
            "jumps" -> targetObj.jumps == amount
            "deaths" -> targetObj.deaths == amount
            "logins" -> targetObj.logins == amount
            else -> {
                player.sendMessage("Unknown stat: $stat")
                return
            }
        }

        player.sendMessage("Modified $stat for ${targetObj.name} by $amount.")
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> Bukkit.getOnlinePlayers().map { it.name } // suggest online players
            2 -> listOf("jumps", "deaths", "logins")      // suggest stat names
            else -> emptyList()
        }.filter { it.startsWith(args.last(), ignoreCase = true) }
    }
}
