package ahjd.geekedCraft.listeners.damage

import ahjd.geekedCraft.damage.DealDamage
import ahjd.geekedCraft.damage.melee.MeleeHit
import ahjd.geekedCraft.damage.util.DamageType
import ahjd.geekedCraft.damage.util.EnvCause
import ahjd.geekedCraft.item.ItemManager
import ahjd.geekedCraft.mob.MobManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent

class DamageCatcherLSN : Listener {

    private fun isValidTarget(target: LivingEntity?): Boolean {
        if (target == null || target is Player) return true
        return !target.isDead && MobManager.has(target.uniqueId.toString())
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (!isValidTarget(event.entity as? LivingEntity)) {
            event.isCancelled = true
            return
        }

        when (event.damager) {
            is Player -> {
                event.isCancelled = true
                val player = event.damager as Player
                if (ItemManager.canMelee(player.inventory.itemInMainHand)) {
                    MeleeHit.onMelee(player)
                }
            }
            is Projectile -> event.isCancelled = true
        }
    }

    @EventHandler
    fun onProjectileDamage(event: ProjectileHitEvent) {
        if (event.isCancelled) return

        val target = event.hitEntity as? LivingEntity ?: return
        val shooter = event.entity.shooter as? LivingEntity ?: return

        if (!isValidTarget(target)) return

        DealDamage.applyDamage(
            attacker = shooter,
            target = target,
            type = DamageType.PROJECTILE,
            projectile = event.entity
        )
    }

    @EventHandler
    fun onPlayerLeftClick(event: PlayerInteractEvent) {
        if (event.action !in listOf(Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK)) return

        val player = event.player
        val item = player.inventory.itemInMainHand

        if (ItemManager.canMelee(item))
            MeleeHit.onMelee(player)
    }

    /** Catch environmental damage like fall, fire, void, etc. */
    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        // Skip if already cancelled or if it's entity-caused damage (handled elsewhere)
        if (event.isCancelled) return
        if (event is EntityDamageByEntityEvent) return

        val target = event.entity as? LivingEntity ?: return

        // Only players take environmental damage in your system
        if (target !is Player) {
            event.isCancelled = true
            return
        }

        val envCause = mapEventCauseToEnvCause(event.cause) ?: return

        // Get fall distance for FALL damage
        val fallDistance = if (event.cause == EntityDamageEvent.DamageCause.FALL) {
            event.entity.fallDistance.toInt()
        } else null

        // Cancel vanilla damage AFTER getting fall distance
        event.damage = 0.0
        event.isCancelled = true

        DealDamage.applyDamage(
            attacker = target,
            target = target,
            type = DamageType.ENVIRONMENTAL,
            cause = envCause,
            fallDistance = fallDistance
        )
    }

    /** Map Bukkit damage cause to your EnvCause */
    private fun mapEventCauseToEnvCause(cause: EntityDamageEvent.DamageCause): EnvCause? {
        return when (cause) {
            EntityDamageEvent.DamageCause.FIRE -> EnvCause.FIRE
            EntityDamageEvent.DamageCause.CAMPFIRE -> EnvCause.CAMPFIRE
            EntityDamageEvent.DamageCause.LAVA -> EnvCause.LAVA
            EntityDamageEvent.DamageCause.FALL -> EnvCause.FALL
            EntityDamageEvent.DamageCause.DROWNING -> EnvCause.DROWNING
            EntityDamageEvent.DamageCause.VOID -> EnvCause.VOID
            EntityDamageEvent.DamageCause.CONTACT -> EnvCause.CONTACT
            EntityDamageEvent.DamageCause.LIGHTNING -> EnvCause.LIGHTNING
            EntityDamageEvent.DamageCause.FIRE_TICK -> EnvCause.FIRE_TICK
            EntityDamageEvent.DamageCause.CRAMMING -> EnvCause.CRAMMING
            EntityDamageEvent.DamageCause.SUFFOCATION -> EnvCause.SUFFOCATION
            EntityDamageEvent.DamageCause.FALLING_BLOCK -> EnvCause.FALLING_BLOCK
            EntityDamageEvent.DamageCause.BLOCK_EXPLOSION -> EnvCause.BLOCK_EXPLOSION
            EntityDamageEvent.DamageCause.FLY_INTO_WALL -> EnvCause.FLY_INTO_WALL
            EntityDamageEvent.DamageCause.HOT_FLOOR -> EnvCause.HOT_FLOOR
            EntityDamageEvent.DamageCause.CUSTOM -> EnvCause.CONTACT
            else -> null
        }
    }
}