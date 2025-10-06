package ahjd.geekedCraft.commands.mob

import ahjd.geekedCraft.commands.SubCommand
import ahjd.geekedCraft.mob.MobManager
import ahjd.geekedCraft.mob.stats.MobStats
import ahjd.geekedCraft.mob.stats.MobEnums
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MobSpawnSBCMD : SubCommand {
    override val name = "spawn"
    override val description = "Spawns a default mob"
    override val permission = "geekedcraft.mob.spawn"

    override fun execute(sender: CommandSender, args: Array<String>) {
        val player = sender as? Player ?: run {
            sender.sendMessage("§cOnly players can run this command.")
            return
        }

        val name = args.getOrNull(0) ?: "Mob" // Optional custom name

        val mobUUID = "corrupt?"
        val mobStats = MobStats(
            uuid = mobUUID,
            name = name,
            mobKind = MobEnums.Kind.ZOMBIE,
            mobType = MobEnums.Type.NORMAL,
            aiType = MobEnums.AI.STATIONARY,
            spawnPoint = player.location,
            protectionRange = 15.0,
            meleeRaw = 3,
            neutralRaw = 2
        )
        MobManager.spawnMob(mobStats, player.location)
        player.sendMessage("§aSpawned a default mob '${mobStats.name}' at your location!")
    }

    // No tab completion needed since there’s only an optional name
    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> = emptyList()
}
