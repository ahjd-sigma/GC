package ahjd.geekedCraft.damage.melee

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.RayTraceResult

object ConeDetection {

    /**
     * Gets all living entities within a cone area that have line of sight
     * @param attacker The player performing the attack
     * @param sweepDegrees The angle of the cone in degrees
     * @param range The maximum distance of the cone
     * @return List of entities that are hit
     */
    fun getTargetsInCone(attacker: Player, sweepDegrees: Double, range: Double): List<LivingEntity> {
        val origin = attacker.eyeLocation
        val direction = origin.direction.normalize()
        val halfSweepRad = Math.toRadians(sweepDegrees / 2)

        val nearbyEntities = origin.world?.getNearbyEntities(origin, range, range, range)
            ?.filterIsInstance<LivingEntity>()
            ?.filter { it != attacker } ?: return emptyList()

        val targets = mutableListOf<LivingEntity>()

        for (target in nearbyEntities) {
            val toTarget = target.location.add(0.0, target.height / 2, 0.0).toVector().subtract(origin.toVector())
            val distance = toTarget.length()

            if (distance > range) continue

            val angle = direction.angle(toTarget.normalize())
            if (angle <= halfSweepRad) {
                if (hasLineOfSight(attacker, target, distance)) {
                    targets.add(target)
                }
            }
        }

        return targets
    }

    /**
     * Checks if there's a clear line of sight between attacker and target
     * Returns true if no solid blocks obstruct the path
     */
    private fun hasLineOfSight(attacker: Player, target: LivingEntity, distance: Double): Boolean {
        val start = attacker.eyeLocation
        val targetCenter = target.location.add(0.0, target.height / 2, 0.0)

        val direction = targetCenter.toVector().subtract(start.toVector()).normalize()

        val rayTrace: RayTraceResult? = start.world?.rayTraceBlocks(
            start,
            direction,
            distance,
            org.bukkit.FluidCollisionMode.NEVER,
            true
        )

        if (rayTrace != null && rayTrace.hitBlock != null) {
            val hitDistance = start.distance(rayTrace.hitPosition.toLocation(start.world!!))
            return hitDistance >= distance
        }

        return true
    }
}