package ahjd.geekedCraft.hologram.cmd

import ahjd.geekedCraft.database.managers.HologramManager
import ahjd.geekedCraft.hologram.HologramController
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class HologramNavigator {
    fun list(sender: CommandSender) {
        val holograms = HologramManager.getAllHolograms()
        if (holograms.isEmpty()) {
            sender.sendMessage("§eNo holograms found!")
            return
        }

        sender.sendMessage("§6=== Holograms (${holograms.size}) ===")
        holograms.forEach { (uuid, hologram) ->
            val loc = hologram.location
            val locStr = if (loc != null) {
                "§7(${loc.world?.name}, ${loc.blockX}, ${loc.blockY}, ${loc.blockZ})"
            } else "§7(no location)"
            val spawned = if (HologramController.isSpawned(hologram)) "§a✓" else "§c✗"
            sender.sendMessage("§e${hologram.name} $spawned §8${uuid.toString().substring(0, 8)}... $locStr")
        }
    }

    fun teleport(sender: CommandSender, args: Array<String>) {
        if (sender !is Player) {
            sender.sendMessage("§cOnly players can teleport!")
            return
        }
        if (args.size < 2) {
            sender.sendMessage("§cUsage: /db hologram tp <name>")
            return
        }

        val name = args[1]
        val hologram = HologramManager.getHologramByName(name)
        if (hologram == null) {
            sender.sendMessage("§cHologram '$name' does not exist!")
            return
        }
        if (hologram.location == null) {
            sender.sendMessage("§cHologram '${hologram.name}' has no location set!")
            return
        }

        sender.teleport(hologram.location!!)
        sender.sendMessage("§aTeleported to hologram '${hologram.name}'!")
    }
}