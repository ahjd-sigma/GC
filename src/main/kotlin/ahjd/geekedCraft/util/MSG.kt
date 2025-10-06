package ahjd.geekedCraft.util

import ahjd.geekedCraft.main.GeekedCraft
import org.bukkit.entity.Player

object MSG {
    private val logger = GeekedCraft.getInstance().logger
    private const val PREFIX = "§7[§6GeekedCraft§7]§e "

    fun info(msg: String) {
        logger.info(msg)
    }

    fun warn(msg: String) {
        logger.warning(msg)
    }

    fun tell(player: Any, msg: String){
        if (player is Player) {
            player.sendMessage(PREFIX + "§c${msg}")
        }else{
            error("$PREFIX$msg This was run by a non player")
        }
    }

    fun error(msg: String) {
        logger.severe(msg)
    }
}