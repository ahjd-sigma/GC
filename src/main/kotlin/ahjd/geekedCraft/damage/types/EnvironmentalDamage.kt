package ahjd.geekedCraft.damage.types

import ahjd.geekedCraft.hologram.util.DamageIndicatorHolo
import ahjd.geekedCraft.human.HumanStatManager
import ahjd.geekedCraft.damage.util.EnvCause
import org.bukkit.entity.Player

object EnvironmentalDamage {
    fun dealDamage(target: Player, cause: EnvCause?, fallDistance: Int?) {
        requireNotNull(cause) { "EnvCause must be provided for Environmental damage" }

        val perdamage = when (cause) {
            EnvCause.DROWNING -> 5
            EnvCause.FIRE -> 8
            EnvCause.LAVA -> 15
            EnvCause.THORNS,
            EnvCause.CAMPFIRE,
            EnvCause.HOT_FLOOR,
            EnvCause.FIRE_TICK -> 5
            EnvCause.FLY_INTO_WALL,
            EnvCause.CRAMMING,
            EnvCause.SUFFOCATION -> 8
            EnvCause.FALLING_BLOCK -> 10
            EnvCause.VOID,
            EnvCause.BLOCK_EXPLOSION,
            EnvCause.LIGHTNING,
            EnvCause.FREEZE -> 10
            EnvCause.CONTACT -> 2
            EnvCause.FALL -> {
                if (fallDistance == null) return
                val safe = 3
                val max = 50
                val damagePerBlock = 100.0 / (max - safe)
                if (fallDistance > safe) ((fallDistance - safe) * damagePerBlock).toInt() else 0
            }
        }

        if (perdamage > 0) {
            val playerOBJ = HumanStatManager.get(target.uniqueId.toString())
            val maxHealth = playerOBJ.maxhealth
            val healthLoss = (maxHealth * (perdamage / 100.0)).toInt()

            // Use modifyBaseValue for consistency with ProjectileDamage
            playerOBJ.modifyBaseValue("health", -healthLoss)

            val dmgHolo = DamageIndicatorHolo()
            dmgHolo.showEnvironmentalIndicator(target.location, healthLoss, cause)
        }
    }
}