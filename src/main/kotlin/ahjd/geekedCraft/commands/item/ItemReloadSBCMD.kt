package ahjd.geekedCraft.commands.item

import ahjd.geekedCraft.commands.SubCommand
import ahjd.geekedCraft.item.ItemManager
import org.bukkit.command.CommandSender

class ItemReloadSBCMD : SubCommand {
    override val name = "reload"
    override val description = "Reloads the item module to match items.yml"
    override val permission = "geekedcraft.item.reload"

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage("§cYou do not have permission to use this command.")
            return
        }

        ItemManager.reload()
        sender.sendMessage("§aItem templates reloaded from items.yml!")
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return if (args.size == 1) listOf("reload") else emptyList()
    }
}
