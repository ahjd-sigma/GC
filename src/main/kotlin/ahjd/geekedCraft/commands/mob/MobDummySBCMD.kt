package ahjd.geekedCraft.commands.mob

import ahjd.geekedCraft.commands.SubCommand
import ahjd.geekedCraft.item.itemstacks.DummyEgg
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MobDummySBCMD : SubCommand {
    override val name = "dummy"
    override val description = "Spawns a training dummy"
    override val permission = "geekedcraft.mob.dummy"

    override fun execute(sender: CommandSender, args: Array<String>) {
        val player = sender as? Player ?: run {
            sender.sendMessage("§cOnly players can run this command.")
            return
        }

        val egg = DummyEgg.createEgg()
        player.inventory.addItem(egg)
        player.sendMessage("§aReceived a Training Dummy Egg!")
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return if (args.size == 2) {
            listOf("<name>")
        } else {
            emptyList()
        }
    }
}