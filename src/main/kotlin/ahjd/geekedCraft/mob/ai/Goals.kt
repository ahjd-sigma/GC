package ahjd.geekedCraft.mob.ai

import ahjd.geekedCraft.damage.DealDamage
import ahjd.geekedCraft.damage.util.DamageType
import ahjd.geekedCraft.main.GeekedCraft
import com.destroystokyo.paper.entity.ai.Goal
import com.destroystokyo.paper.entity.ai.GoalKey
import com.destroystokyo.paper.entity.ai.GoalType
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*
import kotlin.random.Random

abstract class BaseGoal(protected val mob: Mob, plugin: GeekedCraft, name: String) : Goal<Mob> {
    private val key = GoalKey.of(Mob::class.java, NamespacedKey(plugin, name))
    override fun getKey(): GoalKey<Mob> = key

    protected fun Player.isValidPlayer(): Boolean = isValid && !isDead &&
            gameMode != GameMode.SPECTATOR && gameMode != GameMode.CREATIVE
}

// ==================== AttackNearbyPlayerGoal ====================

class AttackNearbyPlayerGoal(
    mob: Mob, p: GeekedCraft,
    private val detect: Double = 16.0,
    private val attack: Double = 2.0
) : BaseGoal(mob, p, "attack_nearby") {

    private var pathTicks = 0
    private var dmgTicks = 0

    override fun shouldActivate(): Boolean {
        val target = mob.target as? Player
        if (target?.isValidPlayer() == true) return true

        mob.location.getNearbyPlayers(detect).filter { it.isValidPlayer() }
            .minByOrNull { it.location.distance(mob.location) }?.let {
                mob.target = it
                return true
            }
        return false
    }

    override fun shouldStayActive(): Boolean = (mob.target as? Player)?.let {
        it.isValidPlayer() && mob.location.distance(it.location) <= detect * 1.5
    } ?: false

    override fun start() {
        mob.target?.let { mob.pathfinder.moveTo(it, 1.0) }
    }

    override fun tick() {
        val t = mob.target as? Player ?: return
        val d = mob.location.distance(t.location)

        if (++pathTicks >= 3) {
            if (!mob.pathfinder.hasPath() || d > 3.0) mob.pathfinder.moveTo(t, 1.0)
            pathTicks = 0
        }

        if (d <= attack && ++dmgTicks >= 10) {
            mob.swingMainHand()
            DealDamage.applyDamage(mob, t, DamageType.MELEE)
            dmgTicks = 0
        }
    }

    override fun stop() {
        mob.target = null
        mob.pathfinder.stopPathfinding()
    }

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE, GoalType.TARGET)
}

// ==================== SelfDefenseGoal ====================

class SelfDefenseGoal(
    mob: Mob, p: GeekedCraft,
    private val attack: Double = 2.0,
    private val chase: Double = 16.0
) : BaseGoal(mob, p, "defensive") {

    private var pathTicks = 0

    override fun shouldActivate(): Boolean {
        val target = mob.target
        if (target != null && target.isValid) return true

        val damageEntity = mob.lastDamageCause?.entity
        if (damageEntity is LivingEntity) {
            mob.target = damageEntity
            return true
        }
        return false
    }

    override fun shouldStayActive(): Boolean {
        val target = mob.target ?: return false
        return target.isValid && mob.location.distance(target.location) <= chase * 1.5
    }

    override fun start() {
        mob.target?.let { mob.pathfinder.moveTo(it, 1.0) }
    }

    override fun tick() {
        val t = mob.target ?: return
        val d = mob.location.distance(t.location)

        if (++pathTicks >= 3) {
            if (!mob.pathfinder.hasPath() || d > 3.0) mob.pathfinder.moveTo(t, 1.0)
            pathTicks = 0
        }

        if (d <= attack) mob.attack(t)
    }

    override fun stop() {
        mob.target = null
        mob.pathfinder.stopPathfinding()
    }

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE, GoalType.TARGET)
}

