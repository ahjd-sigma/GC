package ahjd.geekedCraft.commands

import org.bukkit.command.CommandSender

interface SubCommand {
    val name: String
    val description: String
    val permission: String? get() = null  // Optional permission
    fun execute(sender: CommandSender, args: Array<String>)
    fun tabComplete(sender: CommandSender, args: Array<String>): List<String> = emptyList()
}