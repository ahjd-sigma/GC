package ahjd.geekedCraft.mob.util.dummy

import ahjd.geekedCraft.damage.DealDamage
import ahjd.geekedCraft.damage.util.DamageType
import ahjd.geekedCraft.main.GeekedCraft
import ahjd.geekedCraft.mob.MobManager
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object DummyDPSTest {
    private val activeSessions = ConcurrentHashMap<String, DPSSession>()
    private var tickerTaskId: Int? = null
    private const val TEST_RADIUS = 10.0

    data class DPSSession(
        val dummyUUID: String,
        val playerUUID: String,
        val damageType: DamageType,
        var totalDamage: Int = 0,
        var attackCount: Int = 0,
        val startTime: Long = System.currentTimeMillis(),
        var lastAttackTime: Long = 0L,
        var isActive: Boolean = true
    )

    data class SessionInfo(
        val isActive: Boolean,
        val damageType: DamageType,
        val totalDamage: Int,
        val attackCount: Int,
        val dps: Double,
        val duration: Double
    )

    fun start() {
        if (tickerTaskId != null) return
        tickerTaskId = GeekedCraft.getInstance().scheduleRepeatingTask({ tick() }, 0L, 1L)
    }

    fun stop() {
        tickerTaskId?.let { Bukkit.getScheduler().cancelTask(it) }
        tickerTaskId = null
        activeSessions.clear()
    }

    private fun tick() {
        val now = System.currentTimeMillis()
        activeSessions.entries.removeAll { (key, s) ->
            if (!s.isActive) return@removeAll true

            val dummy = Bukkit.getEntity(UUID.fromString(s.dummyUUID)) as? LivingEntity
            val player = Bukkit.getPlayer(UUID.fromString(s.playerUUID))

            when {
                dummy == null || player == null || !player.isOnline || player.isDead -> {
                    endSession(key, "Target invalid")
                    true
                }
                dummy.location.distance(player.location) > TEST_RADIUS -> {
                    endSession(key, "Player left test radius")
                    true
                }
                !MobManager.has(s.dummyUUID) -> true
                else -> {
                    val attackSpeed = MobManager.get(s.dummyUUID).attackSpeed
                    if (attackSpeed > 0) {
                        val attackDelayMs = (1000.0 / attackSpeed).toLong()
                        if (now - s.lastAttackTime >= attackDelayMs) {
                            performAttack(dummy, player, s)
                            s.lastAttackTime = now
                            s.attackCount++
                        }
                    }
                    false
                }
            }
        }
    }

    private fun performAttack(dummy: LivingEntity, player: Player, s: DPSSession) {
        DealDamage.applyDamage(dummy, player, s.damageType)
        val stats = MobManager.get(s.dummyUUID)
        s.totalDamage += when (s.damageType) {
            DamageType.MELEE -> stats.meleeRaw
            DamageType.PROJECTILE -> stats.projectileRaw
            DamageType.MAGIC -> stats.magicRaw
            else -> 0
        }
    }

    fun startTest(player: Player, dummyUUID: String, damageType: DamageType): Boolean {
        val dummy = Bukkit.getEntity(UUID.fromString(dummyUUID)) as? LivingEntity
        if (dummy == null || !MobManager.has(dummyUUID)) {
            player.sendMessage("§c§l✖ §cInvalid dummy!")
            return false
        }

        if (activeSessions.values.any { it.playerUUID == player.uniqueId.toString() }) {
            player.sendMessage("§c§l✖ §cYou already have an active DPS test!")
            return false
        }

        if (dummy.location.distance(player.location) > TEST_RADIUS) {
            player.sendMessage("§c§l✖ §cYou're too far from the dummy!")
            return false
        }

        val key = "${dummyUUID}_${player.uniqueId}"
        activeSessions[key] = DPSSession(dummyUUID, player.uniqueId.toString(), damageType, lastAttackTime = System.currentTimeMillis())

        val stats = MobManager.get(dummyUUID)
        player.sendMessage("§a§l✓ §aDPS Test started!")
        player.sendMessage("§7Type: §f${damageType.name}")
        player.sendMessage("§7Attack Speed: §f${stats.attackSpeed}/s")
        player.sendMessage("§7Stay within §e$TEST_RADIUS blocks §7to continue")

        return true
    }

    fun stopTest(player: Player): Boolean {
        val key = activeSessions.entries.find { it.value.playerUUID == player.uniqueId.toString() }?.key
        if (key == null) {
            player.sendMessage("§c§l✖ §cNo active DPS test!")
            return false
        }

        endSession(key, "Test stopped by player")
        activeSessions.remove(key)
        return true
    }

    fun getDummyDPS(dummyUUID: String): Double {
        val s = activeSessions.values.find { it.dummyUUID == dummyUUID } ?: return 0.0
        val duration = (System.currentTimeMillis() - s.startTime) / 1000.0
        return if (duration > 0) s.totalDamage / duration else 0.0
    }

    fun getSessionInfo(dummyUUID: String): SessionInfo? {
        val s = activeSessions.values.find { it.dummyUUID == dummyUUID } ?: return null
        val duration = (System.currentTimeMillis() - s.startTime) / 1000.0
        val dps = if (duration > 0) s.totalDamage / duration else 0.0
        return SessionInfo(s.isActive, s.damageType, s.totalDamage, s.attackCount, dps, duration)
    }

    private fun endSession(key: String, reason: String) {
        val s = activeSessions[key] ?: return
        val player = Bukkit.getPlayer(UUID.fromString(s.playerUUID))
        val duration = (System.currentTimeMillis() - s.startTime) / 1000.0
        val dps = if (duration > 0) s.totalDamage / duration else 0.0

        player?.apply {
            sendMessage("§e§l⚡ DPS Test Ended")
            sendMessage("§7Reason: §f$reason")
            sendMessage("§7Duration: §f%.1fs".format(duration))
            sendMessage("§7Total Damage: §f${s.totalDamage}")
            sendMessage("§7Total Damage: §f${s.totalDamage}")
            sendMessage("§7Attacks: §f${s.attackCount}")
            sendMessage("§7DPS: §f%.2f".format(dps))
        }

        s.isActive = false
    }
}