// ==================== StationaryGoal ====================

class StationaryGoal(mob: Mob, p: GeekedCraft, private val look: Double = 10.0) :
    BaseGoal(mob, p, "stationary") {

    private val spawn = mob.location.clone()

    override fun shouldActivate(): Boolean = true
    override fun shouldStayActive(): Boolean = true
    override fun start() { resetPos() }
    override fun stop() {
        mob.target = null
        resetPos()
    }

    override fun tick() {
        if (mob.velocity.lengthSquared() > 0.001) mob.velocity = Vector(0, 0, 0)

        mob.location.getNearbyPlayers(look).filter { it.isValidPlayer() }
            .minByOrNull { it.location.distance(mob.location) }?.let {
                mob.target = it
                val dir = it.eyeLocation.toVector().subtract(mob.eyeLocation.toVector()).normalize()
                mob.location.clone().apply { direction = dir }.let { l ->
                    mob.setRotation(l.yaw, l.pitch)
                }
            } ?: run {
            val target = mob.target as? Player
            if (target != null && (!target.isValidPlayer() ||
                        mob.location.distance(target.location) > look * 1.5)) {
                mob.target = null
            }
        }

        if (mob.location.distance(spawn) > 0.1) resetPos()
    }

    private fun resetPos() {
        if (mob.location.distance(spawn) > 0.1) mob.teleport(spawn)
        mob.velocity = Vector(0, 0, 0)
    }

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.LOOK, GoalType.MOVE)
}

// ==================== WanderGoal ====================

class WanderGoal(mob: Mob, p: GeekedCraft, private val radius: Double = 5.0) :
    BaseGoal(mob, p, "wander") {

    private val spawn = mob.location.clone()
    private var cooldown = 0

    override fun shouldActivate(): Boolean {
        if (cooldown > 0) {
            cooldown--
            return false
        }
        return mob.target == null && !mob.pathfinder.hasPath() && Random.nextDouble() < 0.08
    }

    override fun shouldStayActive(): Boolean = mob.pathfinder.hasPath() && mob.target == null

    override fun start() {
        val offset = Vector(
            Random.nextDouble(-radius, radius),
            0.0,
            Random.nextDouble(-radius, radius)
        )
        spawn.clone().add(offset).apply {
            y = mob.world.getHighestBlockYAt(this).toDouble() + 1
            mob.pathfinder.moveTo(this, 0.8)
        }
        cooldown = 60
    }

    override fun stop() {
        mob.pathfinder.stopPathfinding()
    }

    override fun tick() {
        if (!mob.pathfinder.hasPath()) stop()
    }

    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE)
}

// ==================== IdleGoal ====================

class IdleGoal(mob: Mob, p: GeekedCraft) : BaseGoal(mob, p, "idle") {
    override fun shouldActivate(): Boolean = mob.target == null && !mob.pathfinder.hasPath()
    override fun shouldStayActive(): Boolean = mob.target == null && !mob.pathfinder.hasPath()
    override fun start() {}
    override fun stop() {}
    override fun tick() {}
    override fun getTypes(): EnumSet<GoalType> = EnumSet.noneOf(GoalType::class.java)
}

// ==================== GuardGoal ====================

class GuardGoal(mob: Mob, p: GeekedCraft) : BaseGoal(mob, p, "guardian") {
    override fun shouldActivate(): Boolean = false
    override fun shouldStayActive(): Boolean = false
    override fun start() {}
    override fun stop() {}
    override fun tick() {}
    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE, GoalType.TARGET)
}

// ==================== PatrolGoal ====================

class PatrolGoal(mob: Mob, p: GeekedCraft) : BaseGoal(mob, p, "patrol") {
    override fun shouldActivate(): Boolean = false
    override fun shouldStayActive(): Boolean = false
    override fun start() {}
    override fun stop() {}
    override fun tick() {}
    override fun getTypes(): EnumSet<GoalType> = EnumSet.of(GoalType.MOVE, GoalType.TARGET)
}