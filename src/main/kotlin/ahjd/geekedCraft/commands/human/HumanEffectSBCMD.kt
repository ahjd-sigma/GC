package ahjd.geekedCraft.commands.human

import ahjd.geekedCraft.commands.SubCommand
import ahjd.geekedCraft.effect.effects.PoisonEffect
import ahjd.geekedCraft.effect.effects.RegenerationEffect
import ahjd.geekedCraft.effect.effects.SpeedBoostEffect
import ahjd.geekedCraft.effect.effects.StrengthEffect
import ahjd.geekedCraft.human.HumanStatManager
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class HumanEffectSBCMD : SubCommand {
    override val name = "effects"
    override val description = "Manages a human's effects"
    override val permission = "geekedcraft.hm.effects"

    override fun execute(sender: CommandSender, args: Array<String>) {

        if (args.isEmpty()) {
            sendUsage(sender)
            return
        }

        when (args[0].lowercase()) {
            "add" -> handleAdd(sender, args)
            "remove" -> handleRemove(sender, args)
            "list" -> handleList(sender, args)
            "clear" -> handleClear(sender, args)
            else -> sendUsage(sender)
        }
    }

    private fun handleAdd(sender: CommandSender, args: Array<String>) {
        // args = ["add", "player", "effectType", "duration", arg1, arg2...]
        if (args.size < 4) {
            sender.sendMessage("§cUsage: /hm effects add <player> <effectType> <duration> [args...]")
            sender.sendMessage("§7Available effects: poison, regen, speed, strength, bleeding, fire, slowheal, manaregen")
            return
        }

        val targetPlayer = Bukkit.getPlayer(args[1]) ?: run {
            sender.sendMessage("§cPlayer '${args[1]}' not found!")
            return
        }

        val stats = HumanStatManager.get(targetPlayer.uniqueId.toString())

        val effectType = args[2].lowercase()
        val durationTicks = args[3].toLongOrNull() ?: run {
            sender.sendMessage("§cInvalid duration: ${args[3]}")
            return
        }

        val effect = when (effectType) {
            "poison" -> {
                val damagePerSecond = args.getOrNull(4)?.toIntOrNull() ?: 5
                PoisonEffect(durationTicks, damagePerSecond)
            }
            "regen", "regeneration" -> {
                val healPerSecond = args.getOrNull(4)?.toIntOrNull() ?: 2
                RegenerationEffect(durationTicks, healPerSecond)
            }
            "speed" -> {
                val speedBonus = args.getOrNull(4)?.toIntOrNull() ?: 50
                SpeedBoostEffect(durationTicks, speedBonus)
            }
            "strength" -> {
                val damageBonus = args.getOrNull(4)?.toIntOrNull() ?: 20
                StrengthEffect(durationTicks, damageBonus)
            }
            else -> {
                sender.sendMessage("§cUnknown effect type: $effectType")
                sender.sendMessage("§7Available: poison, regen, speed, strength")
                return
            }
        }

        stats.applyEffect(effect)
        val durationSeconds = durationTicks / 20.0
        sender.sendMessage("§aApplied ${effect.displayName} §ato ${targetPlayer.name} for ${durationSeconds}s")
        targetPlayer.sendMessage("§7You have been afflicted with ${effect.displayName}")
    }

    private fun handleRemove(sender: CommandSender, args: Array<String>) {
        // args = ["remove", "player", "effectId"]
        if (args.size < 3) {
            sender.sendMessage("§cUsage: /hm effects remove <player> <effectId>")
            return
        }

        val targetPlayer = Bukkit.getPlayer(args[1]) ?: run {
            sender.sendMessage("§cPlayer '${args[1]}' not found!")
            return
        }

        val stats = HumanStatManager.get(targetPlayer.uniqueId.toString())

        val effectId = args[2].lowercase()
        val effectToRemove = stats.getActiveEffects().find { it.id == effectId }

        if (effectToRemove == null) {
            sender.sendMessage("§c${targetPlayer.name} does not have effect: $effectId")
            return
        }

        stats.removeEffect(effectToRemove)
        sender.sendMessage("§aRemoved ${effectToRemove.displayName} §afrom ${targetPlayer.name}")
        targetPlayer.sendMessage("§7${effectToRemove.displayName} §7has been removed")
    }

    private fun handleList(sender: CommandSender, args: Array<String>) {
        // args = ["list"] or ["list", "player"]
        val targetPlayer = if (args.size >= 2) {
            Bukkit.getPlayer(args[1]) ?: run {
                sender.sendMessage("§cPlayer '${args[1]}' not found!")
                return
            }
        } else {
            sender as? Player ?: run {
                sender.sendMessage("§cPlease specify a player: /hm effects list <player>")
                return
            }
        }

        val stats = HumanStatManager.get(targetPlayer.uniqueId.toString())

        val activeEffects = stats.getActiveEffects()

        if (activeEffects.isEmpty()) {
            sender.sendMessage("§e${targetPlayer.name} has no active effects")
            return
        }

        sender.sendMessage("§6=== Active Effects for ${targetPlayer.name} ===")
        activeEffects.forEach { effect ->
            val durationStr = if (effect.durationTicks != null) {
                "${effect.durationTicks!! / 20.0}s"
            } else {
                "Permanent"
            }
            val intervalStr = if (effect.periodic) {
                " (every ${effect.tickInterval / 20.0}s)"
            } else {
                ""
            }
            sender.sendMessage("§7- ${effect.displayName} §8[§7${effect.id}§8] §7- $durationStr$intervalStr")
            sender.sendMessage("  §8${effect.description}")
        }
    }

    private fun handleClear(sender: CommandSender, args: Array<String>) {
        // args = ["clear", "player"]
        if (args.size < 2) {
            sender.sendMessage("§cUsage: /hm effects clear <player>")
            return
        }

        val targetPlayer = Bukkit.getPlayer(args[1]) ?: run {
            sender.sendMessage("§cPlayer '${args[1]}' not found!")
            return
        }

        val stats = HumanStatManager.get(targetPlayer.uniqueId.toString())

        val effectCount = stats.getActiveEffects().size

        if (effectCount == 0) {
            sender.sendMessage("§e${targetPlayer.name} has no active effects to clear")
            return
        }

        // Remove all effects
        stats.getActiveEffects().toList().forEach { effect ->
            stats.removeEffect(effect)
        }

        sender.sendMessage("§aCleared $effectCount effect(s) from ${targetPlayer.name}")
        targetPlayer.sendMessage("§7All your effects have been cleared")
    }

    private fun sendUsage(sender: CommandSender) {
        sender.sendMessage("§6=== Human Effects Commands ===")
        sender.sendMessage("§e/hm effects add <player> <type> <ticks> [args] §7- Apply an effect")
        sender.sendMessage("§e/hm effects remove <player> <effectId> §7- Remove a specific effect")
        sender.sendMessage("§e/hm effects list [player] §7- List active effects")
        sender.sendMessage("§e/hm effects clear <player> §7- Clear all effects")
        sender.sendMessage("")
        sender.sendMessage("§7Effect Types:")
        sender.sendMessage("§8- §epoison <damage/sec> §7- Poison damage over time")
        sender.sendMessage("§8- §eregen <heal/sec> §7- Heal over time")
        sender.sendMessage("§8- §espeed <bonus> §7- Speed boost")
        sender.sendMessage("§8- §estrength <bonus> §7- Damage boost")
        sender.sendMessage("")
        sender.sendMessage("§7Note: 20 ticks = 1 second")
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {

        return when (args.size) {
            // /hm effects <TAB> or /hm effects a<TAB>
            1 -> listOf("add", "remove", "list", "clear")
                .filter { it.startsWith(args[0].lowercase()) }

            // /hm effects add <TAB> or /hm effects add P<TAB>
            2 -> {
                Bukkit.getOnlinePlayers()
                    .map { it.name }
                    .filter { it.lowercase().startsWith(args[1].lowercase()) }
            }

            // /hm effects add Player <TAB>
            3 -> {
                when (args[0].lowercase()) {
                    "add" -> listOf("poison", "regen", "speed", "strength")
                        .filter { it.startsWith(args[2].lowercase()) }
                    "remove" -> {
                        // Get target player's active effects
                        val targetPlayer = Bukkit.getPlayer(args[1])
                        val stats = targetPlayer?.let { HumanStatManager.get(it.uniqueId.toString()) }
                        stats?.getActiveEffects()
                            ?.map { it.id }
                            ?.filter { it.startsWith(args[2].lowercase()) }
                            ?: emptyList()
                    }
                    else -> emptyList()
                }
            }

            // /hm effects add Player poison <TAB>
            4 -> {
                if (args[0].lowercase() == "add") {
                    listOf("20", "40", "60", "100", "200", "400") // Common durations in ticks
                } else {
                    emptyList()
                }
            }

            // /hm effects add Player poison 200 <TAB>
            5 -> {
                if (args[0].lowercase() == "add") {
                    when (args[2].lowercase()) {
                        "poison"-> listOf("5", "10", "15")
                        "regen" -> listOf("5", "10", "20")
                        "speed" -> listOf("25", "50", "100")
                        "strength" -> listOf("10", "20", "50")
                        else -> emptyList()
                    }
                } else {
                    emptyList()
                }
            }

            else -> emptyList()
        }
    }
}