package ahjd.geekedCraft.mob

import ahjd.geekedCraft.events.mob.MobSpeedChangeEvent
import ahjd.geekedCraft.mob.ai.MobAI
import ahjd.geekedCraft.mob.stats.MobEnums
import ahjd.geekedCraft.mob.stats.MobStats
import ahjd.geekedCraft.mob.util.MobHealthDisplay
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import java.util.*

object MobManager {
    private val stats = mutableMapOf<String, MobStats>()

    fun get(uuid: String) = stats[uuid] ?: error("No MobStats for $uuid")
    fun has(uuid: String) = uuid in stats
    fun getAllMobs() = stats.values
    fun remove(uuid: String) = stats.remove(uuid)

    fun clear() {
        stats.values.forEach { (Bukkit.getEntity(UUID.fromString(it.uuid)) as? LivingEntity)?.remove() }
        stats.clear()
    }

    private fun spawn(m: MobStats, loc: Location, setup: MobStats.() -> Unit = {}): LivingEntity {
        m.setup()
        val type = when (m.mobKind) {
            MobEnums.Kind.ZOMBIE -> EntityType.ZOMBIE
            MobEnums.Kind.SKELETON -> EntityType.SKELETON
            MobEnums.Kind.CREEPER -> EntityType.CREEPER
        }

        return (loc.world.spawnEntity(loc, type) as LivingEntity).apply {
            m.uuid = uniqueId.toString()
            m.spawnPoint = loc
            stats[m.uuid] = m

            MobHealthDisplay.updateHealthDisplay(this, m)
            Bukkit.getPluginManager().callEvent(MobSpeedChangeEvent(this, m.speed))
            MobAI.mobAIInjection(this as Mob, m.aiType)

            isPersistent = true
            removeWhenFarAway = false
            canPickupItems = false
            if (m.isDummy) setGravity(false)
        }
    }

    fun spawnMob(m: MobStats, loc: Location) = spawn(m, loc)

    fun spawnDummy(m: MobStats, loc: Location) = spawn(m, loc) {
        isDummy = true
        health = Int.MAX_VALUE
        maxhealth = Int.MAX_VALUE
        iframes = 0L
        knockback = 0
        knockbackResistance = 100
        speed = 0
        healthregen = 0
        aiType = MobEnums.AI.STATIONARY
    }
}