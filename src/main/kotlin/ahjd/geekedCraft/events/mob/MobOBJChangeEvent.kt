package ahjd.geekedCraft.events.mob

import ahjd.geekedCraft.mob.stats.MobStats
import ahjd.geekedCraft.mob.stats.ChangeType
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class MobOBJChangeEvent(
    val mobOBJ: MobStats,
    val changeType: ChangeType  // Single enum, not a set
) : Event() {

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }

    override fun getHandlers(): HandlerList = HANDLERS
}