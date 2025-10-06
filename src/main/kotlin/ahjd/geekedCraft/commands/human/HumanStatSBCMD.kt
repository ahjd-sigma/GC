package ahjd.geekedCraft.commands.human

import ahjd.geekedCraft.commands.SubCommand
import ahjd.geekedCraft.human.HumanStatManager
import ahjd.geekedCraft.human.HumanStats
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.reflect.full.memberProperties

class HumanStatSBCMD : SubCommand {
    override val name = "stats"
    override val description = "Manages a human's stats"
    override val permission = "geekedcraft.hm.stats"

    // Cache stat names for performance (computed once)
    private val allStats: List<String> by lazy {
        HumanStats::class.memberProperties
            .filter { it.name != "uuid" && it.returnType.classifier == Int::class }
            .map { it.name }
    }

    override fun execute(sender: CommandSender, args: Array<String>) {
        val player = sender as? Player ?: run {
            sender.sendMessage("§cOnly players can run this command.")
            return
        }

        // Determine target player
        val (targetPlayer, statArgs) = if (args.isNotEmpty() && args[0].equals("player", ignoreCase = true)) {
            if (args.size < 2) {
                player.sendMessage("§cUsage: /human stats player <player> [stat] [value]")
                return
            }
            val target = Bukkit.getPlayer(args[1])
            if (target == null) {
                player.sendMessage("§cPlayer '${args[1]}' not found.")
                return
            }
            target to args.drop(2).toTypedArray()
        } else {
            player to args
        }

        val stats = HumanStatManager.get(targetPlayer.uniqueId.toString())
        val isOtherPlayer = targetPlayer != player

        if (statArgs.isEmpty()) {
            // Show current stats
            val header = if (isOtherPlayer) "§6=== ${targetPlayer.name}'s Stats ===" else "§6=== Your Stats ==="
            player.sendMessage(header)
            stats.getAllStats().forEach { (name, value) ->
                player.sendMessage("§e$name: §f$value")
            }
            return
        }

        if (statArgs.size >= 2) {
            val statName = statArgs[0]
            val inputValue = statArgs[1].toIntOrNull()

            if (inputValue == null) {
                player.sendMessage("§cValue must be a number.")
                return
            }

            if (stats.setBaseValue(statName, inputValue)) {
                val actualValue = stats.getValue(statName)

                val targetName = if (isOtherPlayer) "${targetPlayer.name}'s" else "your"
                if (actualValue != inputValue) {
                    player.sendMessage("§aSet $targetName $statName to $actualValue (clamped from $inputValue).")
                } else {
                    player.sendMessage("§aSet $targetName $statName to $actualValue.")
                }

                // Notify target player if they're not the sender
                if (isOtherPlayer) {
                    targetPlayer.sendMessage("§e${player.name} modified your $statName to $actualValue.")
                }
            } else {
                player.sendMessage("§cInvalid stat. Valid stats: ${allStats.joinToString(", ")}")
            }
        } else {
            val usage = if (isOtherPlayer) {
                "§cUsage: /human stats player ${targetPlayer.name} <stat> <value>"
            } else {
                "§cUsage: /human stats <stat> <value>"
            }
            player.sendMessage(usage)
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> {
                val suggestions = allStats.filter { it.startsWith(args[0], ignoreCase = true) }.toMutableList()
                if ("player".startsWith(args[0], ignoreCase = true)) {
                    suggestions.add(0, "player")
                }
                suggestions
            }
            2 -> {
                if (args[0].equals("player", ignoreCase = true)) {
                    // Suggest online player names
                    Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[1], ignoreCase = true) }
                } else {
                    emptyList()
                }
            }
            3 -> {
                if (args[0].equals("player", ignoreCase = true)) {
                    // Suggest stats after player name
                    allStats.filter { it.startsWith(args[2], ignoreCase = true) }
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }
}