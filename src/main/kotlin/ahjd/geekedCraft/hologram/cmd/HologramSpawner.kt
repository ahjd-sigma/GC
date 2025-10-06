package ahjd.geekedCraft.hologram.cmd

import ahjd.geekedCraft.database.managers.HologramManager
import ahjd.geekedCraft.hologram.HologramController
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class HologramSpawner {
    fun spawn(sender: CommandSender, args: Array<String>) {
        if (sender !is Player) {
            sender.sendMessage("§cOnly players can spawn holograms!")
            return
        }
        if (args.size < 2) {
            sender.sendMessage("§cUsage: /db hologram spawn <n>")
            return
        }

        val name = args[1]
        val hologram = HologramManager.getHologramByName(name)
        if (hologram == null) {
            sender.sendMessage("§cHologram '$name' does not exist!")
            return
        }

        // Block if already spawned
        if (HologramController.isSpawned(hologram)) {
            sender.sendMessage("§cHologram '${hologram.name}' is already spawned!")
            sender.sendMessage("§eUse '/db hologram despawn ${hologram.name}' first if you want to respawn it.")
            return
        }

        val entity = HologramController.spawn(hologram)
        if (entity != null) {
            sender.sendMessage("§aSpawned hologram '${hologram.name}' (${entity.uniqueId})!")
        } else {
            sender.sendMessage("§cFailed to spawn hologram!")
        }
    }

    fun move(sender: CommandSender, args: Array<String>) {
        if (sender !is Player) {
            sender.sendMessage("§cOnly players can move holograms!")
            return
        }
        if (args.size < 2) {
            sender.sendMessage("§cUsage: /db hologram move <n>")
            return
        }

        val name = args[1]
        val hologram = HologramManager.getHologramByName(name)
        if (hologram == null) {
            sender.sendMessage("§cHologram '$name' does not exist!")
            return
        }

        val newLocation = sender.location
        val success = HologramController.move(hologram, newLocation)

        if (success) {
            sender.sendMessage("§aMoved hologram '${hologram.name}' to your location!")
        } else {
            sender.sendMessage("§eHologram moved in database, but entity is not spawned!")
        }
    }

    fun despawn(sender: CommandSender, args: Array<String>) {
        if (args.size < 2) {
            sender.sendMessage("§cUsage: /db hologram despawn <name|radius>")
            return
        }

        // Try name first
        val hologram = HologramManager.getHologramByName(args[1])
        if (hologram != null) {
            if (HologramController.despawn(hologram)) {
                sender.sendMessage("§aDespawned hologram '${hologram.name}'!")
            } else {
                sender.sendMessage("§eHologram '${hologram.name}' is not currently spawned!")
            }
            return
        }

        // Try radius
        if (sender !is Player) {
            sender.sendMessage("§cHologram not found and only players can despawn by radius!")
            return
        }

        val radius = args[1].toDoubleOrNull()
        if (radius == null || radius <= 0) {
            sender.sendMessage("§c'${args[1]}' is not a valid hologram name or radius!")
            return
        }

        val count = HologramController.despawnInRadius(sender.location, radius)
        sender.sendMessage("§aDespawned $count hologram entities within $radius blocks!")
    }

    fun tabCompleteDespawn(args: Array<String>): List<String> {
        return if (args.size == 2) {
            HologramManager.getAllHolograms().values.map { it.name }
                .filter { it.startsWith(args[1], ignoreCase = true) } + listOf("<radius>")
        } else emptyList()
    }
}