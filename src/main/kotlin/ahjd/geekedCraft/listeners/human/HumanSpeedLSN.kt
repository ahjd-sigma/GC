package ahjd.geekedCraft.listeners.human

import ahjd.geekedCraft.util.SpeedNormalizer
import org.bukkit.entity.Player

object HumanSpeedLSN {

    fun onHumanSpeedChange(player: Player, newSpeed: Int) {
        player.walkSpeed = SpeedNormalizer.normalize(newSpeed)
    }
}