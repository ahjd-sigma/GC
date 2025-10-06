package ahjd.geekedCraft.commands.database

import ahjd.geekedCraft.commands.SubCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class DBCMD : CommandExecutor, TabCompleter {

    private val subCommands = mutableMapOf<String, SubCommand>()

    init {
        registerSubCommand(StatsSBCMD())
        registerSubCommand(ModifyStatSBCMD())
        registerSubCommand(HologramSBCMD())
    }

    private fun registerSubCommand(sub: SubCommand) {
        subCommands[sub.name.lowercase()] = sub
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("Available subcommands:")
            subCommands.values
                .filter { s ->
                    val perm = s.permission
                    perm == null || sender.hasPermission(perm)
                }
                .forEach { s -> sender.sendMessage("${s.name} - ${s.description}") }
            return true
        }

        val sub = subCommands[args[0].lowercase()]
        if (sub == null) {
            sender.sendMessage("Unknown subcommand. Available:")
            subCommands.values
                .filter { s ->
                    val perm = s.permission
                    perm == null || sender.hasPermission(perm)
                }
                .forEach { s -> sender.sendMessage("${s.name} - ${s.description}") }
            return true
        }

        val perm = sub.permission
        if (perm != null && !sender.hasPermission(perm)) {
            sender.sendMessage("You do not have permission to use this subcommand.")
            return true
        }

        sub.execute(sender, args.drop(1).toTypedArray())
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String>? {
        return try {
            when {
                args.isEmpty() -> subCommands.values
                    .filter { s ->
                        val perm = s.permission
                        perm == null || sender.hasPermission(perm)
                    }
                    .map { it.name }
                args.size == 1 -> subCommands.values
                    .filter { s ->
                        val perm = s.permission
                        perm == null || sender.hasPermission(perm)
                    }
                    .map { it.name }
                    .filter { it.startsWith(args[0], ignoreCase = true) }
                else -> {
                    val sub = subCommands[args[0].lowercase()] ?: return emptyList()
                    val perm = sub.permission
                    if (perm == null || sender.hasPermission(perm)) {
                        sub.tabComplete(sender, args.drop(1).toTypedArray())
                    } else emptyList()
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}