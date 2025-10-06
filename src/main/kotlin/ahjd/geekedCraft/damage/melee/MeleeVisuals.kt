package ahjd.geekedCraft.damage.melee

import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

object MeleeVisuals {

    // Customizable constants
    private const val PARTICLES_PER_DEGREE = 0.5  // Particles per degree of arc
    private const val MIN_PARTICLES = 5           // Minimum particles regardless of arc size
    private const val MAX_PARTICLES = 50          // Maximum particles to prevent lag
    private const val VERTICAL_OFFSET = -0.7      // How much to lower particles (negative = down)
    private val CONE_PARTICLE = Particle.ELECTRIC_SPARK

    /**
     * Displays particle effects showing the attack cone with sweep particles at edges
     */
    fun showConeVisual(player: Player, sweepDegrees: Double, range: Double) {
        val origin = player.eyeLocation
        val direction = origin.direction.normalize()
        val halfSweepRad = Math.toRadians(sweepDegrees / 2)

        val perpendicular1 = Vector(-direction.z, 0.0, direction.x).normalize()
        val perpendicular2 = direction.clone().crossProduct(perpendicular1).normalize()

        // Calculate particle count based on arc size
        val calculatedParticles = (sweepDegrees * PARTICLES_PER_DEGREE).toInt()
        val particleCount = calculatedParticles.coerceIn(MIN_PARTICLES, MAX_PARTICLES)

        // Draw particles along the front edge
        drawSweepEdges(player, origin, direction, perpendicular2, halfSweepRad, range, particleCount)
    }

    private fun drawSweepEdges(
        player: Player,
        origin: org.bukkit.Location,
        direction: Vector,
        perpendicular: Vector,
        halfSweepRad: Double,
        range: Double,
        particleCount: Int
    ) {
        // Only draw arc at the front edge
        for (j in 0..particleCount) {
            val angle = -halfSweepRad + (2 * halfSweepRad * j / particleCount.toDouble())
            val rotated = rotateAroundAxis(direction, perpendicular, angle)
            val point = origin.clone().add(rotated.multiply(range))

            // Apply vertical offset
            point.y += VERTICAL_OFFSET

            player.world.spawnParticle(
                CONE_PARTICLE,
                point,
                1,
                0.0, 0.0, 0.0,
                0.0
            )
        }
    }

    /**
     * Shows hit effect particles on a target
     */
    fun showHitParticles(target: LivingEntity) {
        val location = target.location.add(0.0, target.height / 2, 0.0)

        target.world.spawnParticle(
            Particle.DAMAGE_INDICATOR,
            location,
            10,
            0.3, 0.3, 0.3,
            0.1
        )

        target.world.spawnParticle(
            Particle.CRIT,
            location,
            5,
            0.2, 0.2, 0.2,
            0.05
        )
    }

    /**
     * Rotates a vector around an arbitrary axis using Rodrigues' rotation formula
     */
    private fun rotateAroundAxis(vector: Vector, axis: Vector, angleRad: Double): Vector {
        val cos = cos(angleRad)
        val sin = sin(angleRad)
        val oneMinusCos = 1 - cos

        val x = vector.x
        val y = vector.y
        val z = vector.z

        val ux = axis.x
        val uy = axis.y
        val uz = axis.z

        val newX = (cos + ux * ux * oneMinusCos) * x +
                (ux * uy * oneMinusCos - uz * sin) * y +
                (ux * uz * oneMinusCos + uy * sin) * z

        val newY = (uy * ux * oneMinusCos + uz * sin) * x +
                (cos + uy * uy * oneMinusCos) * y +
                (uy * uz * oneMinusCos - ux * sin) * z

        val newZ = (uz * ux * oneMinusCos - uy * sin) * x +
                (uz * uy * oneMinusCos + ux * sin) * y +
                (cos + uz * uz * oneMinusCos) * z

        return Vector(newX, newY, newZ)
    }
}