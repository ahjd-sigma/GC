package ahjd.geekedCraft.hologram.cmd

import ahjd.geekedCraft.database.managers.HologramManager
import ahjd.geekedCraft.hologram.HologramController
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Display

class HologramEditor {
    fun edit(sender: CommandSender, args: Array<String>) {
        if (args.size < 4) {
            sender.sendMessage("§cUsage: /db hologram edit <name> <property> <value...>")
            sender.sendMessage("§eProperties: text, shadowed, seethrough, linewidth, viewrange, billboard, opacity, scale")
            return
        }

        val name = args[1]
        val hologram = HologramManager.getHologramByName(name)
        if (hologram == null) {
            sender.sendMessage("§cHologram '$name' does not exist!")
            return
        }

        val property = args[2].lowercase()
        val value = args.drop(3).joinToString(" ")
        val wasSpawned = HologramController.isSpawned(hologram)

        try {
            when (property) {
                "text" -> {
                    hologram.text = Component.text(value)
                    HologramManager.saveHologram(hologram)
                    if (wasSpawned) {
                        HologramController.updateEntity(hologram) { it.text(Component.text(value)) }
                    }
                    sender.sendMessage("§aUpdated text for '${hologram.name}'!")
                }
                "shadowed" -> {
                    val boolValue = value.toBoolean()
                    hologram.shadowed = boolValue
                    HologramManager.saveHologram(hologram)
                    if (wasSpawned) {
                        HologramController.updateEntity(hologram) { it.isShadowed = boolValue }
                    }
                    sender.sendMessage("§aUpdated shadowed to $value for '${hologram.name}'!")
                }
                "seethrough" -> {
                    val boolValue = value.toBoolean()
                    hologram.seeThrough = boolValue
                    HologramManager.saveHologram(hologram)
                    if (wasSpawned) {
                        HologramController.updateEntity(hologram) { it.isSeeThrough = boolValue }
                    }
                    sender.sendMessage("§aUpdated see-through to $value for '${hologram.name}'!")
                }
                "linewidth" -> {
                    val intValue = value.toInt()
                    hologram.lineWidth = intValue
                    HologramManager.saveHologram(hologram)
                    if (wasSpawned) {
                        HologramController.updateEntity(hologram) { it.lineWidth = intValue }
                    }
                    sender.sendMessage("§aUpdated line width to $value for '${hologram.name}'!")
                }
                "viewrange" -> {
                    val floatValue = value.toFloat()
                    hologram.viewRange = floatValue
                    HologramManager.saveHologram(hologram)
                    if (wasSpawned) {
                        HologramController.updateEntity(hologram) { it.viewRange = floatValue }
                    }
                    sender.sendMessage("§aUpdated view range to $value for '${hologram.name}'!")
                }
                "billboard" -> {
                    val billboard = Display.Billboard.valueOf(value.uppercase())
                    hologram.billboard = billboard
                    HologramManager.saveHologram(hologram)
                    if (wasSpawned) {
                        HologramController.updateEntity(hologram) { it.billboard = billboard }
                    }
                    sender.sendMessage("§aUpdated billboard to $value for '${hologram.name}'!")
                }
                "opacity" -> {
                    val intValue = value.toInt()
                    if (intValue < -1 || intValue > 255) {
                        sender.sendMessage("§cOpacity must be between -1 and 255!")
                        return
                    }
                    val byteValue = intValue.toByte()
                    hologram.textOpacity = byteValue
                    HologramManager.saveHologram(hologram)
                    if (wasSpawned) {
                        HologramController.updateEntity(hologram) { it.textOpacity = byteValue }
                    }
                    sender.sendMessage("§aUpdated opacity to $value for '${hologram.name}'!")
                }
                "scale" -> {
                    // Parse scale values: "1.5" for uniform or "1.5 2.0 1.0" for x y z
                    val parts = value.split(" ").mapNotNull { it.toFloatOrNull() }
                    val scale = when (parts.size) {
                        1 -> Triple(parts[0], parts[0], parts[0])
                        3 -> Triple(parts[0], parts[1], parts[2])
                        else -> {
                            sender.sendMessage("§cInvalid scale format! Use: <uniform> or <x> <y> <z>")
                            return
                        }
                    }

                    hologram.scale = scale
                    HologramManager.saveHologram(hologram)
                    if (wasSpawned) {
                        HologramController.updateEntity(hologram) { display ->
                            val transform = display.transformation
                            transform.scale.set(scale.first.toDouble(), scale.second.toDouble(), scale.third.toDouble())
                            display.transformation = transform
                        }
                    }
                    sender.sendMessage("§aUpdated scale to ${scale.first}, ${scale.second}, ${scale.third} for '${hologram.name}'!")
                }
                else -> sender.sendMessage("§cUnknown property: $property")
            }

            if (!wasSpawned) {
                sender.sendMessage("§7Note: Hologram is not spawned. Changes saved to database.")
            }
        } catch (e: IllegalArgumentException) {
            sender.sendMessage("§cInvalid value for property '$property': $value")
        } catch (e: Exception) {
            sender.sendMessage("§cError: ${e.message}")
        }
    }

    fun tabComplete(args: Array<String>): List<String> {
        return when (args.size) {
            2 -> HologramManager.getAllHolograms().values.map { it.name }
                .filter { it.startsWith(args[1], ignoreCase = true) }
            3 -> listOf("text", "shadowed", "seethrough", "linewidth", "viewrange", "billboard", "opacity", "scale")
                .filter { it.startsWith(args[2].lowercase()) }
            4 -> when {
                args[2].equals("billboard", true) -> listOf("FIXED", "VERTICAL", "HORIZONTAL", "CENTER")
                    .filter { it.startsWith(args[3].uppercase()) }
                args[2] in listOf("shadowed", "seethrough") -> listOf("true", "false")
                args[2].equals("opacity", true) -> listOf("-1", "0", "127", "255")
                args[2].equals("scale", true) -> listOf("1.0", "1.5", "2.0", "<x> <y> <z>")
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}