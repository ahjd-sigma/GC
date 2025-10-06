package ahjd.geekedCraft.damage.melee

import ahjd.geekedCraft.human.HumanStatManager
import ahjd.geekedCraft.main.GeekedCraft
import ahjd.geekedCraft.damage.util.DamageType
import ahjd.geekedCraft.damage.DealDamage
import ahjd.geekedCraft.mob.MobManager
import net.minecraft.network.protocol.game.ClientboundCooldownPacket
import net.minecraft.resources.ResourceLocation
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

object MeleeHit {
    private val plugin = GeekedCraft.getInstance()
    private val keyLastUse = NamespacedKey(plugin, "last_use_time")

    // Internal cooldown map: stores last attack time per player
    private val playerLastAttackTime = mutableMapOf<String, Long>()

    fun onMelee(attacker: Player) {
        val mainHand = attacker.inventory.itemInMainHand
        val uuid = attacker.uniqueId.toString()
        val stats = HumanStatManager.get(uuid)

        // Get attack speed stat
        val attackSpeed = stats.getValue("attackspeed") ?: 0

        // Calculate player's global cooldown based on item's base cooldown and attack speed
        val meta = mainHand.itemMeta
        val baseCooldownSeconds = meta?.getUseCooldown()?.getCooldownSeconds() ?: 0f
        val playerCooldownMs = if (baseCooldownSeconds > 0f) {
            val finalCooldown = (baseCooldownSeconds * (1.0f - attackSpeed / 100.0f)).coerceAtLeast(0.1f)
            (finalCooldown * 1000).toLong()
        } else {
            500L // Default 500ms if item has no cooldown
        }

        // Check global player cooldown (prevents weapon swapping)
        val now = System.currentTimeMillis()
        val lastAttack = playerLastAttackTime[uuid] ?: 0L
        if (now - lastAttack < playerCooldownMs) return

        // Check if item is on cooldown
        if (isOnCooldown(attacker, mainHand)) return

        // Update global player cooldown
        playerLastAttackTime[uuid] = now

        // Get other stats
        val sweep = stats.getValue("sweep")?.toDouble() ?: 60.0
        val range = stats.getValue("range")?.toDouble() ?: 3.0

        // Show visual cone
        showConeVisual(attacker, sweep, range)

        // Attack logic
        val origin = attacker.eyeLocation
        val direction = origin.direction.normalize()
        val halfSweepRad = Math.toRadians(sweep / 2)

        val nearbyEntities = origin.world?.getNearbyEntities(origin, range, range, range)
            ?.filterIsInstance<LivingEntity>()
            ?.filter { it != attacker } ?: return

        var hitSomething = false

        for (target in nearbyEntities) {
            val toTarget = target.location.add(0.0, target.height / 2, 0.0).toVector().subtract(origin.toVector())
            val distance = toTarget.length()
            if (distance > range) continue

            val angle = direction.angle(toTarget.normalize())
            if (angle <= halfSweepRad) {
                val isValidTarget = when (target) {
                    is Player -> true
                    else -> MobManager.has(target.uniqueId.toString())
                }
                if (!isValidTarget) continue

                DealDamage.applyDamage(
                    attacker = attacker,
                    target = target,
                    type = DamageType.MELEE
                )

                showHitParticles(target)
                hitSomething = true
            }
        }

        applyCooldown(mainHand, attackSpeed, attacker)
    }

    private fun isOnCooldown(player: Player, item: ItemStack): Boolean {
        if (item.type == Material.AIR) return false
        val meta = item.itemMeta ?: return false
        val cdComp = meta.getUseCooldown()
        val cooldownSec = cdComp.getCooldownSeconds()
        if (cooldownSec <= 0f) return false

        val pdc = meta.persistentDataContainer
        val lastUse = pdc.get(keyLastUse, PersistentDataType.LONG) ?: return false

        val now = System.currentTimeMillis()
        return now - lastUse < (cooldownSec * 1000).toLong()
    }

    private fun applyCooldown(item: ItemStack, attackSpeed: Int, player: Player) {
        val meta = item.itemMeta ?: return
        val pdc = meta.persistentDataContainer

        val cdComp = meta.getUseCooldown()
        val baseCooldown = cdComp.getCooldownSeconds()
        val finalCooldown = (baseCooldown * (1.0f - attackSpeed / 100.0f)).coerceAtLeast(0.1f)
        val now = System.currentTimeMillis()

        pdc.set(keyLastUse, PersistentDataType.LONG, now)
        item.itemMeta = meta

        val cooldownTicks = (finalCooldown * 20).toInt()
        sendItemCooldownPacket(player, item, cooldownTicks)
    }

    private fun sendItemCooldownPacket(player: Player, item: ItemStack, ticks: Int) {
        val nmsPlayer = (player as CraftPlayer).handle
        val resourceLocation = getItemResourceLocation(item)
        val packet = ClientboundCooldownPacket(resourceLocation, ticks)
        nmsPlayer.connection.send(packet)
    }

    private fun getItemResourceLocation(item: ItemStack): ResourceLocation {
        val path = item.type.key.key
        return ResourceLocation.withDefaultNamespace(path)
    }

    private fun showConeVisual(player: Player, sweepDegrees: Double, range: Double) {
        val origin = player.eyeLocation
        val direction = origin.direction.normalize()

        val halfSweepRad = Math.toRadians(sweepDegrees / 2)

        // Create perpendicular vectors for the cone base
        val perpendicular1 = Vector(-direction.z, 0.0, direction.x).normalize()
        val perpendicular2 = direction.clone().crossProduct(perpendicular1).normalize()

        // Draw multiple arcs to show the cone
        val arcCount = 3
        val particlesPerArc = 20

        for (i in 1..arcCount) {
            val currentRange = range * (i.toDouble() / arcCount)

            for (j in 0 until particlesPerArc) {
                val angle = -halfSweepRad + (2 * halfSweepRad * j / (particlesPerArc - 1))

                val rotated = rotateAroundAxis(direction, perpendicular2, angle)
                val point = origin.clone().add(rotated.multiply(currentRange))

                val colorFade = i.toDouble() / arcCount
                val red = 255
                val green = (100 * colorFade).toInt()
                val blue = 0

                player.world.spawnParticle(
                    Particle.DUST,
                    point,
                    1,
                    0.0, 0.0, 0.0,
                    Particle.DustOptions(Color.fromRGB(red, green, blue), 0.5f)
                )
            }
        }

        // Draw edge lines
        val edgeParticles = 15
        for (i in 0..edgeParticles) {
            val currentRange = range * (i.toDouble() / edgeParticles)

            val leftRotated = rotateAroundAxis(direction, perpendicular2, -halfSweepRad)
            val leftPoint = origin.clone().add(leftRotated.multiply(currentRange))
            player.world.spawnParticle(
                Particle.DUST,
                leftPoint,
                1,
                0.0, 0.0, 0.0,
                Particle.DustOptions(Color.fromRGB(255, 50, 0), 0.7f)
            )

            val rightRotated = rotateAroundAxis(direction, perpendicular2, halfSweepRad)
            val rightPoint = origin.clone().add(rightRotated.multiply(currentRange))
            player.world.spawnParticle(
                Particle.DUST,
                rightPoint,
                1,
                0.0, 0.0, 0.0,
                Particle.DustOptions(Color.fromRGB(255, 50, 0), 0.7f)
            )
        }
    }

    private fun showHitParticles(target: LivingEntity) {
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