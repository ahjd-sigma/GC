package ahjd.geekedCraft.commands.mob

import ahjd.geekedCraft.commands.SubCommand
import ahjd.geekedCraft.events.mob.MobOBJChangeEvent
import ahjd.geekedCraft.mob.MobManager
import ahjd.geekedCraft.mob.stats.ChangeType
import ahjd.geekedCraft.mob.stats.MobEnums
import ahjd.geekedCraft.mob.stats.MobStats
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.reflect.full.memberProperties

class MobStatSBCMD : SubCommand {
    override val name = "stats"
    override val description = "Modifies a mob's stats"
    override val permission = "geekedcraft.mob.stats"

    // Dynamically get all numeric stats from MobStats class
    private val numericStats: List<String> by lazy {
        MobStats::class.memberProperties
            .filter { it.returnType.classifier == Int::class }
            .map { it.name }
    }

    private val enumStats = listOf("mobKind", "mobType", "aiType")
    private val specialStats = listOf("iframes") // Long type stats

    private val allStats: List<String> by lazy {
        numericStats + enumStats + specialStats
    }

    override fun execute(sender: CommandSender, args: Array<String>) {
        val player = sender as? Player ?: run {
            sender.sendMessage("§cOnly players can run this command.")
            return
        }

        val mobUUID = getTargetMob(player)
        if (mobUUID == null) {
            player.sendMessage("§cNo mob in sight.")
            return
        }

        if (!MobManager.has(mobUUID)) {
            player.sendMessage("§cThis mob has no StatOBJ (Normal/Untracked mob).")
            return
        }

        val mobStats = MobManager.get(mobUUID)

        if (args.isEmpty()) {
            // Show current stats using the toMap() method
            player.sendMessage("§6=== Mob Stats ===")
            mobStats.toMap().forEach { (name, value) ->
                player.sendMessage("§e$name: §f$value")
            }
            return
        }

        if (args.size >= 2) {
            val statName = args[0]
            val newValue = args[1]

            when {
                statName == "iframes" -> {
                    val value = newValue.toLongOrNull()
                    if (value == null || value < 0) {
                        player.sendMessage("§cValue must be a non-negative number (milliseconds).")
                        return
                    }
                    mobStats.iframes = value
                    player.sendMessage("§aSet iframes to ${value}ms for ${mobStats.name}.")
                }

                numericStats.contains(statName) -> {
                    val value = newValue.toIntOrNull()
                    if (value == null) {
                        player.sendMessage("§cValue must be a number.")
                        return
                    }
                    if (mobStats.setValue(statName, value)) {
                        player.sendMessage("§aSet $statName to $value for ${mobStats.name}.")
                    } else {
                        player.sendMessage("§cFailed to set $statName.")
                    }
                }

                enumStats.contains(statName) -> {
                    val enumValue = when (statName) {
                        "mobKind" -> MobEnums.Kind.entries.firstOrNull { it.name.equals(newValue, true) }
                        "mobType" -> MobEnums.Type.entries.firstOrNull { it.name.equals(newValue, true) }
                        "aiType" -> MobEnums.AI.entries.firstOrNull { it.name.equals(newValue, true) }
                        else -> null
                    }

                    if (enumValue != null) {
                        when (statName) {
                            "mobKind" -> {
                                mobStats.mobKind = enumValue as MobEnums.Kind
                                Bukkit.getPluginManager().callEvent(MobOBJChangeEvent(mobStats, ChangeType.KIND))
                            }
                            "mobType" -> mobStats.mobType = enumValue as MobEnums.Type
                            "aiType" -> {
                                mobStats.aiType = enumValue as MobEnums.AI
                                Bukkit.getPluginManager().callEvent(MobOBJChangeEvent(mobStats, ChangeType.AI_PERSONALITY))
                            }
                        }
                        player.sendMessage("§aSet $statName to $newValue for ${mobStats.name}.")
                    } else {
                        val validOptions = when (statName) {
                            "mobKind" -> MobEnums.Kind.entries.joinToString(", ") { it.name }
                            "mobType" -> MobEnums.Type.entries.joinToString(", ") { it.name }
                            "aiType" -> MobEnums.AI.entries.joinToString(", ") { it.name }
                            else -> ""
                        }
                        player.sendMessage("§cInvalid value. Valid options: $validOptions")
                    }
                }

                else -> {
                    player.sendMessage("§cInvalid stat. Valid stats: ${allStats.joinToString(", ")}")
                }
            }
        } else {
            player.sendMessage("§cUsage: /mob stats <stat> <value>")
        }
    }

    private fun getTargetMob(player: Player, range: Int = 20) =
        player.getTargetEntity(range)?.uniqueId?.toString()

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> allStats.filter { it.startsWith(args[0], true) }
            2 -> when (args[0]) {
                "mobKind" -> MobEnums.Kind.entries.map { it.name }.filter { it.startsWith(args[1], true) }
                "mobType" -> MobEnums.Type.entries.map { it.name }.filter { it.startsWith(args[1], true) }
                "aiType" -> MobEnums.AI.entries.map { it.name }.filter { it.startsWith(args[1], true) }
                "iframes" -> listOf("0", "100", "500", "1000", "2000")
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}