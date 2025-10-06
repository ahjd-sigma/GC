package ahjd.geekedCraft.hologram.cmd

import ahjd.geekedCraft.database.managers.HologramManager
import ahjd.geekedCraft.database.objects.HologramOBJ
import ahjd.geekedCraft.hologram.HologramController
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class HologramCreator {
    fun create(sender: CommandSender, args: Array<String>) {
        if (sender !is Player) {
            sender.sendMessage("§cOnly players can create holograms!")
            return
        }
        if (args.size < 3) {
            sender.sendMessage("§cUsage: /db hologram create <n> <text...>")
            return
        }

        val name = args[1]

        // Check if hologram with this name already exists
        if (HologramManager.getHologramByName(name) != null) {
            sender.sendMessage("§cA hologram with the name '$name' already exists!")
            return
        }

        val text = args.drop(2).joinToString(" ")

        // Create hologram with UUID, ready to be spawned
        val hologram = HologramOBJ(
            uuid = UUID.randomUUID(),
            name = name,
            text = Component.text(text),
            location = sender.location
        )

        HologramManager.saveHologram(hologram)
        sender.sendMessage("§aCreated hologram '$name'!")
        sender.sendMessage("§7UUID: ${hologram.uuid}")
    }

    fun delete(sender: CommandSender, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage("§cUsage: /db hologram delete <name>")
            return
        }

        val name = args[1]
        val hologram = HologramManager.getHologramByName(name)
        if (hologram == null) {
            sender.sendMessage("§cHologram '$name' does not exist!")
            return
        }

        HologramController.despawn(hologram)
        hologram.uuid?.let { HologramManager.deleteHologram(it) }
        sender.sendMessage("§aDeleted hologram '${hologram.name}'!")
    }
}