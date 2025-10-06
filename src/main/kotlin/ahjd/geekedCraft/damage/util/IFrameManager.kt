package ahjd.geekedCraft.damage.util

import ahjd.geekedCraft.mob.MobManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

object IFrameManager {

    private const val HUMAN_IFRAME_MS = 500L

    private val lastHit = mutableMapOf<LivingEntity, Long>()

    fun canTakeDamage(entity: LivingEntity): Boolean {
        // check if entity is valid for the damage system
        if (entity !is Player && !MobManager.has(entity.uniqueId.toString())) {
            return false // Not a valid damage target
        }

        val now = System.currentTimeMillis()
        val (lastHitTime, iframeMs) = when (entity) {
            is Player -> (lastHit[entity] ?: 0L) to HUMAN_IFRAME_MS
            else -> {
                val mobStats = MobManager.get(entity.uniqueId.toString())
                (lastHit[entity] ?: 0L) to mobStats.iframes
            }
        }

        val timeSinceHit = now - lastHitTime

        if (timeSinceHit > iframeMs * 2) {
            lastHit.remove(entity)
        }

        return timeSinceHit >= iframeMs
    }

    fun startIFrame(entity: LivingEntity) {
        lastHit[entity] = System.currentTimeMillis()
    }
}