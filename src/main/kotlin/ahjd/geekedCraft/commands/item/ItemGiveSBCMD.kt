package ahjd.geekedCraft.commands.item

import ahjd.geekedCraft.commands.SubCommand
import ahjd.geekedCraft.item.ItemManager
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class ItemGiveSBCMD : SubCommand {
    override val name = "give"
    override val description = "Gives Items from items.yml"
    override val permission = "geekedcraft.item.give"

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage("§cYou don’t have permission to use this command.")
            return
        }

        if (args.size < 2) {
            sender.sendMessage("§cUsage: /item give <player> <itemId> [amount]")
            return
        }

        val target = Bukkit.getPlayer(args[0])
        if (target == null) {
            sender.sendMessage("§cPlayer '${args[0]}' not found.")
            return
        }

        val itemId = args[1]
        val amount = args.getOrNull(2)?.toIntOrNull() ?: 1

        val item = ItemManager.createItem(itemId, amount)
        if (item == null) {
            sender.sendMessage("§cItem template '$itemId' not found in items.yml.")
            return
        }

        ItemManager.giveItem(target, item)
        sender.sendMessage("§aGave $amount x $itemId to ${target.name}.")
        if (sender != target) {
            target.sendMessage("§aYou received $amount x $itemId.")
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return when (args.size) {
            1 -> Bukkit.getOnlinePlayers().map { it.name } // suggest players
            2 -> ItemManager.getTemplateIds()              // suggest item IDs
            else -> emptyList()
        }
    }
}
