package ahjd.geekedCraft.damage

import ahjd.geekedCraft.damage.types.EnvironmentalDamage
import ahjd.geekedCraft.damage.types.MagicDamage
import ahjd.geekedCraft.damage.types.MeleeDamage
import ahjd.geekedCraft.damage.types.ProjectileDamage
import ahjd.geekedCraft.damage.util.DamageType
import ahjd.geekedCraft.damage.util.EnvCause
import ahjd.geekedCraft.damage.util.IFrameManager
import ahjd.geekedCraft.damage.util.KnockbackUtil
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket
import org.bukkit.Sound
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.util.Vector

/**
 * Central damage handling system
 * Coordinates damage calculation, animations, and knockback
 */
object DealDamage {

    fun applyDamage(
        attacker: LivingEntity,
        target: LivingEntity,
        type: DamageType,
        projectile: Projectile? = null,
        magicKbDirection: Vector? = null,
        magicKbStrength: Int? = null,
        cause: EnvCause? = null,
        fallDistance: Int? = null
    ) {
        // Check i-frames
        if (!IFrameManager.canTakeDamage(target)) return

        // Handle damage by type
        when (type) {
            DamageType.MELEE -> {
                handleMelee(attacker, target)
                KnockbackUtil.applyMeleeKnockback(attacker, target)
            }

            DamageType.PROJECTILE -> {
                handleProjectile(attacker, target)
                if (projectile != null) {
                    KnockbackUtil.applyProjectileKnockback(attacker, target, projectile)
                }
            }

            DamageType.MAGIC -> {
                handleMagic(attacker, target)
                if (magicKbDirection != null && magicKbStrength != null) {
                    KnockbackUtil.applyMagicKnockback(target, magicKbDirection, magicKbStrength)
                }
            }

            DamageType.ENVIRONMENTAL -> {
                handleEnvironmental(target, cause, fallDistance)
            }
        }

        // Visual and audio feedback
        sendHurtAnimationNMS(target)

        // Start i-frame cooldown
        IFrameManager.startIFrame(target)
    }

    // ==================== DAMAGE TYPE HANDLERS ====================

    private fun handleMelee(attacker: LivingEntity, target: LivingEntity) {
        handleCombat(
            attacker, target,
            MeleeDamage::onPlayerToPlayer,
            MeleeDamage::onPlayerToMob,
            MeleeDamage::onMobToPlayer,
            MeleeDamage::onMobToMob
        )
    }

    private fun handleProjectile(attacker: LivingEntity, target: LivingEntity) {
        when {
            attacker is Player && target is Player ->
                ProjectileDamage.onPlayerToPlayer(attacker, target)

            attacker is Player && target !is Player ->
                ProjectileDamage.onPlayerToMob(attacker, target)

            attacker !is Player && target is Player ->
                ProjectileDamage.onMobToPlayer(attacker, target)

            attacker !is Player && target !is Player ->
                ProjectileDamage.onMobToMob(attacker, target)
        }
    }

    private fun handleMagic(attacker: LivingEntity, target: LivingEntity) {
        handleCombat(
            attacker, target,
            MagicDamage::onPlayerToPlayer,
            MagicDamage::onPlayerToMob,
            MagicDamage::onMobToPlayer,
            MagicDamage::onMobToMob
        )
    }

    private fun handleEnvironmental(target: LivingEntity, cause: EnvCause?, fallDistance: Int?) {
        requireNotNull(cause) { "EnvCause must be provided for Environmental damage" }
        if (target is Player) {
            EnvironmentalDamage.dealDamage(target, cause, fallDistance)
        }
    }

    // ==================== HELPERS ====================

    private fun handleCombat(
        attacker: LivingEntity,
        target: LivingEntity,
        playerToPlayer: (Player, Player) -> Unit,
        playerToMob: (Player, LivingEntity) -> Unit,
        mobToPlayer: (LivingEntity, Player) -> Unit,
        mobToMob: (LivingEntity, LivingEntity) -> Unit
    ) {
        when {
            attacker is Player && target is Player -> playerToPlayer(attacker, target)
            attacker is Player && target !is Player -> playerToMob(attacker, target)
            attacker !is Player && target is Player -> mobToPlayer(attacker, target)
            attacker !is Player && target !is Player -> mobToMob(attacker, target)
        }
    }

    /**
     * Send hurt animation using NMS packet
     * Provides visual feedback for damage
     */
    private fun sendHurtAnimationNMS(entity: LivingEntity) {
        val nmsEntity = (entity as CraftLivingEntity).handle
        val packet = ClientboundHurtAnimationPacket(nmsEntity)

        val entityLocation = entity.location

        // Send packet to all nearby players (64 block radius)
        entity.world.players.forEach { player ->
            if (player.location.distance(entityLocation) < 64) {
                (player as CraftPlayer).handle.connection.send(packet)
            }
        }

        // Play hurt sound
        playHurtSound(entity, entityLocation)
    }

    /**
     * Play appropriate hurt sound based on entity type
     */
    private fun playHurtSound(entity: LivingEntity, location: org.bukkit.Location) {
        if (entity is Player) {
            entity.playSound(location, Sound.ENTITY_PLAYER_HURT, 1.0f, 1.0f)
        } else {
            val soundName = "entity.${entity.type.name.lowercase()}.hurt"
            try {
                entity.world.playSound(location, soundName, 1.0f, 1.0f)
            } catch (e: Exception) {
                // Fallback to generic hurt sound if entity-specific sound doesn't exist
                entity.world.playSound(location, Sound.ENTITY_GENERIC_HURT, 1.0f, 1.0f)
            }
        }
    }
}