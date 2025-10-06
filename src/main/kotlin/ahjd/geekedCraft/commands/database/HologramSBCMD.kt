package ahjd.geekedCraft.commands.database

import ahjd.geekedCraft.commands.SubCommand
import ahjd.geekedCraft.database.managers.HologramManager
import ahjd.geekedCraft.hologram.cmd.HologramCreator
import ahjd.geekedCraft.hologram.cmd.HologramEditor
import ahjd.geekedCraft.hologram.cmd.HologramNavigator
import ahjd.geekedCraft.hologram.cmd.HologramSpawner
import org.bukkit.command.CommandSender

class HologramSBCMD : SubCommand {
    override val name = "hologram"
    override val description = "Manage holograms"
    override val permission = "geekedcraft.db.hologram"

    private val creator = HologramCreator()
    private val editor = HologramEditor()
    private val spawner = HologramSpawner()
    private val navigator = HologramNavigator()

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            sender.sendMessage("§cUsage: /db hologram <create|delete|edit|list|spawn|move|despawn|tp>")
            return
        }

        when (args[0].lowercase()) {
            "create" -> creator.create(sender, args)
            "delete" -> creator.delete(sender, args)
            "list" -> navigator.list(sender)
            "tp" -> navigator.teleport(sender, args)
            "edit" -> editor.edit(sender, args)
            "spawn" -> spawner.spawn(sender, args)
            "move" -> spawner.move(sender, args)
            "despawn" -> spawner.despawn(sender, args)
            else -> sender.sendMessage("§cUnknown subcommand: ${args[0]}")
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> listOf("create", "delete", "edit", "list", "spawn", "move", "despawn", "tp")
                .filter { it.startsWith(args[0].lowercase()) }
            else -> when (args[0].lowercase()) {
                "create" -> if (args.size == 2) listOf("<n>") else emptyList()
                "delete", "spawn", "move", "tp", "edit" -> if (args.size == 2)
                    HologramManager.getAllHolograms().values.map { it.name }
                        .filter { it.startsWith(args[1], ignoreCase = true) }
                else if (args[0].lowercase() == "edit") editor.tabComplete(args)
                else emptyList()
                "despawn" -> spawner.tabCompleteDespawn(args)
                else -> emptyList()
            }
        }
    }
}