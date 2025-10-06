package ahjd.geekedCraft.damage.util

import ahjd.geekedCraft.human.HumanStatManager
import ahjd.geekedCraft.mob.MobManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.util.Vector

object KnockbackUtil {

    private const val BASE_KNOCKBACK = 0.4 // Base horizontal knockback
    private const val BASE_VERTICAL = 0.3 // Base vertical knockback
    private const val DEFAULT_KB = 100 // Default knockback if no stats are found

    /**
     * Apply melee knockback (uses attacker's knockback stat)
     * Direction: away from attacker
     */
    fun applyMeleeKnockback(attacker: LivingEntity, target: LivingEntity) {
        val attackerKb = getAttackerKb(attacker)

        val direction = target.location.toVector()
            .subtract(attacker.location.toVector())
            .setY(0)
            .normalize()

        applyKnockbackInternal(target, direction, attackerKb)
    }

    /**
     * Apply projectile knockback (uses attacker's knockback stat)
     * Direction: projectile's flight direction
     */
    fun applyProjectileKnockback(attacker: LivingEntity, target: LivingEntity, projectile: Projectile) {
        val attackerKb = getAttackerKb(attacker)

        val direction = projectile.velocity.clone().normalize()
        applyKnockbackInternal(target, direction, attackerKb)
    }

    /**
     * Apply magic knockback (custom direction and strength)
     */
    fun applyMagicKnockback(target: LivingEntity, direction: Vector, kbStrength: Int) {
        applyKnockbackInternal(target, direction, kbStrength)
    }

    /**
     * Get attacker's knockback stat
     */
    private fun getAttackerKb(attacker: LivingEntity): Int {
        return when {
            attacker is Player -> HumanStatManager.get(attacker.uniqueId.toString()).knockback
            MobManager.has(attacker.uniqueId.toString()) -> MobManager.get(attacker.uniqueId.toString()).knockback
            else -> DEFAULT_KB
        }
    }

    /**
     * Internal knockback application with resistance calculations
     */
    private fun applyKnockbackInternal(
        target: LivingEntity,
        direction: Vector,
        attackerKb: Int
    ) {
        val targetKbResistance = when {
            target is Player -> {
                val stats = HumanStatManager.get(target.uniqueId.toString())
                try {
                    stats.getValue("knockbackResistance")?.coerceIn(0, 100)?.div(100.0) ?: 0.0
                } catch (e: Exception) {
                    0.0
                }
            }
            MobManager.has(target.uniqueId.toString()) -> {
                MobManager.get(target.uniqueId.toString()).knockbackResistance / 100.0
            }
            else -> 0.0
        }

        val kbMultiplier = attackerKb / 100.0
        val resistanceMultiplier = 1.0 - targetKbResistance
        val finalKbStrength = kbMultiplier * resistanceMultiplier

        if (kotlin.math.abs(finalKbStrength) < 0.01) return

        val normalizedDir = direction.clone().setY(0).normalize()
        val horizontalKb = normalizedDir.multiply(BASE_KNOCKBACK * finalKbStrength)
        val verticalKb = BASE_VERTICAL * kotlin.math.abs(finalKbStrength)

        val knockbackVector = Vector(horizontalKb.x, verticalKb, horizontalKb.z)
        target.velocity = target.velocity.add(knockbackVector)
    }

    /**
     * Apply explosion-like knockback (radial from a point)
     */
    fun applyExplosionKnockback(
        epicenter: org.bukkit.Location,
        target: LivingEntity,
        power: Double = 1.0,
        sourceKb: Int = DEFAULT_KB
    ) {
        val direction = target.location.toVector()
            .subtract(epicenter.toVector())

        val effectiveKb = (sourceKb * power).toInt()
        applyKnockbackInternal(target, direction, effectiveKb)
    }
}
