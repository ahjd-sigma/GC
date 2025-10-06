package ahjd.geekedCraft.mob.misc.dummy

import ahjd.geekedCraft.item.itemstacks.DummyEgg
import ahjd.geekedCraft.main.GeekedCraft
import ahjd.geekedCraft.mob.MobManager
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object DummyCombatTracker {
    private val sessions = ConcurrentHashMap<String, CombatSession>()
    private var tickerTaskId: Int? = null
    private const val COMBAT_TIMEOUT = 15000L
    private const val INACTIVITY_DESPAWN = 120000L

    fun start() {
        if (tickerTaskId != null) return
        tickerTaskId = GeekedCraft.getInstance().scheduleRepeatingTask({ tick() }, 0L, 20L)
    }

    fun stop() {
        tickerTaskId?.let { Bukkit.getScheduler().cancelTask(it) }
        tickerTaskId = null
    }

    private fun tick() {
        val now = System.currentTimeMillis()
        sessions.entries.removeAll { (uuid, s) ->
            val sinceSpawn = now - s.spawnTime
            val sinceDamage = now - s.lastDamageTime

            if ((s.lastDamageTime == 0L && sinceSpawn >= INACTIVITY_DESPAWN) ||
                (s.lastDamageTime > 0L && sinceDamage >= INACTIVITY_DESPAWN)) {
                despawnDummy(uuid)
                return@removeAll true
            }

            if (s.isInCombat && now - s.lastDamageTime >= COMBAT_TIMEOUT) {
                s.damageEntries.clear()
                s.isInCombat = false
                s.combatStartTime = 0L
            }
            false
        }
    }

    fun despawnDummy(dummyUuid: String): Boolean {
        val entity = Bukkit.getEntity(UUID.fromString(dummyUuid)) as? LivingEntity
        entity?.location?.world?.dropItemNaturally(entity.location, DummyEgg.createEgg())
        entity?.remove()
        MobManager.remove(dummyUuid)
        sessions.remove(dummyUuid)
        return entity != null
    }

    fun onDummyDamaged(dummyUuid: String, damage: Int) {
        val now = System.currentTimeMillis()
        val session = sessions.getOrPut(dummyUuid) { CombatSession() }

        if (!session.isInCombat) {
            session.isInCombat = true
            session.combatStartTime = now
        }

        session.lastDamageTime = now
        session.damageEntries.add(DamageEntry(damage, now))
    }

    fun getDPS(dummyUuid: String): Double {
        val s = sessions[dummyUuid] ?: return 0.0
        if (!s.isInCombat || s.damageEntries.isEmpty()) return 0.0

        val totalDmg = s.damageEntries.sumOf { it.damage }
        val duration = (System.currentTimeMillis() - s.combatStartTime) / 1000.0
        return if (duration > 0) totalDmg / duration else 0.0
    }

    fun getTotalDamage(dummyUuid: String) = sessions[dummyUuid]?.damageEntries?.sumOf { it.damage } ?: 0

    fun getDamageData(dummyUuid: String) = sessions[dummyUuid]?.damageEntries?.toList() ?: emptyList()

    fun isInCombat(dummyUuid: String) = sessions[dummyUuid]?.isInCombat ?: false

    fun clear(dummyUuid: String) = sessions.remove(dummyUuid)
}