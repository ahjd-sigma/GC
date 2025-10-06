package ahjd.geekedCraft.mob.stats

import ahjd.geekedCraft.events.mob.MobDeathEvent
import ahjd.geekedCraft.events.mob.MobSpeedChangeEvent
import ahjd.geekedCraft.mob.misc.MobHealthDisplay
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import java.util.*

data class MobStats(
    var uuid: String,
    var name: String,
    var mobKind: MobEnums.Kind,
    var mobType: MobEnums.Type,
    var iframes: Long = 500L,
    var knockback: Int = 100,
    var knockbackResistance: Int = 0,
    var attackSpeed: Int = 0,
    var health: Int = 100,
    var maxhealth: Int = 100,
    var healthregen: Int = 5,
    var defense: Int = 0,
    var speed: Int = 100,
    var aiType: MobEnums.AI,
    var spawnPoint: Location,
    var protectionRange: Double,
    var meleeRaw: Int = 0,
    var projectileRaw: Int = 0,
    var magicRaw: Int = 0,
    var elementalRaw: Int = 0,
    var thunderRaw: Int = 0,
    var earthRaw: Int = 0,
    var waterRaw: Int = 0,
    var fireRaw: Int = 0,
    var airRaw: Int = 0,
    var darkRaw: Int = 0,
    var lightRaw: Int = 0,
    var neutralRaw: Int = 0,
    var thunderResistance: Int = 0,
    var earthResistance: Int = 0,
    var waterResistance: Int = 0,
    var fireResistance: Int = 0,
    var airResistance: Int = 0,
    var darkResistance: Int = 0,
    var lightResistance: Int = 0,
    var neutralResistance: Int = 0,
    var bleedingResistance: Int = 0,
    var poisonResistance: Int = 0,
    var isDummy: Boolean = false
) {
    companion object {
        private val ranges = mapOf(
            "health" to (0..100000), "maxhealth" to (5..100000), "healthregen" to (-10000..10000),
            "defense" to (0..10000), "speed" to (-100..500), "attackSpeed" to (-1000..1000),
            "knockbackResistance" to (0..100), "knockback" to (0..500)
        ).withDefault { 0..100000 } // Default range for all damage/resistance stats
    }

    fun setValue(stat: String, value: Int): Boolean {
        return try {
            val field = this::class.java.getDeclaredField(stat).apply { isAccessible = true }
            val coerced = value.coerceIn(ranges.getValue(stat))
            field.setInt(this, coerced)

            when (stat) {
                "health" -> {
                    if (!isDummy) {
                        health = health.coerceIn(0, maxhealth)
                        (Bukkit.getEntity(UUID.fromString(uuid)) as? LivingEntity)?.let {
                            MobHealthDisplay.updateHealthDisplay(it, this)
                            if (health <= 0) Bukkit.getPluginManager().callEvent(MobDeathEvent(it))
                        }
                    }
                }
                "maxhealth" -> {
                    if (health > maxhealth) health = maxhealth
                    (Bukkit.getEntity(UUID.fromString(uuid)) as? LivingEntity)?.let {
                        MobHealthDisplay.updateHealthDisplay(it, this)
                    }
                }
                "speed" -> (Bukkit.getEntity(UUID.fromString(uuid)) as? LivingEntity)?.let {
                    Bukkit.getPluginManager().callEvent(MobSpeedChangeEvent(it, speed))
                }
            }
            true
        } catch (e: NoSuchFieldException) {
            false
        }
    }

    fun getValue(stat: String): Int =
        this::class.java.getDeclaredField(stat).apply { isAccessible = true }.getInt(this)

    /**
     * Converts all MobStats properties to a Map for easy iteration and display
     * @return Map of property names to their values
     */
    fun toMap(): Map<String, Any> = mapOf(
        "uuid" to uuid,
        "name" to name,
        "mobKind" to mobKind.name,
        "mobType" to mobType.name,
        "aiType" to aiType.name,
        "iframes" to iframes,
        "knockback" to knockback,
        "knockbackResistance" to knockbackResistance,
        "attackSpeed" to attackSpeed,
        "health" to health,
        "maxhealth" to maxhealth,
        "healthregen" to healthregen,
        "defense" to defense,
        "speed" to speed,
        "protectionRange" to protectionRange,
        "meleeRaw" to meleeRaw,
        "projectileRaw" to projectileRaw,
        "magicRaw" to magicRaw,
        "elementalRaw" to elementalRaw,
        "thunderRaw" to thunderRaw,
        "earthRaw" to earthRaw,
        "waterRaw" to waterRaw,
        "fireRaw" to fireRaw,
        "airRaw" to airRaw,
        "darkRaw" to darkRaw,
        "lightRaw" to lightRaw,
        "neutralRaw" to neutralRaw,
        "thunderResistance" to thunderResistance,
        "earthResistance" to earthResistance,
        "waterResistance" to waterResistance,
        "fireResistance" to fireResistance,
        "airResistance" to airResistance,
        "darkResistance" to darkResistance,
        "lightResistance" to lightResistance,
        "neutralResistance" to neutralResistance,
        "bleedingResistance" to bleedingResistance,
        "poisonResistance" to poisonResistance,
        "isDummy" to isDummy
    )
}